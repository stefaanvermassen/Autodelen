package schedulers;

import database.DataAccessContext;
import database.DataAccessException;
import database.SchedulerDAO;
import models.User;
import notifiers.Notifier;
import play.libs.Akka;
import providers.DataProvider;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Stefaan Vermassen on 26/04/14.
 */
public class SendUnreadNotificationsMailScheduler implements Runnable {

    @Override
    public void run() {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            SchedulerDAO dao = context.getSchedulerDAO();
            //Todo: number_of_unread_messages from system_variable
            List<User> emailList = dao.getReminderEmailList(0);
            context.commit();
            for(User user : emailList){
                Notifier.sendReminderMail(user);
            }
        }catch(DataAccessException ex) {
            throw ex;
        }
    }
}
