package schedulers;

import models.Job;
import play.Logger;

/**
 * Created by Cedric on 5/3/2014.
 */
public class ReportGenerationJob implements ScheduledJobExecutor {
    @Override
    public void execute(Job job) {
        Logger.debug("Generate report...");
    }
}
