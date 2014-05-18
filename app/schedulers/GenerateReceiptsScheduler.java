package schedulers;

import controllers.Receipts;
import database.*;
import models.User;
import providers.DataProvider;

import java.sql.Date;
import java.util.List;

public class GenerateReceiptsScheduler implements Runnable {

    @Override
    public void run() {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            context.getCarRideDAO().endPeriod();
            context.getRefuelDAO().endPeriod();
            context.getCarCostDAO().endPeriod();

            List<User> users = context.getUserDAO().getUserList(FilterField.USER_NAME, true, 1, dao.getAmountOfUsers(null), null);
            context.commit();
            for(User user : users) {
                Receipts.generateReceipt(user, new Date(java.util.Calendar.getInstance().getTime().getTime()));
            }
        }catch(DataAccessException ex) {
            throw ex;
        }
    }


}
