package database;

import database.jdbc.JDBCDataAccessProvider;

/**
 * Created by Cedric on 2/16/14.
 */
public class DatabaseHelper {

    //TODO: this class needs a decent implementation or alternative
    private static DataAccessProvider accessProvider;

    public static DataAccessProvider getDataAccessProvider() {
        if(accessProvider == null){
            accessProvider = new JDBCDataAccessProvider("/conf/database.properties");
        }
        return accessProvider;
    }
}
