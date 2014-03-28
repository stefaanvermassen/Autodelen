package controllers;

import controllers.Security.RoleSecured;
import database.*;
import models.User;
import models.UserRole;
import play.mvc.*;
import views.html.*;

import java.util.Set;

public class Settings extends Controller {

    public static Result index() {
        return ok(settings.render());
    }


    /**
     * Method: GET
     * Temporary method to create a superuser
     * @return Redirect to the userrole page
     */
    @Deprecated
    @RoleSecured.RoleAuthenticated()
    public static Result instantAdmin() {
        User user = DatabaseHelper.getUserProvider().getUser();
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserRoleDAO dao = context.getUserRoleDAO();
            Set<UserRole> roles = dao.getUserRoles(user.getId());
            if (roles.contains(UserRole.SUPER_USER)) {
                flash("warning", "U heeft reeds superuser rechten.");
                return badRequest(dashboard.render());
            } else {
                try {
                    dao.addUserRole(user.getId(), UserRole.SUPER_USER);
                    context.commit();
                    DatabaseHelper.getUserRoleProvider().invalidateRoles(user);
                    roles.add(UserRole.SUPER_USER);

                    flash("success", "U heeft nu superuserrechten. Gelieve je extra rechten aan te duiden.");
                    return ok(views.html.userroles.editroles.render(UserRoles.getUserRolesStatus(roles), user));
                } catch (DataAccessException ex) {
                    context.rollback();
                    throw ex;
                }
            }
        } catch (DataAccessException ex) {
            throw ex;
        }

    }

}
