package controllers;

import database.DatabaseHelper;
import models.User;
import play.mvc.*;
import views.html.drives.*;

public class Drives extends Controller {

    public static Result index() {
        return ok(drives.render());
    }

}
