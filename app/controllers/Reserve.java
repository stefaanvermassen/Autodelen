package controllers;

import controllers.Security.RoleSecured;
import database.*;
import database.FilterField;
import database.jdbc.JDBCFilter;
import models.*;
import notifiers.Notifier;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.api.templates.Html;
import play.data.Form;
import play.mvc.*;
import views.html.reserve.*;
import views.html.reserve.reservationspage;

import java.util.Iterator;
import java.util.List;

public class Reserve extends Controller {

    private static final int PAGE_SIZE = 10;

    private static final DateTimeFormatter DATEFORMATTER =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    public static class ReservationModel {
        public String from;
        public String until;

        public DateTime getTimeFrom() {
            return DATEFORMATTER.parseDateTime(from).withSecondOfMinute(0);
        }

        public DateTime getTimeUntil() {
            return DATEFORMATTER.parseDateTime(until).withSecondOfMinute(0);
        }

        public String validate() {
            DateTime now = DateTime.now();
            DateTime dateFrom = getTimeFrom();
            DateTime dateUntil = getTimeUntil();
            if("".equals(dateFrom) || "".equals(dateUntil)) {
                return "Gelieve zowel een begin als einddatum te selecteren!";
            } else if(dateFrom.isAfter(dateUntil) || dateFrom.isEqual(dateUntil)) {
                return "De einddatum kan niet voor de begindatum liggen!";
            } else if(dateFrom.isBefore(now)) {
                return "Een reservatie die plaats vindt voor vandaag is ongeldig";
            }
            return null;
        }

    }

    @RoleSecured.RoleAuthenticated({UserRole.CAR_USER, UserRole.CAR_OWNER})
    public static Result index() {
        return ok(showIndex());
    }

    public static Html showIndex() {
        return reservations.render();
    }

    @RoleSecured.RoleAuthenticated({UserRole.CAR_USER, UserRole.CAR_OWNER})
    public static Result reserve(int carId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            CarDAO dao = context.getCarDAO();
            Car car = dao.getCar(carId);

            ReservationDAO rdao = context.getReservationDAO();
            List<Reservation> reservations = rdao.getReservationListForCar(carId);
            Iterator<Reservation> it = reservations.iterator();
            while(it.hasNext())
            {
                Reservation reservation = it.next();
                if(reservation.getStatus() == ReservationStatus.REFUSED)
                    it.remove();
            }

            return ok(reservationDetails.render(Form.form(ReservationModel.class), car, reservations));
        } catch(DataAccessException ex) {
            throw ex;
        }
    }

    @RoleSecured.RoleAuthenticated({UserRole.CAR_USER, UserRole.CAR_OWNER})
    public static Result confirmReservation(int carId) {
        // Get the car object to test whether the operation is legal
        Car car;
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            CarDAO dao = context.getCarDAO();
            car = dao.getCar(carId);
            if (car == null) {
                flash("danger", "De reservatie van deze auto is onmogelijk: auto onbestaand!");
                return badRequest(showIndex());
            }
            ReservationDAO rdao = context.getReservationDAO();
            List<Reservation> reservations = rdao.getReservationListForCar(carId);
            // Request the form
            Form<ReservationModel> reservationForm = Form.form(ReservationModel.class).bindFromRequest();
            if(reservationForm.hasErrors()) {
                return badRequest(reservationDetails.render(reservationForm, car, reservations));
            }
            try {
                // Test whether the reservation is valid
                DateTime from = reservationForm.get().getTimeFrom();
                DateTime until = reservationForm.get().getTimeUntil();
                for(Reservation reservation : reservations) {
                    if(reservation.getStatus() != ReservationStatus.REFUSED &&
                            (from.isBefore(reservation.getTo()) && until.isAfter(reservation.getFrom()))) {
                        reservationForm.reject("De reservatie overlapt met een reeds bestaande reservatie!");
                        return badRequest(reservationDetails.render(reservationForm, car, reservations));
                    }
                }

                // Create the reservation
                User user = DatabaseHelper.getUserProvider().getUser();
                Reservation reservation = rdao.createReservation(from, until, car, user);
                context.commit();

                if (reservation != null) {
                    if(car.getOwner().getId() == user.getId()) {
                        reservation.setStatus(ReservationStatus.ACCEPTED);
                        rdao.updateReservation(reservation);
                        context.commit();
                    } else {
                        Notifier.sendReservationApproveRequestMail(car.getOwner(), reservation);
                    }
                    return redirect(routes.Drives.index());
                } else {
                    reservationForm.error("De reservatie kon niet aangemaakt worden. Contacteer de administrator");
                    return badRequest(reservationDetails.render(reservationForm, car, reservations));
                }
            } catch(DataAccessException ex) {
                throw ex;
            }
        } catch(DataAccessException ex) {
            throw ex;
        }
    }

    // Partial

    public static Result showCarsPage(int page, int asc, String orderBy, String searchString) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            CarDAO dao = context.getCarDAO();

            FilterField orderby = FilterField.stringToField(orderBy);
            orderby = (orderby == null) ? FilterField.CAR_NAME : orderby;

            Filter filter = new JDBCFilter();
            if(searchString != "") {
                String[] searchStrings = searchString.split(",");
                for(String s : searchStrings) {
                    String[] s2 = s.split("=");
                    if(s2.length == 2) {
                        String field = s2[0];
                        String value = s2[1];
                        filter.fieldContains(FilterField.stringToField(field), value);
                    }
                }
            }
            List<Car> listOfCars = dao.getCarList(orderby, (asc == 1), page, PAGE_SIZE, filter);

            int amountOfResults = dao.getAmountOfCars(filter);
            int amountOfPages = (int) Math.ceil( amountOfResults / (double) PAGE_SIZE);

            return ok(reservationspage.render(listOfCars, page, amountOfResults, amountOfPages));
        } catch (DataAccessException ex) {
            throw ex;
        }
    }
}
