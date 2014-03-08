/**
 * Created by Cedric on 3/7/14.
 */
import database.DatabaseConfiguration;
import database.DatabaseHelper;
import database.jdbc.JDBCDataAccessProvider;
import play.*;

import java.io.IOException;
import java.io.InputStream;

public class Global extends GlobalSettings {

    public void onStart(Application app) {
        try {
            DatabaseHelper.setDataAccessProvider(new JDBCDataAccessProvider(DatabaseConfiguration.getConfiguration("conf/database.properties")));
        } catch(IOException ex){
            Logger.error("Could not load database properties: " + ex.getMessage());
        }
    }

    public void onStop(Application app) {
        //Logger.info("Application shutdown...");
    }

}
