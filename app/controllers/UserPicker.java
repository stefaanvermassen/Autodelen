package controllers;

import controllers.Security.RoleSecured;
import database.DataAccessContext;
import database.DataAccessException;
import database.DatabaseHelper;
import database.UserDAO;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;

public class UserPicker extends Controller {

    @RoleSecured.RoleAuthenticated()
    public static Result getList(String search) {
        search = search.trim();
        if (search != "") {
            search = search.replaceAll("\\s+", " ");
            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                UserDAO dao = context.getUserDAO();
                String users = "";
                for (User user : dao.searchUsers(search)) {
                    String value = user.getFirstName() + " " + user.getLastName();
                    for (String part : search.split(" ")) {
                        value = value.replaceAll("(?i)\\b(" + part + ")", "<#>$1</#>");
                    }

                    users += "<li data-uid=\"" + user.getEmail() + "\"><a href=\"javascript:void(0)\">" + value.replace("#", "strong") + "</a></li>";
                }
                return ok(users);
            } catch (DataAccessException ex) {
                throw ex;//TODO?
            }
        } else {
            return ok();
        }
    }
}
