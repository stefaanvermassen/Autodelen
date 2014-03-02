package database.jdbc;

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
    private TemplateDAO templateDAO;

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
    public TemplateDAO getTemplateDao() {
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
}
