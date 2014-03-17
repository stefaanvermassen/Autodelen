package controllers;

import controllers.Security.RoleSecured;
import database.*;
import models.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.api.templates.Html;
import play.data.Form;
import play.mvc.*;
import views.html.reserve.*;
import views.html.reserve.reserve2;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Reserve extends Controller {

    private static final DateTimeFormatter DATEFORMATTER =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

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
            DateTime from = getTimeFrom();
            DateTime until = getTimeFrom();
            if("".equals(from) || "".equals(until)) {
                return "Gelieve zowel een begin als einddatum te selecteren!";
            } else if(from.isAfter(until) || from.isEqual(until)) {
                return "De einddatum kan niet voor de begindatum liggen!";
            } else if(from.isBefore(now)) {
                return "Een reservatie die plaats vindt voor vandaag is ongeldig";
            }
            return null;
        }

    }

    @RoleSecured.RoleAuthenticated()
    public static Result index() {
        return ok(showIndex());
    }

    public static Html showIndex() {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            CarDAO dao = context.getCarDAO();
            List<Car> cars = dao.getCarList();
            return reserve.render(cars);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    @RoleSecured.RoleAuthenticated()
    public static Result reserve(int carId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            CarDAO dao = context.getCarDAO();
            Car car = dao.getCar(carId);

            ReservationDAO rdao = context.getReservationDAO();
            List<Reservation> reservations = rdao.getReservationListForCar(carId);
            // Clean up: delete all reservations that belong to the past
            DateTime now = DateTime.now();
            for(Reservation reservation : reservations) {
                DateTime from = DATEFORMATTER.parseDateTime(reservation.getFrom().toString("yyyy-MM-dd HH:mm:ss"));
                DateTime until = DATEFORMATTER.parseDateTime(reservation.getTo().toString("yyyy-MM-dd HH:mm:ss"));
                if(!from.isAfter(now) && !until.isAfter(now)) {
                    rdao.deleteReservation(reservation);
                }
            }

            return ok(reserve2.render(Form.form(ReservationModel.class), car, reservations));
        } catch(DataAccessException ex) {
            throw ex;
        }
    }

    @RoleSecured.RoleAuthenticated()
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
                return badRequest(reserve2.render(reservationForm, car, reservations));
            }
            try {
                // Test whether the reservation is valid
                DateTime from = reservationForm.get().getTimeFrom();
                DateTime until = reservationForm.get().getTimeUntil();
                for(Reservation reservation : reservations) {
                    if(!from.isAfter(reservation.getTo()) && !until.isBefore(reservation.getFrom())) {
                        reservationForm.reject("De reservatie overlapt met een reeds bestaande reservatie!");
                        return badRequest(reserve2.render(reservationForm, car, reservations));
                    }
                }

                // Create the reservation
                User user = DatabaseHelper.getUserProvider().getUser(session("email"));
                Reservation reservation = rdao.createReservation(from, until, car, user);
                context.commit();

                if (reservation != null) {
                    // TODO: temporary test here if loaner is owner, later adjust
                    if(car.getOwner().getId() == user.getId()) {
                        reservation.setStatus(ReservationStatus.ACCEPTED);
                        rdao.updateReservation(reservation);
                        context.commit();
                    }
                    return redirect(
                            routes.Drives.index() // redirect to drives list
                    );
                } else {
                    reservationForm.error("De reservatie kon niet aangemaakt worden. Contacteer de administrator");
                    return badRequest(reserve2.render(reservationForm, car, reservations));
                }
            } catch(DataAccessException ex) {
                throw ex;
            }
        } catch(DataAccessException ex) {
            throw ex;
        }
    }
}
