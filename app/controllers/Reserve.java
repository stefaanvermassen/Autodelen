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
import views.html.*;
import views.html.reserve2;

import java.util.List;

public class Reserve extends Controller {

    private static final DateTimeFormatter DATEFORMATTER =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static class ReservationModel {
        public String from;
        public String until;

        public DateTime getTimeFrom() {
            return DATEFORMATTER.parseDateTime(from);
        }

        public DateTime getTimeUntil() {
            return DATEFORMATTER.parseDateTime(until);
        }

        public String validate() {
            // Should not happen, but just in case
            if("".equals(from) || "".equals(until)) {
                return "Gelieve zowel een begin als einddatum te selecteren!";
            }
            // Should also not happen
            else if(getTimeFrom().isAfter(getTimeUntil())) {
                return "De einddatum kan niet voor de begindatum liggen!";
            }
            return null;
        }

    }

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
            return ok(reserve2.render(Form.form(ReservationModel.class), car));
        } catch(DataAccessException ex) {
            throw ex;
        }
    }

    @RoleSecured.RoleAuthenticated()
    public static Result confirmReservation(int carId) {
        Car car;
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            CarDAO dao = context.getCarDAO();
            car = dao.getCar(carId);
            if (car == null) {
                flash("danger", "De reservatie van deze auto is onmogelijk: auto onbestaand!");
                return badRequest(showIndex());
            }
        } catch(DataAccessException ex) {
            throw ex;
        }
        Form<ReservationModel> reservationForm = Form.form(ReservationModel.class).bindFromRequest();
        if(reservationForm.hasErrors()) {
            return badRequest(reserve2.render(reservationForm, car));
        }
        try(DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            try {
                User user = DatabaseHelper.getUserProvider().getUser(session("email"));


                ReservationDAO rdao = context.getReservationDAO();
                Reservation reservation = rdao.createReservation(reservationForm.get().getTimeFrom(),
                        reservationForm.get().getTimeUntil(), car, user);
                context.commit();

                if (reservation != null) {
                    return redirect(
                            routes.Reserve.index() // return to infosession list
                    );
                } else {
                    reservationForm.error("De reservatie kon niet aangemaakt worden. Contacteer de administrator");
                    return badRequest(reserve2.render(reservationForm, car));
                }
            } catch(DataAccessException ex) {
                context.rollback();
                throw ex;
            }
        } catch(DataAccessException ex) {
            throw ex;
        }
    }
}
