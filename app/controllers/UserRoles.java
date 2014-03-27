package controllers;

import controllers.Security.RoleSecured;
import database.*;
import models.User;
import models.UserRole;
import play.api.templates.Html;
import play.mvc.*;

import scala.Tuple2;
import views.html.userroles.*;

import java.util.*;

public class UserRoles extends Controller {

    private static final int PAGE_SIZE = 10;

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result index() {
        return ok(overview.render());
    }

    /**
     *
     * @param page The page in the userlists
     * @param ascInt An integer representing ascending (1) or descending (0)
     * @param orderBy A field representing the field to order on
     * @param searchString A string witth form field1:value1,field2:value2 representing the fields to filter on
     * @return A partial page with a table of users of the corresponding page
     */
    @RoleSecured.RoleAuthenticated()
    public static Result showUsersPage(int page, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField carField = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);
        return ok(userList(page, carField, asc, filter));
    }

    private static Html userList(int page, FilterField orderBy, boolean asc, Filter filter) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();

            if(orderBy == null) {
                orderBy = FilterField.USER_NAME;
            }
            List<User> listOfUsers = dao.getUserList(orderBy, asc, page, PAGE_SIZE, filter);

            int amountOfResults = dao.getAmountOfUsers(filter);
            int amountOfPages = (int) Math.ceil( amountOfResults / (double) PAGE_SIZE);

            return userspage.render(listOfUsers, page, amountOfResults, amountOfPages);
        } catch (DataAccessException ex) {
            throw ex;
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
            User user = udao.getUser(userId, true);
            if (user == null) {
                flash("danger", "GebruikersID " + userId + " bestaat niet.");
                return badRequest(overview.render());
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
     *
     * @param userId
     * @return
     */
    @SuppressWarnings("unchecked")
    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result editPost(int userId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO udao = context.getUserDAO();
            User user = udao.getUser(userId, true);
            if (user == null) {
                flash("danger", "GebruikersID " + userId + " bestaat niet.");
                return badRequest(overview.render());
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

                        // Invalidate the cache for a page refresh
                        DatabaseHelper.getUserRoleProvider().invalidateRoles(user.getId());

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