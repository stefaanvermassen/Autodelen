package controllers;

import controllers.Security.RoleSecured;
import database.*;
import models.User;
import models.UserRole;
import play.mvc.*;

import scala.Tuple2;
import views.html.userroles.*;

import java.util.*;

public class UserRoles extends Controller {

    /**
     * Method: GET
     * Shows a list of all users and their roles
     * @return A table with all users and their userroles
     */
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
     * Returns a form to edit a users roles
     * @param userId
     * @return
     */
    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result edit(int userId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO udao = context.getUserDAO();
            User user = udao.getUser(userId, true);
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

    /**
     * Creates a mapping between all roles and a boolean whether the rule is set in the provided role set
     * @param assignedRoles The roles which the user has enabled
     * @return An array of all roles and their corresponding status (enabled | disabled)
     */
    @SuppressWarnings("unchecked")
    public static Tuple2<UserRole, Boolean>[] getUserRolesStatus(Set<UserRole> assignedRoles) {
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
     * Finilizes a userrole edit submission form and saves the new roles to the database
     * @param userId The user ID which has been edited
     * @return The user edit page with the newly assigned roles if successfull, error message otherwise
     */
    @SuppressWarnings("unchecked")
    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result editPost(int userId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO udao = context.getUserDAO();
            User user = udao.getUser(userId, true);
            if (user == null) {
                flash("danger", "GebruikersID " + userId + " bestaat niet.");
                return badRequest(overview.render(udao.getAllUsers()));
            } else {
                UserRoleDAO dao = context.getUserRoleDAO();
                Set<UserRole> oldRoles = dao.getUserRoles(userId);

                Map<String, String[]> map = request().body().asFormUrlEncoded();
                String[] checkedVal = map.get("role"); // get selected topics

                // Parse the POST values whether they contain the roles (only checked get posted)
                Set<UserRole> newRoles = EnumSet.of(UserRole.USER);
                if (checkedVal != null) {
                    for (String strRole : checkedVal) {
                        newRoles.add(Enum.valueOf(UserRole.class, strRole));
                    }
                }

                // Get all newly assigned roles
                Set<UserRole> addedRoles = EnumSet.copyOf(newRoles);
                addedRoles.removeAll(oldRoles);

                // Get all removed roles
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

                        // Invalidate the cache for a page refresh
                        DatabaseHelper.getUserRoleProvider().invalidateRoles(user);

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