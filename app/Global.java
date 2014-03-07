/**
 * Created by Cedric on 3/7/14.
 */
import database.DatabaseHelper;
import database.jdbc.JDBCDataAccessProvider;
import play.*;

public class Global extends GlobalSettings {

    public void onStart(Application app) {
        DatabaseHelper.setDataAccessProvider(new JDBCDataAccessProvider());
    }

    public void onStop(Application app) {
        //Logger.info("Application shutdown...");
    }

}
