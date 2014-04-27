package schedulers;

import controllers.routes;
import database.*;
import models.Notification;
import models.User;
import notifiers.Notifier;

import java.util.List;

/**
 * Created by Stefaan Vermassen on 26/04/14.
 */
public class SendUnreadNotificationsMailScheduler extends Scheduler{

    @Override
    public void run() {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
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
