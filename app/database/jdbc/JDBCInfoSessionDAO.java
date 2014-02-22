package database.jdbc;

import database.DataAccessException;
import database.InfoSessionDAO;
import models.Address;
import models.InfoSession;
import models.User;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cedric on 2/22/14.
 */
public class JDBCInfoSessionDAO implements InfoSessionDAO {

    private Connection connection;

    private static String INFOSESSION_SELECTOR = "SELECT infosession_id, infosession_timestamp, " +
            "address_id, address_city, address_zipcode, address_street, address_street_number, address_street_bus " +
            "user_id, user_password, user_firstname, user_lastname, user_phone, user_email " +
            "INNER JOIN users ON infosession_host_user_id = user_id INNER JOIN addresses ON infosession_address_id = address_id";

    private PreparedStatement createInfoSessionStatement;
    private PreparedStatement getInfoSessionsAfterStatement;
    private PreparedStatement getInfoSessionById;

    public JDBCInfoSessionDAO(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getCreateInfoSessionStatement() throws SQLException {
        if (createInfoSessionStatement == null) {
            createInfoSessionStatement = connection.prepareStatement("INSERT INTO infosessions(infosession_timestamp, infosession_address_id, infosession_host_user_id) VALUES (?,?,?)",
                    new String[]{"infosession_id"});
        }
        return createInfoSessionStatement;
    }

    private PreparedStatement getGetInfoSessionsAfterStatement() throws SQLException {
        if (getInfoSessionsAfterStatement == null) {
            getInfoSessionsAfterStatement = connection.prepareStatement(INFOSESSION_SELECTOR + " WHERE infosession_timestamp > ?");
        }
        return getInfoSessionsAfterStatement;
    }

    private PreparedStatement getGetInfoSessionById() throws SQLException {
        if (getInfoSessionById == null) {
            getInfoSessionById = connection.prepareStatement(INFOSESSION_SELECTOR + " WHERE infosession_id = ?");
        }
        return getInfoSessionById;
    }

    public static InfoSession populateInfoSession(ResultSet rs) throws SQLException {
        return new InfoSession(rs.getInt("infosession_id"), new DateTime(rs.getTime("infosession_timestamp").getTime()), JDBCAddressDAO.populateAddress(rs), JDBCUserDAO.populateUser(rs, false, false));
    }

    @Override
    public InfoSession createInfoSession(User host, Address address, DateTime time) throws DataAccessException {
        if (host.getId() == 0 || address.getId() == 0)
            throw new DataAccessException("Tried to create infosession without database user / database address");

        try {
            PreparedStatement ps = getCreateInfoSessionStatement();
            ps.setTimestamp(1, new Timestamp(time.getMillis())); //TODO: timezones?? convert to datetime see below
            ps.setInt(2, address.getId());
            ps.setInt(3, host.getId());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new InfoSession(keys.getInt(1), time, address, host, InfoSession.NO_ENROLLEES);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new infosession.", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch infosessions after date.", ex);
        }
    }

    @Override
    public InfoSession getInfoSession(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getGetInfoSessionById();
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return populateInfoSession(rs);
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading infosession resultset", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch infosession by id.", ex);
        }
    }

    @Override
    public boolean deleteInfoSession(int id) throws DataAccessException {
        throw new RuntimeException();
    }

    @Override
    public List<InfoSession> getInfoSessionsAfter(DateTime since) throws DataAccessException {
        List<InfoSession> sessions = new ArrayList<InfoSession>();
        try {
            PreparedStatement ps = getGetInfoSessionsAfterStatement();
            ps.setTimestamp(1, new Timestamp(since.getMillis())); //TODO: convert to datetime see above
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sessions.add(populateInfoSession(rs));
                }
                return sessions;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading user resultset", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch user by email.", ex);
        }
    }

    @Override
    public void updateInfoSession(InfoSession session) throws DataAccessException {
        throw new RuntimeException();
    }

    @Override
    public void registerUser(InfoSession session, User user) throws DataAccessException {
        throw new RuntimeException();
    }
}
