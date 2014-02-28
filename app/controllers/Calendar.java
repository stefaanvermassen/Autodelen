package controllers;

import controllers.Security.RoleSecured;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.calendar.*;

/**
 * Created by Cedric on 2/18/14.
 */
public class Calendar extends Controller {

    @RoleSecured.RoleAuthenticated()
    public static Result index() {
        return ok(calendar.render());
    }
}
