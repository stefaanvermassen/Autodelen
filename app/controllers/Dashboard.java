package controllers;

import controllers.Security.RoleSecured;
import database.DataAccessContext;
import database.DataAccessException;
import database.UserDAO;
import models.User;
import play.mvc.*;

import providers.DataProvider;
import views.html.*;
import views.html.dashboard;

public class Dashboard extends Controller {

    @RoleSecured.RoleAuthenticated()
    public static Result index() {
            User currentUser = DataProvider.getUserProvider().getUser();
            return ok(dashboard.render(currentUser, Profile.getProfileCompleteness(currentUser)));
    }

}
