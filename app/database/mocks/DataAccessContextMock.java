package database.mocks;


import database.*;
import database.jdbc.*;

import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

/**
 * Created by Cedric on 2/16/14.
 */
public class DataAccessContextMock implements DataAccessContext {

    private UserDAO userDAO;
    private InfoSessionDAO infoSessionDAO;
    private AddressDAO addressDAO;
    private ReservationDAO reservationDAO;
    private CarDAO carDAO;
    private UserRoleDAO userRoleDAO;
    private TemplateDAO templateDAO;
    

    @Override
    public UserDAO getUserDAO() {
        if (userDAO == null) {
            userDAO = mock(JDBCUserDAO.class);
        }
        return userDAO;
    }

    @Override
    public InfoSessionDAO getInfoSessionDAO() {
        if(infoSessionDAO == null){
            infoSessionDAO = mock(JDBCInfoSessionDAO.class);
        }
        return infoSessionDAO;
    }

    @Override
    public TemplateDAO getTemplateDAO() {
        if(templateDAO == null){
            templateDAO = mock(JDBCTemplateDAO.class);
        }
        return templateDAO;
    }

    @Override
    public AddressDAO getAddressDAO() {
        if(addressDAO == null){
            addressDAO = mock(JDBCAddressDAO.class);
        }
        return addressDAO;
    }

    @Override
    public void begin() {
        //TODO What happens here?
    }

    @Override
    public void commit() {
    }

    @Override
    public void rollback() {

    }

    @Override
    public void close() {

    }

	@Override
	public CarDAO getCarDAO() {
		if(carDAO == null){
            carDAO = mock(JDBCCarDAO.class);
        }
        return carDAO;
	}

	@Override
	public ReservationDAO getReservationDAO() {
		if(reservationDAO == null){
            reservationDAO = mock(JDBCReservationDAO.class);
        }
        return reservationDAO;
	}

	@Override
	public UserRoleDAO getUserRoleDAO() {
		if(userRoleDAO == null){
			userRoleDAO = mock(JDBCUserRoleDAO.class);
        }
        return userRoleDAO;
	}
}
