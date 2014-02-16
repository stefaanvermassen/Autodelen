package database.jdbc;

import java.io.FileInputStream;
import java.sql.*;  // for standard JDBC programs
import java.util.Properties;

import database.DataAccessContext;
import database.DataAccessException;
import database.DataAccessProvider;

/**
 * Created by Cedric on 2/16/14.
 */
public class JDBCDataAccessProvider implements DataAccessProvider {

    private Properties databaseProperties;

    public JDBCDataAccessProvider(String propertiesPath){
        try(FileInputStream file = new FileInputStream(propertiesPath)) {
            databaseProperties = new Properties();
            databaseProperties.load(file);
        } catch(Exception ex){
            databaseProperties = null;
        }
    }

    @Override
    public DataAccessContext getDataAccessContext() throws DataAccessException {
        try {
            return new JDBCDataAccessContext(getConnection());
        } catch (SQLException ex) {
            throw new DataAccessException("Could not create DAC", ex);
        }
    }

    private Connection getConnection() throws SQLException {
        String user = databaseProperties.getProperty("user");
        if (user != null) {
            return DriverManager.getConnection(databaseProperties.getProperty("url"), user, databaseProperties.getProperty("password"));
        } else {
            return DriverManager.getConnection(databaseProperties.getProperty("url"));
        }
    }

}
