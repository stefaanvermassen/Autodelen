package database.jdbc;

import database.ApprovalDAO;
import database.DataAccessException;
import models.Approval;
import models.InfoSession;
import models.User;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cedric on 3/30/2014.
 */
public class JDBCApprovalDAO implements ApprovalDAO {

    private static final String APPROVAL_FIELDS = "approval_id, approval_admin, approval_submission, approval_date, approval_infosession, approval_status";
    private Connection connection;

    private PreparedStatement createApprovalStatement;
    private PreparedStatement getApprovalByIdStatement;
    private PreparedStatement getApprovalByUserStatement;

    public JDBCApprovalDAO(Connection connection){
        this.connection = connection;
    }

    private PreparedStatement getGetApprovalByUserStatement() throws SQLException {
        if(getApprovalByUserStatement == null){
            getApprovalByUserStatement = connection.prepareStatement("SELECT " + APPROVAL_FIELDS + " FROM approvals WHERE approval_user = ?");
        }
        return getApprovalByUserStatement;
    }

    private PreparedStatement getGetApprovalByIdStatement() throws SQLException {
        if(getApprovalByIdStatement == null){
            getApprovalByIdStatement = connection.prepareStatement("SELECT " + APPROVAL_FIELDS + " FROM approvals WHERE approval_id = ?");
        }
        return getApprovalByIdStatement;
    }

    private PreparedStatement getCreateApprovalStatement() throws SQLException {
        if(createApprovalStatement == null){
            createApprovalStatement = connection.prepareStatement("INSERT INTO approvals(approval_user, approval_submission, approval_infosession, approval_status) VALUES(?,?,?,?)", new String[]{"approval_id"});
        }
        return createApprovalStatement;
    }

    private Approval populateApproval(ResultSet rs) throws SQLException {
        //TODO: include admin, user and infosession
        return new Approval(rs.getInt("approval_id"), null, null, new DateTime(rs.getTimestamp("approval_submission").getTime()),
                rs.getTimestamp("approval_date") != null ? new DateTime(rs.getTimestamp("approval_Date").getTime()) : null,
                null);
    }

    @Override
    public List<Approval> getApprovals(User user) throws DataAccessException {
        try {
            PreparedStatement ps = getGetApprovalByUserStatement();
            ps.setInt(1, user.getId());

            try(ResultSet rs = ps.executeQuery()) {
                List<Approval> approvals = new ArrayList<>();
                while(rs.next()){
                    approvals.add(populateApproval(rs));
                }
                return approvals;
            } catch(SQLException ex){
                throw new DataAccessException("Failed to read approval resultset.", ex);
            }
        } catch(SQLException ex){
            throw new DataAccessException("Failed to get approvals for user.", ex);
        }
    }

    @Override
    public Approval getApproval(int approvalId) throws DataAccessException {
        try {
            PreparedStatement ps = getGetApprovalByIdStatement();
            ps.setInt(1, approvalId);

            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return populateApproval(rs);
                else return null;
            } catch(SQLException ex){
                throw new DataAccessException("Failed to read approval resultset.", ex);
            }
        } catch(SQLException ex){
            throw new DataAccessException("Failed to get approvals for user.", ex);
        }
    }

    @Override
    public Approval createApproval(User user, InfoSession session, DateTime time) {
        try {
            PreparedStatement ps = getCreateApprovalStatement();
            ps.setInt(1, user.getId());
            ps.setTimestamp(2, new Timestamp(time.getMillis()));
            ps.setInt(3, session.getId());
            ps.setString(4, Approval.ApprovalStatus.PENDING.name());

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating approval request.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new Approval(keys.getInt(1), user, null, time, null, session);
            } catch(SQLException ex){
                throw new DataAccessException("Failed to create approval.", ex);
            }
        } catch(SQLException ex){
            throw new DataAccessException("Failed to create approval request", ex);
        }
    }
}
