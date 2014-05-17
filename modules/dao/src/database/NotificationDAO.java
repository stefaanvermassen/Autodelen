package database;

import models.Notification;
import models.User;
import models.UserRole;
import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by Stefaan Vermassen on 15/03/14.
 */
public interface NotificationDAO {

    public int getAmountOfNotifications(Filter filter) throws DataAccessException;
    public List<Notification> getNotificationListForUser(int userId) throws DataAccessException; // TODO: delete
    public List<Notification> getNotificationList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;
    public int getNumberOfUnreadNotifications(int userId) throws DataAccessException;
    public Notification createNotification(User user, String subject, String body) throws DataAccessException;
    public void markNotificationAsRead(int notificationId) throws DataAccessException;
    public void markAllNotificationsAsRead(int userId) throws DataAccessException;
}
