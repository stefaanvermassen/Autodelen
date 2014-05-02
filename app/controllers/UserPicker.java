package controllers;

import controllers.Security.RoleSecured;
import database.*;
import database.jdbc.JDBCFilter;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import providers.DataProvider;

import java.util.List;

public class UserPicker extends Controller {

    private static final int MAX_VISIBLE_RESULTS = 10;

    @RoleSecured.RoleAuthenticated()
    public static Result getList(String search) {
        search = search.trim();
        if (search != "") {
            search = search.replaceAll("\\s+", " ");
            try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
                UserDAO dao = context.getUserDAO();
                String users = "";
                Filter filter = new JDBCFilter();
                filter.putValue(FilterField.USER_NAME, search);
                List<User> results = dao.getUserList(FilterField.USER_NAME, true, 1, MAX_VISIBLE_RESULTS, filter);
                for (User user : results) {
                    String value = user.getFirstName() + " " + user.getLastName();
                    for (String part : search.split(" ")) {
                        value = value.replaceAll("(?i)\\b(" + part + ")", "<#>$1</#>");
                    }
                    value += " (" + user.getId() + ")";

                    users += "<li data-uid=\"" + user.getId() + "\"><a href=\"javascript:void(0)\">" + value.replace("#", "strong") + "</a></li>";
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
