package database;

import database.jdbc.JDBCDataAccessProvider;
import database.providers.UserProvider;

/**
 * Created by Cedric on 2/16/14.
 */
public class DatabaseHelper {

    //TODO: this class needs a decent implementation or alternative
    private static DataAccessProvider accessProvider;
    private static UserProvider userProvider;

    public static UserProvider getUserProvider() {
        if (userProvider == null) {
            userProvider = new UserProvider(getDataAccessProvider());
        }
        return userProvider;
    }

    public static DataAccessProvider getDataAccessProvider() {
        if (accessProvider == null) {
            try {
                accessProvider = new JDBCDataAccessProvider();
            } catch (Exception ex) {
                throw new DataAccessException("Failed to load MySQL driver", ex);
            }
        }
        return accessProvider;
    }
}
