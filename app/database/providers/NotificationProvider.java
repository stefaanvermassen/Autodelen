package database.providers;

import database.DataAccessContext;
import database.DataAccessException;
import database.DataAccessProvider;
import database.NotificationDAO;
import models.Notification;
import models.User;
import play.cache.Cache;

import java.util.List;

/**
 * Created by stefaan on 20/03/14.
 */
public class NotificationProvider {

    private static final String NOTIFICATIONS_BY_ID = "notification:id:%d";
    private DataAccessProvider provider;
    private UserProvider userProvider;

    public NotificationProvider(DataAccessProvider provider, UserProvider userProvider) {
        this.provider = provider;
        this.userProvider = userProvider;
    }

    public List<Notification> getNotificationsByUserMail(String email){
        User user = userProvider.getUser(email);
        return getNotifications(user.getId());
    }

    public List<Notification> getNotifications(int userId) {
        return getNotifications(userId, true);
    }

    public List<Notification> getNotifications(int userId, boolean cached) {
        String key = String.format(NOTIFICATIONS_BY_ID, userId);
        Object obj = null;
        if (cached) {
            obj = Cache.get(key);
        }
        if (obj == null || !(obj instanceof List)) {
            try (DataAccessContext context = provider.getDataAccessContext()) {
                NotificationDAO dao = context.getNotificationDAO();
                List<Notification> notifications = dao.getNotificationListForUser(userId);
                if (notifications != null) {
                    Cache.set(key, notifications);
                    return notifications;
                } else {
                    return null;
                }
            } catch (DataAccessException ex) {
                throw ex;
            }
        } else {
            return (List<Notification>) obj; //Type erasure problem from Java, works at runtime
        }
    }
}
