package controllers;

import controllers.Security.RoleSecured;
import database.*;
import models.User;
import models.UserRole;
import org.h2.engine.Database;
import play.mvc.*;

import scala.Tuple2;
import views.html.userroles.*;

import java.util.Set;

public class UserRoles extends Controller {

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result index() {
        return ok(overview.render());
    }

    /**
     * Method: GET
     *
     * @param userId
     * @return
     */
    @SuppressWarnings("unchecked")
    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result edit(int userId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO udao = context.getUserDAO();
            User user = udao.getUser(userId);
            if (user == null) {
                flash("danger", "GebruikersID " + userId + " bestaat niet.");
                return badRequest(overview.render());
            } else {
                UserRoleDAO dao = context.getUserRoleDAO();
                Set<UserRole> roles = dao.getUserRoles(userId);
                UserRole[] allRoles = UserRole.values();
                Tuple2<UserRole, Boolean>[] filtered = new Tuple2[allRoles.length - 1];
                int k = 0;
                for (int i = 0; i < allRoles.length; ++i) {
                    if (allRoles[i] != UserRole.USER) { //TODO: review whole USER role, this thing is a hack and can be left out
                        filtered[k++] = new Tuple2<>(allRoles[i], roles.contains(allRoles[i]));
                    }
                }
                return ok(editroles.render(filtered, user));
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }


    /**
     * Method: POST
     *
     * @param userId
     * @return
     */
    @SuppressWarnings("unchecked")
    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result editPost(int userId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO udao = context.getUserDAO();
            User user = udao.getUser(userId);
            if (user == null) {
                flash("danger", "GebruikersID " + userId + " bestaat niet.");
                return badRequest(overview.render());
            } else {
                UserRoleDAO dao = context.getUserRoleDAO();
                Set<UserRole> roles = dao.getUserRoles(userId);
                return ok("Received request.");
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

}
