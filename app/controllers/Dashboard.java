package controllers;

import controllers.Security.RoleSecured;
import play.mvc.*;

import views.html.*;

public class Dashboard extends Controller {

    @RoleSecured.RoleAuthenticated()
    public static Result index() {
        return ok(dashboard.render());
    }

}
