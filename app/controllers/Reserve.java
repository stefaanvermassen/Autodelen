package controllers;

import controllers.Security.RoleSecured;
import controllers.util.Pagination;
import database.*;
import database.FilterField;
import database.jdbc.JDBCFilter;
import models.*;
import notifiers.Notifier;
import org.joda.time.DateTime;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.api.templates.Html;
import play.data.Form;
import play.mvc.*;
import providers.DataProvider;
import views.html.reserve.*;
import views.html.reserve.reservationDetailsPartial;
import views.html.reserve.reservations;
import views.html.reserve.reservationspage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Controller responsible to display and filter cars for reservation and to enable a user to reserve a car.
 *
 */
public class Reserve extends Controller {

    // The number of cars displayed in the table of the index page
    private static final int PAGE_SIZE = 10;

    // Formatter to translate a string to a datetime
    private static final DateTimeFormatter DATEFORMATTER =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    /**
     * Class implementing a model wrapped in a form.
     * This model is used during the form submission when a user submits
     * a reservation for a car.
     * The user is obligated to provide information about the reservation:
     * - the start date and time
     * - the end date and time
     */
    public static class ReservationModel {
        // Date and time from which the user wants to loan the car
        public String from;
        // Date and time the user will return the car to the owner
        public String until;

        /**
         * @return the start datetime of the reservation
         */
        public DateTime getTimeFrom() {
            return DATEFORMATTER.parseDateTime(from).withSecondOfMinute(0);
        }

        /**
         * @return the end datetime of the reservation
         */
        public DateTime getTimeUntil() {
            return DATEFORMATTER.parseDateTime(until).withSecondOfMinute(0);
        }

        /**
         * Validates the form:
         * - the start date and time, and the end date and time are specified
         * - the start date and time of a reservation is before the end date and time
         * - the start date is after the date of today
         * @return an error string or null
         */
        public String validate() {
            DateTime dateFrom = null;
            DateTime dateUntil = null;
            try {
                dateFrom = getTimeFrom();
                dateUntil = getTimeUntil();
            } catch(IllegalArgumentException ex) {
                if(dateFrom == null)
                    return "Ongeldig datum: van = " + from;
                else
                    return "Ongeldig datum: tot = " + until;
            }
            if("".equals(dateFrom) || "".equals(dateUntil))
                return "Gelieve zowel een begin als einddatum te selecteren!";
            else if(dateFrom.isAfter(dateUntil) || dateFrom.isEqual(dateUntil))
                return "De einddatum kan niet voor de begindatum liggen!";
            return null;
        }

    }

    /**
     * Method: GET
     *
     * @return the reservation index page containing all cars
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_USER})
    public static Result index() {
        return ok(showIndex());
    }

    /**
     * @return The html context of the reservations index page
     */
    public static Html showIndex() {
        return reservations.render("");
    }

    /**
     * Method: GET
     *
     * Render the details page of a future reservation for a car where the user is able to
     * confirm the reservation and specify the start and end of the reservation
     *
     * @param carId the id of the car for which the reservationsdetails ought to be rendered
     * @param from the string containing the date and time of the start of the reservation
     * @param until the string containing the date and time of the end of the reservation
     * @return the details page of a future reservation for a car
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_USER})
    public static Result reserve(int carId, String from, String until){
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()){
            CarDAO dao = context.getCarDAO();
            Car car = dao.getCar(carId);
            if (car == null) {
                flash("danger", "De reservatie van deze auto is onmogelijk: auto onbestaand!");
                return badRequest(showIndex());
            } else {
                return ok(reservationDetailsPartial.render(car, from, until, Form.form(ReservationModel.class)));
            }
        } catch(DataAccessException ex){
            throw ex; //log?
        }
    }

    /**
     * Method: POST
     *
     * Confirmation of a reservation. The reservation is validated.
     * If the reservation is valid, the reservation is created and the owner is
     * informed of the request for a reservation.
     *
     * @param carId The id of the car for which the reservation is being confirmed
     * @return the user is redirected to the drives page
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_USER})
    public static Result confirmReservation(int carId) {
        // Get the car object to test whether the operation is legal
        Car car;
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            CarDAO dao = context.getCarDAO();
            car = dao.getCar(carId);
            if (car == null) {
                flash("danger", "De reservatie van deze auto is onmogelijk: auto onbestaand!");
                return badRequest(showIndex());
            }
            ReservationDAO rdao = context.getReservationDAO();
            List<Reservation> res = rdao.getReservationListForCar(carId);
            // Request the form
            Form<ReservationModel> reservationForm = Form.form(ReservationModel.class).bindFromRequest();
            if(reservationForm.hasErrors()) {
                return badRequest(reservations.render(reservationForm.globalError().message()));
            }
            try {
                // Test whether the reservation is valid
                DateTime from = reservationForm.get().getTimeFrom();
                DateTime until = reservationForm.get().getTimeUntil();
                for(Reservation reservation : res) {
                    if((reservation.getStatus() != ReservationStatus.REFUSED && reservation.getStatus() != ReservationStatus.CANCELLED) &&
                            (from.isBefore(reservation.getTo()) && until.isAfter(reservation.getFrom()))) {
                        return badRequest(reservations.render("De reservatie overlapt met een reeds bestaande reservatie!"));
                    }
                }

                // Create the reservation
                User user = DataProvider.getUserProvider().getUser();
                Reservation reservation = rdao.createReservation(from, until, car, user);

                // Schedule the auto accept
                JobDAO jdao = context.getJobDAO();
                int minutesAfterNow = DataProvider.getSettingProvider().getIntOrDefault("reservation_auto_accept", 4320);
                MutableDateTime autoAcceptDate = new MutableDateTime();
                autoAcceptDate.addMinutes(minutesAfterNow);
                jdao.createJob(JobType.RESERVE_ACCEPT, reservation.getId(), autoAcceptDate.toDateTime());

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
                } else
                    return badRequest(reservations.render("De reservatie kon niet aangemaakt worden. Contacteer de administrator"));
            } catch(DataAccessException ex) {
                throw ex;
            }
        } catch(DataAccessException ex) {
            throw ex;
        }
    }

    // Partial

    /**
     * Method: GET
     *
     * Method to render a partial page containing an amount of cars for reservation that ought to be displayed
     * corresponding a:
     * - a search string
     * - the number of cars to be rendered per page
     * - the page that's being rendered
     *
     * @param page the page to be rendered
     * @param ascInt boolean int, if 1 the records are ordered ascending
     * @param orderBy the string designating the filterfield on which to order
     * @param searchString the string containing all search information
     * @return the requested page of cars for reservation
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_USER})
    public static Result showCarsPage(int page, int ascInt, String orderBy, String searchString) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            CarDAO dao = context.getCarDAO();

            FilterField field = FilterField.stringToField(orderBy);

            boolean asc = Pagination.parseBoolean(ascInt);
            Filter filter = Pagination.parseFilter(searchString);

            if(field == null) {
                field = FilterField.CAR_NAME;
            }
            try {
                DATEFORMATTER.parseDateTime(filter.getValue(FilterField.FROM));
                DATEFORMATTER.parseDateTime(filter.getValue(FilterField.UNTIL));
            } catch(IllegalArgumentException ex) {
                return ok(reservationspage.render(new ArrayList<Car>(), page, 0, 0, false));
            }

            List<Car> listOfCars = dao.getCarList(field, asc, page, PAGE_SIZE, filter);

            int amountOfResults = dao.getAmountOfCars(filter);
            int amountOfPages = (int) Math.ceil( amountOfResults / (double) PAGE_SIZE);

            return ok(reservationspage.render(listOfCars, page, amountOfResults, amountOfPages, true));
        } catch (DataAccessException ex) {
            throw ex;
        }
    }
}
