package database.jdbc;

import database.DataAccessContext;
import database.DataAccessException;
import database.DataAccessProvider;
import play.db.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Cedric on 2/16/14.
 */
public class JDBCDataAccessProvider implements DataAccessProvider {

    // TODO: read from application.conf?
    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/autodelen";

    //  Database credentials
    static final String USER = "root";
    static final String PASS = "zelensis123";

    @Override
    public DataAccessContext getDataAccessContext() throws DataAccessException {
        Connection conn;
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

        } catch (ClassNotFoundException e) {
            throw new DataAccessException("Couldn't find jdbc driver", e);
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't connect to database", e);
        }

        return new JDBCDataAccessContext(conn);
    }

}
