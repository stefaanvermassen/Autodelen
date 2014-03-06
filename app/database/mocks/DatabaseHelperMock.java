package database.mocks;

import database.DataAccessProvider;
import database.providers.UserProvider;
import database.providers.UserRoleProvider;

public class DatabaseHelperMock {

	private static DataAccessProvider accessProvider;

    public static UserProvider getUserProvider() {
        return null;
    }

    public static UserRoleProvider getUserRoleProvider(){
        return null; 
    }

    public static DataAccessProvider getDataAccessProvider() {
        if (accessProvider == null) {
        	accessProvider = new DataAccessProviderMock();
        }
        return accessProvider;
    }
	
}
