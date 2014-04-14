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
    public List<Approval> getApprovals(User user) throws DataAccessException;
    public Approval getApproval(int approvalId) throws DataAccessException;
    public Approval createApproval(User user, InfoSession session, DateTime time) throws DataAccessException;
}
