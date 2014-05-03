package database;

import models.Job;
import models.JobType;
import org.joda.time.DateTime;

import java.util.List;

public interface JobDAO {
    public List<Job> getUnfinishedBefore(DateTime time) throws DataAccessException;
    public Job createJob(JobType type, int refId, DateTime when) throws DataAccessException;
    public void deleteJob(int jobId) throws DataAccessException;
    public void deleteJob(JobType type, int refId) throws DataAccessException;
    public void setJobStatus(int jobId, boolean finished) throws DataAccessException;
}