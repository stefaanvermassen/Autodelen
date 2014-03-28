/**
 * Created by Cedric on 3/7/14.
 */
import database.DatabaseConfiguration;
import database.DatabaseHelper;
import database.jdbc.JDBCDataAccessProvider;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.*;
import play.data.format.Formatters;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Locale;

public class Global extends GlobalSettings {

    public void onStart(Application app) {
        try {
            DatabaseHelper.setDataAccessProvider(new JDBCDataAccessProvider(DatabaseConfiguration.getConfiguration("conf/database.properties")));

            // Register datetime formatter
            play.data.format.Formatters.register(DateTime.class, new Formatters.SimpleFormatter<DateTime>() {
                private final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"); //ISO time without miliseconds

                @Override
                public DateTime parse(String s, Locale locale) throws ParseException {
                    return DATETIME_FORMATTER.parseDateTime(s);
                }

                @Override
                public String print(DateTime dateTime, Locale locale) {
                    return dateTime.toString(DATETIME_FORMATTER);
                }
            });

        } catch(IOException ex){
            Logger.error("Could not load database properties: " + ex.getMessage());
        }
    }

    public void onStop(Application app) {
        //Logger.info("Application shutdown...");
    }

}
