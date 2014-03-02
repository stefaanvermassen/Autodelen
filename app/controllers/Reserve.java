package controllers;

import database.DatabaseHelper;
import models.User;
import play.mvc.*;
import views.html.*;

public class Reserve extends Controller {

    public static Result index() {
        return ok(reserve.render());
    }

    public static Result details() {
    	return ok(reserve2.render());
    }

}
