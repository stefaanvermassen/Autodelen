package controllers;

import controllers.Security.RoleSecured;
import database.*;
import models.User;
import models.UserRole;
import play.mvc.*;
import views.html.*;

public class Settings extends Controller {

    public static Result index() {
        return ok(settings.render());
    }

    @RoleSecured.RoleAuthenticated()
    public static Result instantAdmin(){
        if(DatabaseHelper.getUserRoleProvider().hasRole(session("email"), UserRole.ADMIN)) {
            flash("warning", "U heeft reeds administratorrechten.");
            return badRequest(dashboard.render());
        } else {
            User user = DatabaseHelper.getUserProvider().getUser(session("email"));
            try(DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                UserRoleDAO roleDao = context.getUserRoleDAO();
                roleDao.addUserRole(user.getId(), UserRole.ADMIN);
                context.commit();

                DatabaseHelper.getUserRoleProvider().invalidateRoles(user.getId());
                flash("success", "U heeft nu administratorrechten.");
                return ok(dashboard.render());
            } catch(DataAccessException ex){
                throw ex;
            }
        }
    }

}
