package controllers;

import controllers.Security.RoleSecured;
import database.DataAccessContext;
import database.DataAccessException;
import database.DatabaseHelper;
import database.NotificationDAO;
import models.Notification;
import models.User;
import play.mvc.*;
import views.html.*;
import views.html.messages;

import java.util.List;

public class Messages extends Controller {



    @RoleSecured.RoleAuthenticated()
    public static Result showNotifications() {
        User user = DatabaseHelper.getUserProvider().getUser(session("email"));
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            NotificationDAO dao = context.getNotificationDAO();
            List<Notification> notifications = dao.getNotificationListForUser(user.getId());
            return ok(messages.render(notifications));
        } catch (DataAccessException ex) {
            throw ex;
        }

    }

}
