package database;

import models.Approval;
import models.User;

/**
 * Created by Cedric on 3/30/2014.
 */
public interface ApprovalDAO {
    public Approval getApproval(User user) throws DataAccessException;
}
