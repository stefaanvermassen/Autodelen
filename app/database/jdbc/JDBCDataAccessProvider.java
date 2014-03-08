package database.jdbc;

import database.DataAccessContext;
import database.DataAccessException;
import database.DataAccessProvider;
import database.DatabaseConfiguration;
import play.db.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Cedric on 2/16/14.
 */
public class JDBCDataAccessProvider implements DataAccessProvider {

    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    private DatabaseConfiguration configuration;

    public JDBCDataAccessProvider(DatabaseConfiguration configuration){
        this.configuration = configuration;
    }

    @Override
    public DataAccessContext getDataAccessContext() throws DataAccessException {
        Connection conn;
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(getDatabaseUrl(configuration), configuration.getUsername(), configuration.getPassword());
        } catch (ClassNotFoundException e) {
            throw new DataAccessException("Couldn't find jdbc driver", e);
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't connect to database", e);
        }

        return new JDBCDataAccessContext(conn);
    }

    private static String getDatabaseUrl(DatabaseConfiguration configuration){
        return String.format("jdbc:mysql://%s:%d/%s", configuration.getServer(), configuration.getPort(), configuration.getDatabase());
    }

}
