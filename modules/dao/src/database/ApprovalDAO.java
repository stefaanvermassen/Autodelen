package database;

import models.Approval;
import models.InfoSession;
import models.User;
import org.joda.time.DateTime;
import java.util.List;

/**
 * Created by Cedric on 3/30/2014.
 */
public interface ApprovalDAO {

    public List<Approval> getPendingApprovals(User user) throws DataAccessException;
    public List<Approval> getPendingApprovals() throws DataAccessException;
    public List<Approval> getApprovals(int page, int pageSize) throws DataAccessException;
    public int getApprovalCount() throws DataAccessException;
    public void setApprovalAdmin(Approval approval, User admin) throws DataAccessException;
    public List<Approval> getApprovals(User user) throws DataAccessException;
    public Approval getApproval(int approvalId) throws DataAccessException;
    public Approval createApproval(User user, InfoSession session, String userMessage) throws DataAccessException;
    public void updateApproval(Approval approval) throws DataAccessException;
}
