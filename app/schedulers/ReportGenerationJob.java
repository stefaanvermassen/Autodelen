package schedulers;

import controllers.Receipts;
import database.DataAccessContext;
import database.DataAccessException;
import database.FilterField;
import database.UserDAO;
import models.Job;
import models.User;
import play.Logger;
import providers.DataProvider;

import java.sql.Date;
import java.util.List;

/**
 * Created by Cedric on 5/3/2014.
 */
public class ReportGenerationJob implements ScheduledJobExecutor {
    @Override
    public void execute(Job job) {

        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            context.getCarRideDAO().endPeriod();
            context.getRefuelDAO().endPeriod();
            context.getCarCostDAO().endPeriod();

            UserDAO dao = context.getUserDAO();

            List<User> users = dao.getUserList(FilterField.USER_NAME, true, 1, dao.getAmountOfUsers(null), null);
            context.commit();
            for(User user : users) {
                Receipts.generateReceipt(user, new Date(java.util.Calendar.getInstance().getTime().getTime()));
            }
        }catch(DataAccessException ex) {
            throw ex;
        }
    }
}
