package database.jdbc;

import database.DataAccessContext;
import database.UserDAO;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Cedric on 2/16/14.
 */
public class JDBCDataAccessContext implements DataAccessContext {

    private Connection connection;
    private UserDAO userDAO;

    public JDBCDataAccessContext(Connection connection) {
        this.connection = connection;
    }

    @Override
    public UserDAO getUserDAO() {
        if (userDAO == null) {
            userDAO = new JDBCUserDAO(connection);
        }
        return userDAO;
    }

    @Override
    public void begin() {

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
            connection.close();
        } catch(SQLException ex){
            //TODO ??
        }
    }
}
