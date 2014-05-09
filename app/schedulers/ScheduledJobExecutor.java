package schedulers;

import models.Job;

/**
 * Created by Cedric on 5/3/2014.
 */
public interface ScheduledJobExecutor {
    public void execute(Job job);
}
