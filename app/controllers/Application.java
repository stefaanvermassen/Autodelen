package controllers;

import play.Routes;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render());
    }

    /**
     * Javascript routes allowing the calling of actions on the server from
     * Javascript as if they were invoked directly in the script.
     */
    public static Result javascriptRoutes() {
        response().setContentType("text/javascript");
        return ok(
                Routes.javascriptRouter("myJsRoutes",
                        // Routes
                        routes.javascript.Cars.showCarsPage(),
                        routes.javascript.InfoSessions.showUpcomingSessionsPage(),
                        routes.javascript.Reserve.showCarsPage()
                )
        );
    }

}
