package database;

import database.providers.NotificationProvider;
import database.providers.UserProvider;
import database.providers.UserRoleProvider;
import play.Logger;

/**
 * Created by Cedric on 2/16/14.
 */
public class DatabaseHelper {

    //TODO: this class needs a decent implementation or alternative
    private static DataAccessProvider accessProvider;
    private static UserProvider userProvider;
    private static UserRoleProvider userRoleProvider;
    private static NotificationProvider notificationProvider;

    public static UserProvider getUserProvider() {
        if (userProvider == null) {
            userProvider = new UserProvider(getDataAccessProvider());
        }
        return userProvider;
    }

    public static UserRoleProvider getUserRoleProvider() {
        if (userRoleProvider == null) {
            userRoleProvider = new UserRoleProvider(getDataAccessProvider(), getUserProvider());
        }
        return userRoleProvider;
    }

    public static NotificationProvider getNotificationProvider() {
        if (notificationProvider == null) {
            notificationProvider = new NotificationProvider(getDataAccessProvider(), getUserProvider());
        }
        return notificationProvider;
    }

    public static void setDataAccessProvider(DataAccessProvider provider) {
        if(accessProvider != null) {
            Logger.info("DatabaseProvider changed to " + provider.getClass().getCanonicalName());
        }

        accessProvider = provider;
        userRoleProvider = null;
        userProvider = null;
    }

    public static DataAccessProvider getDataAccessProvider() {
        if (accessProvider == null) {
            throw new NullPointerException("No databaseprovider has been specified.");
        }
        return accessProvider;
    }
}
