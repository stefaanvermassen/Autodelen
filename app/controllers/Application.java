package controllers;

import play.Routes;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

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
                        controllers.routes.javascript.Cars.showCarsPage(),
                        routes.javascript.Cars.showCarCostsPage(),
                        routes.javascript.Cars.getCarCostModal(),
                        routes.javascript.Cars.updateAvailabilities(),
                        routes.javascript.Cars.updatePriviliged(),
                        routes.javascript.Refuels.provideRefuelInfo(),
                        routes.javascript.Refuels.showUserRefuelsPage(),
                        routes.javascript.Refuels.showOwnerRefuelsPage(),
                        routes.javascript.Damages.editDamage(),
                        routes.javascript.Damages.addStatus(),
                        routes.javascript.Damages.addProof(),
                        routes.javascript.InfoSessions.showUpcomingSessionsPage(),
                        routes.javascript.InfoSessions.showSessionsPage(),
                        routes.javascript.Reserve.showCarsPage(),
                        routes.javascript.Users.showUsersPage(),
                        routes.javascript.UserRoles.showUsersPage(),
                        routes.javascript.EmailTemplates.showExistingTemplatesPage(),
                        routes.javascript.Notifications.showNotificationsPage(),
                        routes.javascript.Messages.showReceivedMessagesPage(),
                        routes.javascript.Messages.showSentMessagesPage(),
                        routes.javascript.InfoSessions.enrollSession(),
                        routes.javascript.Drives.showDrivesPage(),
                        routes.javascript.Maps.getMap(),
                        routes.javascript.InfoSessions.pendingApprovalListPaged(),
                        routes.javascript.Reserve.reserve()
                )
        );
    }

}
