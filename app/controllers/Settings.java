package controllers;

import database.DatabaseHelper;
import models.User;
import play.mvc.*;
import views.html.*;

public class Settings extends Controller {

    public static Result index() {
        return ok(settings.render());
    }

}
