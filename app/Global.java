/**
 * Created by Cedric on 3/7/14.
 */
import database.*;
import database.jdbc.JDBCDataAccessProvider;
import models.EmailTemplate;
import models.MailType;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.*;
import play.data.format.Formatters;
import providers.DataProvider;
import scala.concurrent.duration.Duration;
import schedulers.Scheduler;
import schedulers.SendUnreadNotificationsMailScheduler;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Global extends GlobalSettings {

    // Tests if all templates are in the database, and if the database works
    private void testDatabase(){
        try(DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()){
            TemplateDAO dao = context.getTemplateDAO();
            StringBuilder sb = new StringBuilder();
            for(MailType type : MailType.values()){
                if(dao.getTemplate(type) == null)
                    sb.append(type + ", ");
            }
            if(sb.length() > 0)
                throw new RuntimeException("Missing database templates for: " + sb.toString());
        } catch(DataAccessException ex){
            throw ex;
        }
    }

    public void onStart(Application app) {
        try {
            DataProvider.setDataAccessProvider(new JDBCDataAccessProvider(DatabaseConfiguration.getConfiguration("conf/database.properties")));
            testDatabase();

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

        Scheduler scheduler = Scheduler.getInstance(); // This sho
        scheduler.start();
        scheduler.schedule(Duration.create(1, TimeUnit.HOURS) ,new SendUnreadNotificationsMailScheduler());
    }

    public void onStop(Application app) {
        //Logger.info("Application shutdown...");
    }

}
