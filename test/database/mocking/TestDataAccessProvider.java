package database.mocking;

import database.DataAccessContext;
import database.DataAccessException;
import database.DataAccessProvider;

/**
 * Created by Cedric on 3/7/14.
 */
public class TestDataAccessProvider implements DataAccessProvider {

    private DataAccessContext context;

    public TestDataAccessProvider(){
        this.context = new TestDataAccessContext();
    }

    @Override
    public DataAccessContext getDataAccessContext() throws DataAccessException {
        return context;
    }
}
