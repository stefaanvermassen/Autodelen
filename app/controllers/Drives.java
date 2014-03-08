package controllers;

import controllers.Security.RoleSecured;
import database.*;
import models.Car;
import models.Reservation;
import models.ReservationStatus;
import models.User;
import play.api.templates.Html;
import play.mvc.*;
import views.html.drives.*;

import java.util.List;

public class Drives extends Controller {

    @RoleSecured.RoleAuthenticated()
    public static Result index() {
        return ok(showIndex());
    }

    public static Html showIndex() {
        User user = DatabaseHelper.getUserProvider().getUser(session("email"));
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            ReservationDAO dao = context.getReservationDAO();
            List<Reservation> reservations = dao.getReservationList(user.getId());
            return drives.render(user.getId(), reservations);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    @RoleSecured.RoleAuthenticated()
    public static Result aproveReservation(int reservationId) {
        return adjustStatus(reservationId, ReservationStatus.ACCEPTED);
    }

    @RoleSecured.RoleAuthenticated()
    public static Result refuseReservation(int reservationId) {
        return adjustStatus(reservationId, ReservationStatus.REFUSED);
    }

    public static Result adjustStatus(int reservationId, ReservationStatus status) {
        User user = DatabaseHelper.getUserProvider().getUser(session("email"));
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            ReservationDAO dao = context.getReservationDAO();
            Reservation reservation = dao.getReservation(reservationId);
            if(reservation == null) {
                flash("danger", "De actie die u wilt uitvoeren is ongeldig: reservatie onbestaand");
                return badRequest(showIndex());
            }
            CarDAO cdao = context.getCarDAO();
            Car car = cdao.getCar(user.getId());
            if(car.getId() != reservation.getCar().getId()) {
                flash("danger", "U bent niet geautoriseerd voor het uitvoeren van deze actie");
                return badRequest(showIndex());
            }
            reservation.setStatus(status);
            dao.updateReservation(reservation);
            context.commit();
            return index();
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
            CarDAO cdao = context.getCarDAO();
            Car car = cdao.getCar(user.getId());
            if(car != null) {
                if(car.getOwner().getId() != user.getId() && reservation.getUser().getId() != user.getId()) {
                    flash("danger", "U bent niet geauthoriseerd voor het uitvoeren van deze actie");
                    return badRequest(showIndex());
                }
            }
            else{
                if(reservation.getUser().getId() != user.getId()) {
                    flash("danger", "U bent niet geauthoriseerd voor het uitvoeren van deze actie");
                    return badRequest(showIndex());
                }
            }
            dao.deleteReservation(reservation);
            context.commit();
            return index();
        } catch(DataAccessException ex) {
            throw ex;
        }
    }
}
