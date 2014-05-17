package controllers;

import controllers.Security.RoleSecured;
import database.*;
import models.Reservation;
import models.User;
import play.api.templates.Html;
import play.data.Form;
import play.mvc.*;

import providers.DataProvider;
import views.html.*;
import views.html.dashboard;

import java.util.List;

public class Dashboard extends Controller {

    @RoleSecured.RoleAuthenticated()
    public static Result index() {
        return ok(dashboard());
    }

    public static Html dashboard() {
        User currentUser = DataProvider.getUserProvider().getUser();
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            ReservationDAO dao = context.getReservationDAO();
            List<Reservation> reservations = dao.getReservationListForUser(currentUser.getId());
            return dashboard.render(currentUser, reservations, Form.form(Reserve.IndexModel.class),
                    Profile.getProfileCompleteness(currentUser), InfoSessions.didUserGoToInfoSession(),
                    InfoSessions.approvalRequestSent());
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

}
