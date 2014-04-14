package controllers;

import controllers.Security.RoleSecured;
import controllers.util.Pagination;
import database.*;
import models.Notification;
import models.User;
import play.api.templates.Html;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.notifiers.*;

import java.util.List;

/**
 * Created by stefaan on 22/03/14.
 */
public class Notifications extends Controller {

    private static final int PAGE_SIZE = 10;

    /**
     * Method: GET
     *
     * @return index page containing all the received notifications of a specific user
     */
    @RoleSecured.RoleAuthenticated()
    public static Result showNotifications() {
       return ok(notifications.render());
    }

    @RoleSecured.RoleAuthenticated()
    public static Result showNotificationsPage(int page, int ascInt, String orderBy, String searchString) {
        User user = DatabaseHelper.getUserProvider().getUser();
        FilterField field = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        filter.putValue(FilterField.USER_ID, user.getId() + "");
        return ok(notificationList(page, field, asc, filter));


    }

    private static Html notificationList(int page, FilterField orderBy, boolean asc, Filter filter) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            NotificationDAO dao = context.getNotificationDAO();

            List<Notification> list = dao.getNotificationList(orderBy, asc, page, PAGE_SIZE, filter);

            int amountOfResults = dao.getAmountOfNotifications(filter);
            int amountOfPages = (int) Math.ceil( amountOfResults / (double) PAGE_SIZE);

            return notificationspage.render(list, page, amountOfResults, amountOfPages);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

}
