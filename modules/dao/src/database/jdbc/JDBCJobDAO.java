package database.jdbc;

import database.DataAccessException;
import database.JobDAO;
import models.Job;
import models.JobType;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cedric on 5/3/2014.
 */
public class JDBCJobDAO implements JobDAO {

    private Connection connection;
    private PreparedStatement getUnfinishedJobsAfterStatement;
    private PreparedStatement createJobStatement;
    private PreparedStatement deleteJobByTypeStatement;
    private PreparedStatement deleteJobByIdStatement;
    private PreparedStatement updateJobStatement;

    public JDBCJobDAO(Connection connection){
        this.connection = connection;
    }

    private PreparedStatement getGetUnfinishedJobsAfterStatement() throws SQLException {
        if(getUnfinishedJobsAfterStatement == null){
            getUnfinishedJobsAfterStatement = connection.prepareStatement("SELECT job_id, job_type, job_ref_id, job_time, job_finished FROM jobs WHERE job_finished=0 AND job_time <= ?");
        }
        return getUnfinishedJobsAfterStatement;
    }

    private PreparedStatement getUpdateJobStatement() throws SQLException {
        if(updateJobStatement == null){
            updateJobStatement = connection.prepareStatement("UPDATE jobs SET job_finished=? WHERE job_id=?");
        }
        return updateJobStatement;
    }

    private PreparedStatement getCreateJobStatement() throws SQLException {
        if(createJobStatement == null){
            createJobStatement = connection.prepareStatement("INSERT INTO jobs(job_type, job_ref_id, job_time, job_finished) VALUES(?,?,?,?)", new String[] { "job_id" });
        }
        return createJobStatement;
    }

    private PreparedStatement getDeleteJobByTypeStatement() throws SQLException {
        if(deleteJobByTypeStatement == null){
            deleteJobByTypeStatement = connection.prepareStatement("DELETE FROM jobs WHERE job_type=? AND job_ref_id=?");
        }
        return deleteJobByTypeStatement;
    }

    private PreparedStatement getDeleteJobByIdStatement() throws SQLException {
        if(deleteJobByIdStatement == null){
            deleteJobByIdStatement = connection.prepareStatement("DELETE FROM jobs WHERE job_id=?");
        }
        return deleteJobByIdStatement;
    }

    private Job populateJob(ResultSet rs) throws SQLException {
        return new Job(rs.getInt("job_id"), Enum.valueOf(JobType.class, rs.getString("job_type")),
                new DateTime(rs.getDate("job_time").getTime()), rs.getBoolean("job_finished"), rs.getObject("job_ref_id") == null ? -1 : rs.getInt("job_ref_id"));
    }

    @Override
    public List<Job> getUnfinishedBefore(DateTime time) throws DataAccessException {
        try {
            PreparedStatement ps = getGetUnfinishedJobsAfterStatement();
            ps.setDate(1, new Date(time.getMillis()));
            List<Job> jobs = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    jobs.add(populateJob(rs));
                }
                return jobs;
            } catch(SQLException ex){
                throw new DataAccessException("Failed to read job resultset.", ex);
            }
        } catch(SQLException ex){
            throw new DataAccessException("Failed to fetch unfinished jobs.", ex);
        }
    }

    @Override
    public Job createJob(JobType type, int refId, DateTime when) throws DataAccessException {
        try {
            PreparedStatement ps = getCreateJobStatement();
            ps.setString(1, type.name());
            ps.setInt(2, refId);
            ps.setDate(3, new Date(when.getMillis()));
            ps.setBoolean(4, false);

            if (ps.executeUpdate() != 1)
                throw new DataAccessException("New job record failed. No rows affected.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next())
                    throw new DataAccessException("Failed to read keys for new job record.");
                return new Job(keys.getInt(1), type, when, false, refId);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new job.", ex);
            }
        } catch(SQLException ex){
            throw new DataAccessException("Failed to create job.", ex);
        }
    }

    @Override
    public void deleteJob(int jobId) throws DataAccessException {
        try {
            PreparedStatement ps = getDeleteJobByIdStatement();
            ps.setInt(1, jobId);
            if(ps.executeUpdate() != 1)
                throw new DataAccessException("Failed to delete job by id. No rows affected.");
        } catch(SQLException ex){
            throw new DataAccessException("Failed to delete job.", ex);
        }
    }

    @Override
    public void deleteJob(JobType type, int refId) throws DataAccessException {
        try {
            PreparedStatement ps = getDeleteJobByTypeStatement();
            ps.setString(1, type.name());
            ps.setInt(2, refId);
            ps.executeUpdate();
        } catch(SQLException ex){
            throw new DataAccessException("Failed to delete job.", ex);
        }
    }

    @Override
    public void setJobStatus(int jobId, boolean finished) throws DataAccessException {
        try {
            PreparedStatement ps = getUpdateJobStatement();
            ps.setBoolean(1, finished);
            ps.setInt(2, jobId);

            if(ps.executeUpdate() != 1)
                throw new DataAccessException("Failed to update job status.");
        } catch(SQLException ex){
            throw new DataAccessException("Failed to update job status.", ex);
        }
    }
}
