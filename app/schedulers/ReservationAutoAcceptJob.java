package schedulers;

import database.DataAccessContext;
import database.ReservationDAO;
import models.Job;
import models.Reservation;
import models.ReservationStatus;
import notifiers.Notifier;
import providers.DataProvider;

/**
 * Created by Cedric on 5/7/2014.
 */
public class ReservationAutoAcceptJob implements ScheduledJobExecutor {
    @Override
    public void execute(Job job) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            ReservationDAO dao = context.getReservationDAO();
            Reservation reservation = dao.getReservation(job.getRefId());
            if(reservation == null) {
                return;
            }

            if(reservation.getStatus() == ReservationStatus.REQUEST ) {
                if(reservation.getFrom().isBeforeNow()) {
                    reservation.setStatus(ReservationStatus.ACCEPTED);
                    Notifier.sendReservationApprovedByOwnerMail(reservation.getUser(), "Automatisch goedgekeurd door systeem.", reservation);
                } else {
                    reservation.setStatus(ReservationStatus.CANCELLED);
                }
                dao.updateReservation(reservation);
                context.commit();
            }
        }

    }
}
