package schedulers;

import database.DataAccessContext;
import database.DataAccessException;
import database.JobDAO;
import database.SchedulerDAO;
import models.Job;
import models.JobType;
import models.User;
import notifiers.Notifier;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import play.Logger;
import play.libs.Akka;
import providers.DataProvider;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Stefaan Vermassen on 26/04/14.
 * Modified by Cedric Van Goethem on 3/5/14 for persistent scheduling
 */
public class Scheduler implements Runnable {


    private static final Map<JobType, ScheduledJobExecutor> EXECUTORS;
    static {
        EXECUTORS = new EnumMap<>(JobType.class);
        EXECUTORS.put(JobType.IS_REMINDER, new InfoSessionReminderJob());
    }
    private static Scheduler scheduler;
    private static Object lock = new Object();

    private ExecutorService cachedPool;

    private boolean isRunning = false;

    public static Scheduler getInstance() {
        if (scheduler == null) {
            scheduler = new Scheduler(); //TODO thread safe singleton
        }
        return scheduler;
    }

    private ConcurrentHashMap<Long, Job> runningJobs;

    public Scheduler() {
        runningJobs = new ConcurrentHashMap<>();
        cachedPool = Executors.newCachedThreadPool();
    }

    public void start() {
        if (!isRunning) {
            checkInitialReports();
            int refresh = getSecondsInterval(); // refresh rate in seconds
            schedule(Duration.create(refresh, TimeUnit.SECONDS), this);
            isRunning = true;
        }
    }

    /**
     * Checks if there's a report already scheduled
     */
    private void checkInitialReports() {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            JobDAO dao = context.getJobDAO();
            Job reportJob = dao.getLastJobForType(JobType.REPORT);
            if (reportJob == null) {
                try {
                    DateTime scheduledFor = firstDayOfNextMonth();
                    dao.createJob(JobType.REPORT, 0, scheduledFor);
                    context.commit();
                    Logger.info("Scheduled next report for " + scheduledFor.toString());
                } catch(DataAccessException ex){
                    Logger.error("Failed to schedule report job", ex);
                }
            }
        }
    }

    private int getSecondsInterval() {
        return DataProvider.getSettingProvider().getIntOrDefault("scheduler_interval", 5 * 60);
    }

    public void schedule(FiniteDuration repeatDuration, Runnable task) {
        Akka.system().scheduler().schedule(
                Duration.create(0, TimeUnit.MILLISECONDS), //Initial delay 0 milliseconds
                repeatDuration,     //Frequency
                task,
                Akka.system().dispatcher()
        );
    }

    @Override
    public void run() {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            JobDAO dao = context.getJobDAO();
            List<Job> jobs = dao.getUnfinishedBefore(new DateTime());
            for(Job job : jobs){
                if(!runningJobs.contains(job)){
                    runningJobs.put(job.getId(), job); // Add the job to the already scheduled pool
                    cachedPool.submit(new ScheduledJob(job));
                }
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    private class ScheduledJob implements Runnable {
        private Job job;
        public ScheduledJob(Job job){
            this.job = job;
        }

        @Override
        public void run() {
            try {
                ScheduledJobExecutor executor = EXECUTORS.get(job.getType());
                if (executor == null) {
                    Logger.error("No executor for type: " + job.getType());
                } else {
                    executor.execute(job);
                    try(DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
                        JobDAO dao = context.getJobDAO();
                        dao.setJobStatus(job.getId(), true);
                        context.commit();
                        Logger.info("Finished job: " + job);
                    }
                }
            } catch(Exception ex){
                Logger.error("Error during " + job.toString() + ": " + ex.getMessage());
            } finally {
                runningJobs.remove(job.getId());
            }
        }
    }

    private static DateTime firstDayOfNextMonth() {
        MutableDateTime mdt = new MutableDateTime();
        mdt.addMonths(1);
        mdt.setDayOfMonth(1);
        mdt.setMillisOfDay(0); // if you want to make sure you're at midnight
        return mdt.toDateTime();
    }


}
