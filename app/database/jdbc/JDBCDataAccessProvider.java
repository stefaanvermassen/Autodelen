package database.jdbc;

import database.DataAccessContext;
import database.DataAccessException;
import database.DataAccessProvider;
import play.db.DB;

/**
 * Created by Cedric on 2/16/14.
 */
public class JDBCDataAccessProvider implements DataAccessProvider {

    @Override
    public DataAccessContext getDataAccessContext() throws DataAccessException {
        return new JDBCDataAccessContext(DB.getConnection());
    }

}
