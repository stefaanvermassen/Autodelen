package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.infosession.*;

/**
 * Created by Cedric on 2/21/14.
 */
public class InfoSession extends Controller {

    //TODO: admin attribute
    @Security.Authenticated(Secured.class)
    public static Result newSession() {
        return ok(newsession.render());
    }
}
