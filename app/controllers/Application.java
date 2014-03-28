package controllers;

import play.Routes;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render());
    }

    // Javascript routes
    public static Result javascriptRoutes() {
        response().setContentType("text/javascript");
        return ok(
                Routes.javascriptRouter("myJsRoutes",
                        // Routes
                        routes.javascript.Cars.showCarsPage(),
                        routes.javascript.InfoSessions.showUpcomingSessionsPage(),
                        routes.javascript.Reserve.showCarsPage(),
                        routes.javascript.InfoSessions.enrollSession()
                )
        );
    }

}
