package database.mocks;

import database.DataAccessContext;
import database.DataAccessException;
import database.DataAccessProvider;

public class DataAccessProviderMock implements DataAccessProvider{

	@Override
	public DataAccessContext getDataAccessContext() throws DataAccessException {
		return new DataAccessContextMock();
	}

}
