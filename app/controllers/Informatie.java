package controllers;

import database.DatabaseHelper;
import models.User;
import play.mvc.*;

import views.html.*;

public class Informatie extends Controller {

    public static Result index() {
        return ok(informatie.render());
    }

}
