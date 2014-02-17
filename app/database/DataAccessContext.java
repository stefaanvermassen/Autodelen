package database;

/**
 * Created by Cedric on 2/16/14.
 */
public interface DataAccessContext extends AutoCloseable {
        public UserDAO getUserDAO ();
        public void begin();
        public void commit();
        public void rollback();
        public void close();
}