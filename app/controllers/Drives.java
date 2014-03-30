package controllers;

import controllers.Security.RoleSecured;
import controllers.util.Pagination;
import database.*;
import models.*;
import notifiers.Notifier;
import play.api.templates.Html;
import play.data.Form;
import play.mvc.*;
import views.html.drives.driveDetails;
import views.html.drives.drives;
import views.html.drives.drivespage;

import java.util.List;

/**
 * Controller responsible for the display of (pending) reservations and the processing
 * of pending reservations (approval or refusal of a reservation).
 *
 */
public class Drives extends Controller {

    private static final int PAGE_SIZE = 5;

    /**
     * Class implementing a model wrapped in a form.
     * This model is used during the form submission when an owner does not
     * approve the reservation of his car.
     * The owner is obligated to inform the loaner why his reservation request
     * is denied.
     */
    public static class RefuseModel {
        // String containing the reason for refusing a reservation
        public String reason;

        /**
         * Validates the form:
         * - the owner must explain why he does not approve a reservation
         * @return an error string or null
         */
        public String validate() {
            if("".equals(reason))
                return "Gelieve mee te delen waarom u deze aanvraag weigert.";
            return null;
        }

    }

    /**
     * Method: GET
     *
     * @return the drives index page containing all (pending) reservations of the user or for his car.
     */
    @RoleSecured.RoleAuthenticated()
    public static Result index() {
        return ok(showIndex());
    }

    /**
     * @return the html page of drives
     */
    public static Html showIndex() {
        return showIndex(null, 0, 1, 1, "", "");
    }

    /**
     * @param form form wrapped with a RefuseModel in order to display possible errors after validating the form.
     * @param errorIndex index refering to the drive that caused errors in the form
     * @return The html page of drives
     */
    public static Html showIndex(Form<RefuseModel> form, int errorIndex, int page, int asc, String orderBy, String filter) {
        if(form == null)
            return drives.render(errorIndex, Form.form(RefuseModel.class), page, asc, orderBy, filter);
        return drives.render(errorIndex, form, page, asc, orderBy, filter);

    }

    /**
     *
     * @param page The page in the drivelists
     * @param ascInt An integer representing ascending (1) or descending (0)
     * @param orderBy A field representing the field to order on
     * @param searchString A string witth form field1:value1,field2:value2 representing the fields to filter on
     * @return A partial page with a table of cars of the corresponding page (only available to car_user+)
     */
    @RoleSecured.RoleAuthenticated()
    public static Result showDrivesPage(int page, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField field = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        return ok(driveList(page, field, asc, filter, ascInt, orderBy, searchString));
    }

    /*
     * I pass ascInt, orderByString and searchString again so I can use them in the drivespage (where I need them in refuseReservation)
     */
    private static Html driveList(int page, FilterField orderBy, boolean asc, Filter filter, int ascInt, String orderByString, String searchString) {
        User user = DatabaseHelper.getUserProvider().getUser();
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            ReservationDAO dao = context.getReservationDAO();

            if(orderBy == null) {
                orderBy = FilterField.FROM;
            }

            // We only want reservations from the current user (or his car(s))
            filter.fieldIs(FilterField.RESERVATION_USER_OR_OWNER_ID, "" + user.getId());

            List<Reservation> listOfReservations = dao.getReservationListPage(orderBy, asc, page, PAGE_SIZE, filter);

            int amountOfResults = dao.getAmountOfReservations(filter);
            int amountOfPages = (int) Math.ceil( amountOfResults / (double) PAGE_SIZE);

            return drivespage.render(user.getId(), Form.form(RefuseModel.class), listOfReservations, page, amountOfResults, amountOfPages, ascInt, orderByString, searchString);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     *
     * Render the detailpage of a drive/reservation.
     *
     * @param reservationId the id of the reservation of which the details are requested
     * @return the detail page of specific drive/reservation
     */
    @RoleSecured.RoleAuthenticated()
    public static Result details(int reservationId) {
        User user = DatabaseHelper.getUserProvider().getUser();
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            ReservationDAO rdao = context.getReservationDAO();
            UserDAO udao = context.getUserDAO();
            CarDAO cdao = context.getCarDAO();
            Reservation reservation = rdao.getReservation(reservationId);
            User loaner = udao.getUser(reservation.getUser().getId(), true);
            Car car = cdao.getCar(reservation.getCar().getId());
            User owner = udao.getUser(car.getOwner().getId(), true);
            if(reservation == null || car == null || loaner == null || owner == null)
                return badRequest(showIndex());
            if(!isLoaner(reservation, user) && !isOwnerOfReservedCar(context, user, reservation))
                return badRequest(showIndex());
            return ok(driveDetails.render(reservation, car, owner, loaner));
        } catch(DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     *
     * Called when a reservation of a car is approved by the owner.
     *
     * @param reservationId The id of the reservation being approved
     * @return the drives index page
     */
    @RoleSecured.RoleAuthenticated()
    public static Result approveReservation(int reservationId) {
        Reservation reservation = adjustStatus(reservationId, ReservationStatus.ACCEPTED);
        if(reservation == null)
            return badRequest(showIndex());
        Notifier.sendReservationApprovedByOwnerMail(reservation.getUser(), reservation);
        return index();
    }

    /**
     * Method: POST
     *
     * Called when a reservation of a car is refused by the owner.
     *
     * @param reservationId the id of the reservation being refused
     * @param errorIndex index indicating index of the reservation being refused
     * @return the drives index page
     */
    @RoleSecured.RoleAuthenticated()
    public static Result refuseReservation(int reservationId, int errorIndex, int page, int ascInt, String orderBy, String filter) {
        Form<RefuseModel> refuseForm = Form.form(RefuseModel.class).bindFromRequest();
        if(refuseForm.hasErrors())
            return badRequest(showIndex(refuseForm, errorIndex, page, ascInt, orderBy, filter));
        Reservation reservation = adjustStatus(reservationId, ReservationStatus.REFUSED);
        if(reservation == null) {
            return badRequest(showIndex());
        }
        Notifier.sendReservationRefusedByOwnerMail(reservation.getUser(), reservation, refuseForm.get().reason);
        return index();
    }

    /**
     * Adjust the status of a given reservation for a car.
     * This method can only be called by the owner of the car and only if the reservation is not yet approved/refused.
     * @param reservationId the id of the reservation for which the status ought the be adjusted
     * @param status the status to which the reservation is to be set
     * @return the reservation if successful, null otherwise
     */
    public static Reservation adjustStatus(int reservationId, ReservationStatus status) {
        User user = DatabaseHelper.getUserProvider().getUser();
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            ReservationDAO dao = context.getReservationDAO();
            Reservation reservation = dao.getReservation(reservationId);
            if(reservation == null) {
                flash("danger", "De actie die u wilt uitvoeren is ongeldig: reservatie onbestaand");
                return null;
            }
            if(!isOwnerOfReservedCar(context, user, reservation) || reservation.getStatus() != ReservationStatus.REQUEST) {
                flash("danger", "U bent niet geauthoriseerd voor het uitvoeren van deze actie");
                return null;
            }
            reservation.setStatus(status);
            dao.updateReservation(reservation);
            context.commit();
            return reservation;
        } catch(DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     *
     * Called when a reservation of a car is cancelled by the loaner.
     *
     * @param reservationId the id of the reservation being cancelled
     * @return the drives index page
     */
    @RoleSecured.RoleAuthenticated()
    public static Result cancelReservation(int reservationId) {
        User user = DatabaseHelper.getUserProvider().getUser();
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            ReservationDAO dao = context.getReservationDAO();
            Reservation reservation = dao.getReservation(reservationId);
            if(reservation == null) {
                flash("danger", "De actie die u wilt uitvoeren is ongeldig: reservatie onbestaand");
                return badRequest(showIndex());
            }
            if(!isLoaner(reservation, user)) {
                flash("danger", "U bent niet geauthoriseerd voor het uitvoeren van deze actie");
                return badRequest(showIndex());
            }
            dao.deleteReservation(reservation);
            context.commit();
            return index();
        } catch(DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Private method to determine whether the user is owner of the car belonging to a reservation.
     * @param context the data access context required to communicate with the database
     * @param user the user who is possibly the owner of the car
     * @param reservation the reservation containing the car
     * @return true if the user is the owner, false otherwise
     */
    private static boolean isOwnerOfReservedCar(DataAccessContext context, User user, Reservation reservation) {
        CarDAO cdao = context.getCarDAO();
        List<Car> cars = cdao.getCarsOfUser(user.getId());
        boolean isOwner = false;
        int index = 0;
        while(!isOwner && index < cars.size()){
            if(cars.get(index).getId() == reservation.getCar().getId())
                isOwner = true;
            index++;
        }
        return isOwner;
    }

    /**
     * Private method to determine whether the user is loaner of the car belonging to a reservation.
     * @param reservation the reservation containing the car
     * @param user the user who is possibly the loaner of the car
     * @return true if the user is the owner, false otherwise
     */
    private static boolean isLoaner(Reservation reservation, User user) {
        return reservation.getUser().getId() == user.getId();
    }
}
