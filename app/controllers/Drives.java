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
import views.html.drives.drives;

import java.util.List;

public class Drives extends Controller {

    public static class RefuseModel {
        public String reason;

        public String validate() {
            if("".equals(reason))
                return "Gelieve mee te delen waarom u deze aanvraag weigert.";
            return null;
        }

    }

    @RoleSecured.RoleAuthenticated()
    public static Result index() {
        return ok(showIndex());
    }

    public static Html showIndex() {
        return showIndex(null, -1);
    }

    public static Html showIndex(Form<RefuseModel> form, int errorIndex) {
        User user = DatabaseHelper.getUserProvider().getUser(session("email"));
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

    @RoleSecured.RoleAuthenticated()
    public static Result approveReservation(int reservationId) {
        User user = DatabaseHelper.getUserProvider().getUser(session("email"));
        Reservation reservation = adjustStatus(reservationId, ReservationStatus.ACCEPTED);
        if(reservation == null)
            return badRequest(showIndex());
        Notifier.sendReservationApprovedByOwnerMail(user, reservation);
        return index();
    }

    @RoleSecured.RoleAuthenticated()
    public static Result refuseReservation(int reservationId, int errorIndex) {
        User user = DatabaseHelper.getUserProvider().getUser(session("email"));
        Form<RefuseModel> refuseForm = Form.form(RefuseModel.class).bindFromRequest();
        if(refuseForm.hasErrors())
            return badRequest(showIndex(refuseForm, errorIndex));
        Reservation reservation = adjustStatus(reservationId, ReservationStatus.REFUSED);
        if(reservation == null) {
            return badRequest(showIndex());
        }
        Notifier.sendReservationRefusedByOwnerMail(user, reservation, refuseForm.get().reason);
        return index();
    }

    public static Reservation adjustStatus(int reservationId, ReservationStatus status) {
        User user = DatabaseHelper.getUserProvider().getUser(session("email"));
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            ReservationDAO dao = context.getReservationDAO();
            Reservation reservation = dao.getReservation(reservationId);
            if(reservation == null) {
                flash("danger", "De actie die u wilt uitvoeren is ongeldig: reservatie onbestaand");
                return null;
            }
            if(!isOwnerOfReservedCar(context, user, reservation)) {
                flash("danger", "U bent niet geauthoriseerd voor het uitvoeren van deze actie");
                return null;
            }
            reservation.setStatus(status);
            dao.updateReservation(reservation);
            context.commit();
            return null;
        } catch(DataAccessException ex) {
            throw ex;
        }
    }

    @RoleSecured.RoleAuthenticated()
    public static Result cancelReservation(int reservationId) {
        User user = DatabaseHelper.getUserProvider().getUser(session("email"));
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            ReservationDAO dao = context.getReservationDAO();
            Reservation reservation = dao.getReservation(reservationId);
            if(reservation == null) {
                flash("danger", "De actie die u wilt uitvoeren is ongeldig: reservatie onbestaand");
                return badRequest(showIndex());
            }
            // TODO: only loaner may cancel the reservation
            if(!isOwnerOfReservedCar(context, user, reservation) && !isLoaner(reservation, user)) {
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

    private static boolean isLoaner(Reservation reservation, User user) {
        return reservation.getUser().getId() == user.getId();
    }
}
