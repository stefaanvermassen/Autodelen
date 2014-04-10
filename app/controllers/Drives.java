package controllers;

import controllers.Security.RoleSecured;
import controllers.util.Pagination;
import database.*;
import database.jdbc.JDBCFilter;
import models.*;
import notifiers.Notifier;
import org.joda.time.DateTime;
import play.api.templates.Html;
import play.data.Form;
import play.mvc.*;
import views.html.drives.driveDetails;
import views.html.drives.drivesAdmin;
import views.html.drives.drives;
import views.html.drives.drivespage;

import java.util.List;

/**
 * Controller responsible for the display of (pending) reservations and the processing
 * of pending reservations (approval or refusal of a reservation).
 *
 * A reservation becomes a drive when this reservation is approved by the owner of the
 * reserved car.
 * There is no difference between a drive and reservation apart from the reservation
 * status (a drive has status approved or request_new).
 * If a reservation is approved, in which case it is a drive, extra information will
 * be associated with the reservation.
 *
 */
public class Drives extends Controller {

    private static final int PAGE_SIZE = 10;

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
     * @return the html page of the index page
     */
    public static Html showIndex() { return drives.render(1, 1, "", ""); }

    /**
     * Method: GET
     *
     * @return the html page of the drives page only visible for admins
     */
    @RoleSecured.RoleAuthenticated({UserRole.RESERVATION_ADMIN})
    public static Result drivesAdmin() {
        return ok(drivesAdmin.render());
    }

    /**
     * Method: GET
     *
     * Render the detailpage of a drive/reservation.
     *
     * @param reservationId the id of the reservation of which the details are requested
     * @return the detail page of specific drive/reservation
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.CAR_USER})
    public static Result details(int reservationId) {
        Html result = detailsPage(reservationId);
        if(result != null)
            return ok(result);
        return badRequest(showIndex());
    }

    /**
     * Private method returning the html page of a drive with a new form.
     * @param reservationId The id of the reservation
     * @return the html page
     */
    private static Html detailsPage(int reservationId) {
        return detailsPage(reservationId, Form.form(Reserve.ReservationModel.class), Form.form(RefuseModel.class));
    }

    /**
     * Private method returning the html page of a drive with a given form
     * @param reservationId the id of the reservation/drive
     * @param form The form
     * @return the html page
     */
    private static Html detailsPage(int reservationId, Form<Reserve.ReservationModel> form, Form<RefuseModel> refuseform) {
        User user = DatabaseHelper.getUserProvider().getUser();
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            ReservationDAO rdao = context.getReservationDAO();
            UserDAO udao = context.getUserDAO();
            CarDAO cdao = context.getCarDAO();
            Reservation reservation = rdao.getReservation(reservationId);
            if(reservation == null) {
                flash("Error", "De opgegeven reservatie is onbestaand");
                return null;
            }
            User loaner = udao.getUser(reservation.getUser().getId(), true);
            Car car = cdao.getCar(reservation.getCar().getId());
            if(car == null || loaner == null) {
                flash("Error", "De reservatie bevat ongeldige gegevens");
                return null;
            }
            User owner = udao.getUser(car.getOwner().getId(), true);
            if(owner == null) {
                flash("Error", "De reservatie bevat ongeldige gegevens");
                return null;
            }
            if(!isLoaner(reservation, user) && !isOwnerOfReservedCar(context, user, reservation)) {
                flash("Errror", "U bent niet gemachtigd om deze informatie op te vragen");
                return null;
            }
            return driveDetails.render(form, refuseform, reservation, car, owner, loaner);
        } catch(DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: POST
     *
     * Adjust the details of a drive. That is, adjust the date and time of the drive.
     * It's only allowed to shorten the date and/or time.
     *
     * @param reservationId the id of the reservation/drive
     * @return the detail page of specific drive/reservation after the details where adjusted
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.CAR_USER})
    public static Result adjustDetails(int reservationId) {
        User user = DatabaseHelper.getUserProvider().getUser();
        Form<Reserve.ReservationModel> adjustForm = Form.form(Reserve.ReservationModel.class).bindFromRequest();
        if(adjustForm.hasErrors())
            return badRequest(detailsPage(reservationId, adjustForm, Form.form(RefuseModel.class)));
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            ReservationDAO rdao = context.getReservationDAO();
            Reservation reservation = rdao.getReservation(reservationId);
            if(reservation == null) {
                adjustForm.reject("Er is een fout gebeurt bij het opvragen van de rit.");
                return badRequest(detailsPage(reservationId, adjustForm, Form.form(RefuseModel.class)));
            }
            if(!isLoaner(reservation, user)) {
                adjustForm.reject("U bent niet gemachtigd deze actie uit te voeren.");
                return badRequest(detailsPage(reservationId, adjustForm, Form.form(RefuseModel.class)));
            }
            DateTime from = adjustForm.get().getTimeFrom();
            DateTime until = adjustForm.get().getTimeUntil();
            if(from.isBefore(reservation.getFrom()) || until.isAfter(reservation.getTo())) {
                adjustForm.reject("Het is niet toegestaan de reservatie te verlengen.");
                return badRequest(detailsPage(reservationId, adjustForm, Form.form(RefuseModel.class)));
            }
            if(reservation.getStatus() == ReservationStatus.REFUSED || reservation.getStatus() == ReservationStatus.CANCELLED) {
                adjustForm.reject("U kan deze reservatie niet aanpassen.");
                return badRequest(detailsPage(reservationId, adjustForm, Form.form(RefuseModel.class)));
            }
            reservation.setFrom(from);
            reservation.setTo(until);
            rdao.updateReservation(reservation);
            context.commit();
            return ok(detailsPage(reservationId, adjustForm, Form.form(RefuseModel.class)));
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
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER})
    public static Result approveReservation(int reservationId) {
        Reservation reservation = adjustStatus(reservationId, ReservationStatus.ACCEPTED);
        if(reservation == null)
            return badRequest(showIndex());
        Notifier.sendReservationApprovedByOwnerMail(reservation.getUser(), reservation);
        return ok(detailsPage(reservationId));
    }

    /**
     * Method: POST
     *
     * Called when a reservation of a car is refused by the owner.
     *
     * @param reservationId the id of the reservation being refused
     * @return the drives index page
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER})
    public static Result refuseReservation(int reservationId) {
        Form<Reserve.ReservationModel> form = Form.form(Reserve.ReservationModel.class);
        Form<RefuseModel> refuseForm = Form.form(RefuseModel.class).bindFromRequest();
        if(refuseForm.hasErrors())
            return badRequest(detailsPage(reservationId, form, refuseForm));
        Reservation reservation = adjustStatus(reservationId, ReservationStatus.REFUSED);
        if(reservation == null) {
            return badRequest(showIndex());
        }
        Notifier.sendReservationRefusedByOwnerMail(reservation.getUser(), reservation, refuseForm.get().reason);
        return ok(detailsPage(reservationId, form, refuseForm));
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
            // Both super user and reservation admin are allowed to adjust the status of a reservation
            if(!(DatabaseHelper.getUserRoleProvider().hasRole(user, UserRole.SUPER_USER))
                    && !((DatabaseHelper.getUserRoleProvider().hasRole(user, UserRole.RESERVATION_ADMIN)))) {
                switch (status) {
                    // Only the loaner is allowed to cancel a reservation at any time
                    case CANCELLED:
                        if (!isLoaner(reservation, user)) {
                            flash("Error", "Alleen de ontlener mag een reservatie annuleren!");
                            return null;
                        }
                        break;
                    // The owner is allowed to approve or refuse a reservation if that reservation
                    // has the request or request_new status
                    default:
                        if (!isOwnerOfReservedCar(context, user, reservation)
                                || reservation.getStatus() != ReservationStatus.REQUEST) {
                            flash("Error", "Alleen de eigenaar kan de status van een reservatie aanpassen");
                            return null;
                        }
                }
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
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.CAR_USER})
    public static Result cancelReservation(int reservationId) {
        Reservation reservation = adjustStatus(reservationId, ReservationStatus.CANCELLED);
        if(reservation == null)
            return badRequest(showIndex());
        return index();
    }

    /**
     * Get the number of reservations having the provided status
     * @param status The statuses
     * @return The number of reservations
     */
    public static int reservationsWithStatus(ReservationStatus status) {
        try(DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            ReservationDAO dao = context.getReservationDAO();
            return dao.numberOfReservationsWithStatus(status);
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

    // RENDERING THE PARTIAL

    /**
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
        Filter filter = new JDBCFilter();
        if(searchString != "") {
            String[] searchStrings = searchString.split(",");
            for(String s : searchStrings) {
                String[] s2 = s.split("=");
                if(s2.length == 2) {
                    String f = s2[0];
                    String v = s2[1];
                    filter.fieldIs(FilterField.stringToField(f), v);
                }
            }
        }

        User user = DatabaseHelper.getUserProvider().getUser();
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            ReservationDAO dao = context.getReservationDAO();

            if(field == null) {
                field = FilterField.FROM;
            }

            // We only want reservations from the current user (or his car(s))
            filter.fieldIs(FilterField.RESERVATION_USER_OR_OWNER_ID, "" + user.getId());

            List<Reservation> listOfReservations = dao.getReservationListPage(field, asc, page, PAGE_SIZE, filter);

            int amountOfResults = dao.getAmountOfReservations(filter);
            int amountOfPages = (int) Math.ceil( amountOfResults / (double) PAGE_SIZE);

            return ok(drivespage.render(user.getId(), Form.form(RefuseModel.class), listOfReservations, page, amountOfResults, amountOfPages, ascInt, orderBy, searchString));
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

}
