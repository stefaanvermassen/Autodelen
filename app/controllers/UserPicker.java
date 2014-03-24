package controllers;

import database.DataAccessContext;
import database.DataAccessException;
import database.DatabaseHelper;
import database.UserDAO;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;

public class UserPicker extends Controller {

    public static Result getList(String search) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();
            String users = "";
            for (User user : dao.searchUsers(search)) {
                users += "<li>" + user.getFirstName() + "</li>";
            }
            return ok(users);
        } catch (DataAccessException ex) {
            throw ex;//TODO?
        }
    }

}
