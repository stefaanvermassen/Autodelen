package schedulers;

import database.DataAccessContext;
import database.ReservationDAO;
import providers.DataProvider;

/**
 * Created by Cedric on 5/7/2014.
 */
public class CheckFinishedRidesJob implements Runnable {
    @Override
    public void run() {
        try(DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()){
            ReservationDAO dao = context.getReservationDAO();
            dao.updateTable();
            context.commit();
        }
    }
}
