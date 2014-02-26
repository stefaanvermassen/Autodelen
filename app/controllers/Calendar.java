package controllers;

import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.calendar.*;

/**
 * Created by Cedric on 2/18/14.
 */
public class Calendar extends Controller {

    @Security.Authenticated(Secured.class)
    public static Result index() {
        return ok(calendar.render());
    }
}