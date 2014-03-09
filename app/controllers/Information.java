package controllers;

import database.DatabaseHelper;
import models.User;
import play.mvc.*;

import views.html.homepage.*;

public class Information extends Controller {

    public static Result index() {
        return ok(information.render());
    }

}
