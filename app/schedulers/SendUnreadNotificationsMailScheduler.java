package schedulers;

import controllers.routes;
import database.*;

import java.util.List;

/**
 * Created by Stefaan Vermassen on 26/04/14.
 */
public class SendUnreadNotificationsMailScheduler extends Scheduler{

    /*Send mail to user if he has more than 3 unread messages or notifications   */
    @Override
    public void run() {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            SchedulerDAO dao = context.getSchedulerDAO();
            List<String> emailList = dao.getReminderEmailList(2);
            context.commit();
            for(String emailAdres : emailList){
                //todo: notifier
            }
        }catch(DataAccessException ex) {
            throw ex;
        }

    }
}
