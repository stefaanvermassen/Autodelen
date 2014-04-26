package controllers;

import controllers.Security.RoleSecured;
import database.DataAccessContext;
import database.DataAccessException;
import database.DatabaseHelper;
import database.RefuelDAO;
import models.Refuel;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.refuels.refuels;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 26/04/14.
 */
public class Refuels extends Controller {

    /**
     * Method: GET
     *
     * @return index page containing all the refuel requests
     */
    @RoleSecured.RoleAuthenticated()
    public static Result showRefuels() {
        User user = DatabaseHelper.getUserProvider().getUser();
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            RefuelDAO dao = context.getRefuelDAO();
            List<Refuel> refuelList = dao.getRefuelsForUser(user.getId());
            return ok(refuels.render(refuelList));
        } catch (DataAccessException ex) {
            throw ex;
        }
    }
}
