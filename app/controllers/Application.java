package controllers;

import database.DatabaseHelper;
import models.User;
import play.Routes;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render());
    }

    public static Result javascriptRoutes() {
        response().setContentType("text/javascript");
        return ok(
                Routes.javascriptRouter("myJsRoutes",
                        routes.javascript.Cars.showCarsPage(),
                        routes.javascript.InfoSessions.showUpcomingSessionsPage()
                )
        );
    }

}
