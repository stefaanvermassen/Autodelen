package controllers;

import controllers.Security.RoleSecured;
import database.*;
import models.User;
import models.UserRole;
import org.h2.engine.Database;
import play.mvc.*;

import scala.Tuple2;
import views.html.userroles.*;

import java.util.*;

public class UserRoles extends Controller {

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result index() {
        //TODO: User picker / filter (paginated) -> Karsten??
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();
            return ok(overview.render(dao.getAllUsers()));
        } catch (DataAccessException ex) {
            throw ex;//TODO?
        }
    }

    /**
     * Method: GET
     *
     * @param userId
     * @return
     */
    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result edit(int userId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO udao = context.getUserDAO();
            User user = udao.getUser(userId);
            if (user == null) {
                flash("danger", "GebruikersID " + userId + " bestaat niet.");
                return badRequest(overview.render(udao.getAllUsers()));
            } else {
                UserRoleDAO dao = context.getUserRoleDAO();
                Set<UserRole> roles = dao.getUserRoles(userId);
                return ok(editroles.render(getUserRolesStatus(roles), user));
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    private static Tuple2<UserRole, Boolean>[] getUserRolesStatus(Set<UserRole> assignedRoles) {
        UserRole[] allRoles = UserRole.values();
        Tuple2<UserRole, Boolean>[] filtered = new Tuple2[allRoles.length - 1];
        int k = 0;
        for (UserRole allRole : allRoles) {
            if (allRole != UserRole.USER) { //TODO: review whole USER role, this thing is a hack and can be left out
                filtered[k++] = new Tuple2<>(allRole, assignedRoles.contains(allRole));
            }
        }
        return filtered;
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
                return badRequest(overview.render(udao.getAllUsers()));
            } else {
                UserRoleDAO dao = context.getUserRoleDAO();
                Set<UserRole> oldRoles = dao.getUserRoles(userId);

                Map<String, String[]> map = request().body().asFormUrlEncoded();
                String[] checkedVal = map.get("role"); // get selected topics

                Set<UserRole> newRoles = EnumSet.of(UserRole.USER);
                if (checkedVal != null) {
                    for (String strRole : checkedVal) {
                        newRoles.add(Enum.valueOf(UserRole.class, strRole));
                    }
                }

                Set<UserRole> addedRoles = EnumSet.copyOf(newRoles);
                addedRoles.removeAll(oldRoles);

                Set<UserRole> removedRoles = EnumSet.copyOf(oldRoles);
                removedRoles.removeAll(newRoles);

                // Check if a superuser did delete his role by accident (SU roles can only be removed by other SU users)
                if (user.getEmail().equals(session("email")) && removedRoles.contains(UserRole.SUPER_USER)) {
                    flash("danger", "Als superuser kan u uw eigen superuser rechten niet verwijderen.");
                    return badRequest(editroles.render(getUserRolesStatus(oldRoles), user));
                } else {
                    try {
                        for (UserRole removedRole : removedRoles) {
                            dao.removeUserRole(userId, removedRole);
                        }

                        for (UserRole addedRole : addedRoles) {
                            dao.addUserRole(userId, addedRole);
                        }
                        context.commit();

                        flash("success", "Er werden " + addedRoles.size() + " recht(en) toegevoegd en " + removedRoles.size() + " recht(en) verwijderd.");
                        return ok(editroles.render(getUserRolesStatus(newRoles), user));
                    } catch (DataAccessException ex) {
                        context.rollback();
                        throw ex;
                    }
                }
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

}
