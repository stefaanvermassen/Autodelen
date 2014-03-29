package controllers;

import controllers.Security.RoleSecured;
import database.*;
import models.Car;
import models.Reservation;
import models.ReservationStatus;
import models.User;
import notifiers.Notifier;
import play.api.templates.Html;
import play.data.Form;
import play.mvc.*;
import views.html.drives.driveDetails;
import views.html.drives.drives;

import java.util.List;

/**
 * Controller responsible for the display of (pending) reservations and the processing
 * of pending reservations (approval or refusal of a reservation).
 *
 */
public class Drives extends Controller {

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
        return showIndex(null, 0);
    }

    /**
     * @param form form wrapped with a RefuseModel in order to display possible errors after validating the form.
     * @param errorIndex index refering to the drive that caused errors in the form
     * @return The html page of drives
     */
    public static Html showIndex(Form<RefuseModel> form, int errorIndex) {
        User user = DatabaseHelper.getUserProvider().getUser();
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            ReservationDAO dao = context.getReservationDAO();
            List<Reservation> reservations = dao.getReservationListForUser(user.getId());
            if(form == null)
                return drives.render(user.getId(), errorIndex, Form.form(RefuseModel.class),reservations);
            return drives.render(user.getId(), errorIndex, form, reservations);
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
    public static Result refuseReservation(int reservationId, int errorIndex) {
        Form<RefuseModel> refuseForm = Form.form(RefuseModel.class).bindFromRequest();
        if(refuseForm.hasErrors())
            return badRequest(showIndex(refuseForm, errorIndex));
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
