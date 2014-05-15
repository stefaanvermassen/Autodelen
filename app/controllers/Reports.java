package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.reports;

/**
 * Created by Stefaan Vermassen on 15/05/14.
 */
public class Reports extends Controller {

    public static Result index() {
        return ok(reports.render());
    }


}
