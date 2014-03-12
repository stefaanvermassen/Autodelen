package database.jdbc;


import database.AddressDAO;
import database.CarDAO;
import database.DataAccessContext;
import database.InfoSessionDAO;
import database.ReservationDAO;
import database.UserDAO;
import database.UserRoleDAO;
import database.*;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Cedric on 2/16/14.
 */
public class JDBCDataAccessContext implements DataAccessContext {

    private Connection connection;
    private UserDAO userDAO;
    private InfoSessionDAO infoSessionDAO;
    private AddressDAO addressDAO;
    private ReservationDAO reservationDAO;
    private CarDAO carDAO;
    private UserRoleDAO userRoleDAO;
    private TemplateDAO templateDAO;
    private CarRideDAO carRideDAO;
    
    public JDBCDataAccessContext(Connection connection) {
        this.connection = connection;
        try {
            this.connection.setAutoCommit(false); //we don't want to commit ourselves
        }catch(SQLException ex){
            //TODO: log?
        }
    }

    @Override
    public UserDAO getUserDAO() {
        if (userDAO == null) {
            userDAO = new JDBCUserDAO(connection);
        }
        return userDAO;
    }

    @Override
    public InfoSessionDAO getInfoSessionDAO() {
        if(infoSessionDAO == null){
            infoSessionDAO = new JDBCInfoSessionDAO(connection);
        }
        return infoSessionDAO;
    }

    @Override
    public TemplateDAO getTemplateDAO() {
        if(templateDAO == null){
            templateDAO = new JDBCTemplateDAO(connection);
        }
        return templateDAO;
    }

    @Override
    public AddressDAO getAddressDAO() {
        if(addressDAO == null){
            addressDAO = new JDBCAddressDAO(connection);
        }
        return addressDAO;
    }

    @Override
    public void begin() {
        //TODO What happens here?
    }

    @Override
    public void commit() {
        try {
            connection.commit();
        } catch (SQLException ex) {
            //TODO ??
        }
    }

    @Override
    public void rollback() {
        try {
            connection.rollback();
        } catch(SQLException ex){
            //TODO ??
        }
    }

    @Override
    public void close() {
        try {
            connection.commit(); //finalize commit if necessary
            connection.close();
        } catch(SQLException ex){
            //TODO ??
        }
    }

	@Override
	public CarDAO getCarDAO() {
		if(carDAO == null){
            carDAO = new JDBCCarDAO(connection);
        }
        return carDAO;
	}

	@Override
	public ReservationDAO getReservationDAO() {
		if(reservationDAO == null){
            reservationDAO = new JDBCReservationDAO(connection);
        }
        return reservationDAO;
	}

	@Override
	public UserRoleDAO getUserRoleDAO() {
		if(userRoleDAO == null){
			userRoleDAO = new JDBCUserRoleDAO(connection);
        }
        return userRoleDAO;
	}

    @Override
    public CarRideDAO getCarRideDAO() {
        if(carRideDAO == null){
            carRideDAO = new JDBCCarRideDAO(connection);
        }
        return carRideDAO;
    }
}
