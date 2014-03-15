package database;

import models.Notification;
import models.User;
import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by Stefaan Vermassen on 15/03/14.
 */
public interface NotificationDAO {

    public List<Notification> getNotificationListForUser(int userId) throws DataAccessException;
    public Notification createNotification(User user, boolean read, String subject, String body, DateTime timestamp) throws DataAccessException;
}
