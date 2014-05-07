package schedulers;

import controllers.Drives;
import models.Job;
import models.Reservation;
import models.ReservationStatus;
import notifiers.Notifier;

/**
 * Created by Cedric on 5/7/2014.
 */
public class ReservationAutoAcceptJob implements ScheduledJobExecutor {
    @Override
    public void execute(Job job) {
        Reservation reservation = Drives.adjustStatus(job.getRefId(), ReservationStatus.ACCEPTED);
        Notifier.sendReservationApprovedByOwnerMail(reservation.getUser(), "", reservation);
    }
}
