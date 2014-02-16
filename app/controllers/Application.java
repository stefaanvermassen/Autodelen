package controllers;

import models.User;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render(session("email") == null ? null : new User(session("email"), "Joske Vermeulen", "")));
    }

}
