package controllers;

import controllers.Security.RoleSecured;
import database.DataAccessContext;
import database.DataAccessException;
import database.DatabaseHelper;
import database.NotificationDAO;
import models.Notification;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.notifiers.notifications;

import java.util.List;

/**
 * Created by stefaan on 22/03/14.
 */
public class Notifications extends Controller {

    /**
     * Method: GET
     *
     * @return index page containing all the received notifications of a specific user
     */
    @RoleSecured.RoleAuthenticated()
    public static Result showNotifications() {
        User user = DatabaseHelper.getUserProvider().getUser();
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            NotificationDAO dao = context.getNotificationDAO();
            List<Notification> notificationList = dao.getNotificationListForUser(user.getId());
            return ok(notifications.render(notificationList));
        } catch (DataAccessException ex) {
            throw ex;
        }

    }
}
