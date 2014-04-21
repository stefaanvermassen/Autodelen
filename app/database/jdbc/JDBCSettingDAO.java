package database.jdbc;

import database.DataAccessException;
import database.SettingDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Cedric on 4/21/2014.
 */
public class JDBCSettingDAO implements SettingDAO{

    private Connection connection;
    private PreparedStatement getSettingForDateStatement;
    private PreparedStatement createSettingAfterStatement;

    public JDBCSettingDAO(Connection connection){
        this.connection = connection;
    }

    private PreparedStatement getGetSettingForDateStatement() throws SQLException {
        if(getSettingForDateStatement == null){
            getSettingForDateStatement = connection.prepareStatement("SELECT setting_value FROM settings WHERE setting_name=? AND (setting_after < ? OR setting_after IS NULL) ORDER BY setting_after DESC LIMIT 1");
        }
        return getSettingForDateStatement;
    }

    private PreparedStatement getCreateSettingAfterStatement() throws SQLException {
        if(createSettingAfterStatement == null){
            createSettingAfterStatement = connection.prepareStatement("INSERT INTO settings(setting_name, setting_value, setting_after) VALUES(?, ?, ?)");
        }
        return createSettingAfterStatement;
    }

    @Override
    public String getSettingForDate(String name, Date date) throws DataAccessException {
        try {
            PreparedStatement ps = getGetSettingForDateStatement();
            ps.setString(1, name);
            ps.setDate(2, new java.sql.Date(date.getTime()));

            try(ResultSet rs = ps.executeQuery()) {
                if(!rs.next())
                    return null;
                else
                    return rs.getString("setting_value");
            } catch(SQLException ex){
                throw new DataAccessException("Failed to get setting from resultset.", ex);
            }

        } catch(SQLException ex){
            throw new DataAccessException("Failed to get setting.", ex);
        }
    }

    @Override
    public void createSettingAfterDate(String name, String value, Date after) {
        try {
            PreparedStatement ps = getCreateSettingAfterStatement();
            ps.setString(1, name);
            ps.setString(2, value);
            ps.setDate(3, new java.sql.Date(after.getTime()));

            if(ps.executeUpdate() != 1)
                throw new DataAccessException("Failed to create setting. Rows inserted != 1");

        } catch(SQLException ex){
            throw new DataAccessException("Failed to prepare create setting statement.", ex);
        }
    }
}
