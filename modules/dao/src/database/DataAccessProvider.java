package database;

/**
 * Created by Cedric on 2/16/14.
 */
public interface DataAccessProvider {
        public DataAccessContext getDataAccessContext() throws DataAccessException;
}
