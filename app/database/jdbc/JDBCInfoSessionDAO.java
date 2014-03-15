package database.jdbc;

import database.DataAccessException;
import database.InfoSessionDAO;
import models.*;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cedric on 2/22/14.
 */
public class JDBCInfoSessionDAO implements InfoSessionDAO {

    private Connection connection;

    private static String INFOSESSION_FIELDS = "infosession_id, infosession_type, infosession_timestamp, infosession_max_enrollees," +
            "address_id, address_country, address_city, address_zipcode, address_street, address_street_number, address_street_bus, " +
            "user_id, user_password, user_firstname, user_lastname, user_phone, user_email, user_status";

    private static String INFOSESSION_SELECTOR = "SELECT " + INFOSESSION_FIELDS + " FROM infosessions " +
            "JOIN users ON infosession_host_user_id = user_id " +
            "JOIN addresses ON infosession_address_id = address_id";

    private PreparedStatement deleteInfoSession;
    private PreparedStatement createInfoSessionStatement;
    private PreparedStatement getInfoSessionsAfterStatement;
    private PreparedStatement getInfoSessionById;
    private PreparedStatement getInfosessionForUser;
    private PreparedStatement registerUserForSession;
    private PreparedStatement unregisterUserForSession;
    private PreparedStatement getAttendeesForSession;
    private PreparedStatement setAddressForSession;
    private PreparedStatement setTimeForSession;
    private PreparedStatement setUserEnrollmentStatusForSession;

    public JDBCInfoSessionDAO(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getDeleteInfoSessionStatement() throws SQLException {
        if(deleteInfoSession == null){
            deleteInfoSession = connection.prepareStatement("DELETE FROM InfoSessions WHERE infosession_id = ?");
        }
        return deleteInfoSession;
    }

    private PreparedStatement getSetAddressForSession() throws SQLException {
        if(setAddressForSession == null){
            setAddressForSession = connection.prepareStatement("UPDATE infosessions SET infosession_address_id = ? WHERE infosession_id = ?");
        }
        return setAddressForSession;
    }

    private PreparedStatement getSetUserEnrollmentStatusForSession() throws SQLException {
        if(setUserEnrollmentStatusForSession == null){
            setUserEnrollmentStatusForSession = connection.prepareStatement("UPDATE infosessionenrollees SET enrollment_status = ? WHERE infosession_enrollee_id = ? AND infosession_id = ?");
        }
        return setUserEnrollmentStatusForSession;
    }

    private PreparedStatement getSetTimeForSession() throws SQLException {
        if(setTimeForSession == null){
            setTimeForSession = connection.prepareStatement("UPDATE infosessions SET infosession_timestamp = ? WHERE infosession_id = ?");
        }
        return setTimeForSession;
    }

    private PreparedStatement getGetInfoSessionForUserStatement() throws SQLException {
        if(getInfosessionForUser == null){
            getInfosessionForUser = connection.prepareStatement("SELECT " + INFOSESSION_FIELDS + " FROM infosessionenrollees " +
            "JOIN infosessions USING (infosession_id) " +
            "JOIN users ON infosession_host_user_id = user_id " +
            "JOIN addresses ON infosession_address_id = address_id WHERE infosession_enrollee_id = ? AND infosession_timestamp > ?");
        }
        return getInfosessionForUser;
    }

    public PreparedStatement getGetAttendeesForSession() throws SQLException {
        if(getAttendeesForSession == null){
            getAttendeesForSession = connection.prepareStatement("SELECT user_id, user_firstname, user_email, user_lastname, enrollment_status " +
                    "FROM infosessionenrollees INNER JOIN users ON user_id = infosession_enrollee_id WHERE infosession_id = ?");
        }
        return getAttendeesForSession;
    }

    private PreparedStatement getRegisterUserForSession() throws SQLException {
        if(registerUserForSession == null){
            registerUserForSession = connection.prepareStatement("INSERT INTO InfoSessionEnrollees(infosession_id, infosession_enrollee_id) VALUES (?,?)");
        }
        return registerUserForSession;
    }

    private PreparedStatement getUnregisterUserForSession() throws SQLException {
        if(unregisterUserForSession == null){
            unregisterUserForSession = connection.prepareStatement("DELETE FROM infosessionenrollees WHERE infosession_id = ? AND infosession_enrollee_id = ?");
        }
        return unregisterUserForSession;
    }

    private PreparedStatement getCreateInfoSessionStatement() throws SQLException {
        if (createInfoSessionStatement == null) {
            createInfoSessionStatement = connection.prepareStatement("INSERT INTO InfoSessions(infosession_type, infosession_timestamp, infosession_address_id, infosession_host_user_id, infosession_max_enrollees) VALUES (?,?,?,?,?)",
                    new String[]{"infosession_id"});
        }
        return createInfoSessionStatement;
    }

    private PreparedStatement getGetInfoSessionsAfterStatement() throws SQLException {
        if (getInfoSessionsAfterStatement == null) {
            getInfoSessionsAfterStatement = connection.prepareStatement(INFOSESSION_SELECTOR + " WHERE infosession_timestamp > ? ORDER BY infosession_timestamp ASC");
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
        return new InfoSession(rs.getInt("infosession_id"), InfoSessionType.valueOf(rs.getString("infosession_type")), new DateTime(rs.getTimestamp("infosession_timestamp")), JDBCAddressDAO.populateAddress(rs), JDBCUserDAO.populateUser(rs, false, false), rs.getInt("infosession_max_enrollees"));
    }

    @Override
    public InfoSession createInfoSession(InfoSessionType type, User host, Address address, DateTime time, int maxEnrollees) throws DataAccessException {
        if (host.getId() == 0 || address.getId() == 0)
            throw new DataAccessException("Tried to create infosession without database user / database address");

        try {
            PreparedStatement ps = getCreateInfoSessionStatement();
            ps.setString(1, type.name());
            ps.setTimestamp(2, new Timestamp(time.getMillis())); //TODO: timezones?? convert to datetime see below
            ps.setInt(3, address.getId());
            ps.setInt(4, host.getId());
            ps.setInt(5, maxEnrollees);

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating infosession.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new InfoSession(keys.getInt(1), type, time, address, host, InfoSession.NO_ENROLLEES, maxEnrollees);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new infosession.", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not create infosession.", ex);
        }
    }

    @Override
    public InfoSession getInfoSession(int id, boolean withAttendees) throws DataAccessException {
        try {
            PreparedStatement ps = getGetInfoSessionById();
            ps.setInt(1, id);
            InfoSession is;
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()){
                    is = populateInfoSession(rs);
                } else return null;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading infosession resultset", ex);
            }

            if(withAttendees)
            {
                PreparedStatement ps2 = getGetAttendeesForSession();
                ps2.setInt(1, id);
                try(ResultSet rs = ps2.executeQuery()){
                    while(rs.next()){
                        is.addEnrollee(new Enrollee(new User(rs.getInt("user_id"), rs.getString("user_email"), rs.getString("user_firstname"), rs.getString("user_lastname")),
                                Enum.valueOf(EnrollementStatus.class, rs.getString("enrollment_status"))));
                    }
                } catch(SQLException ex){
                    throw new DataAccessException("Failed to get attendees for infosession", ex);
                }
            }
            return is;
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch infosession by id.", ex);
        }
    }

    /**
     * Updates the time field only
     * @param id
     * @return
     * @throws DataAccessException
     */
    @Override
    public boolean deleteInfoSession(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getDeleteInfoSessionStatement();
            ps.setInt(1, id);
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when deleting infosession.");
        } catch (SQLException ex){
            throw new DataAccessException("Could not delete infosession",ex);
        }

        // Why do we have to return a boolean?
        return true;
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
                throw new DataAccessException("Error reading infosession resultset", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not infosessions.", ex);
        }
    }

    @Override
    public void updateInfosessionTime(InfoSession session) throws DataAccessException {
        if(session.getId() == 0)
            throw new DataAccessException("Cannot update time field on unsaved session.");

        try {
            PreparedStatement ps = getSetTimeForSession();
            ps.setTimestamp(1, new Timestamp(session.getTime().getMillis()));
            ps.setInt(2, session.getId());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when updating time on infosession.");

        } catch(SQLException ex) {
            throw new DataAccessException("Failed to update infosession timestamp.", ex);
        }
    }

    @Override
    public void updateInfoSessionAddress(InfoSession session) throws DataAccessException {
        Address address = session.getAddress();
        if(address == null || session.getId() == 0 || address.getId() == 0)
            throw new DataAccessException("Failed to update session. Address or session doesn't exist in database.");

        try {
            PreparedStatement ps = getSetAddressForSession();
            ps.setInt(1, address.getId());
            ps.setInt(2, session.getId());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Address update for InfoSession did not affect any row.");

        } catch(SQLException ex){
            throw new DataAccessException("Failed to update address for infosession.", ex);
        }
    }

    @Override
    public void registerUser(InfoSession session, User user) throws DataAccessException {
       try {
           PreparedStatement ps = getRegisterUserForSession();
           ps.setInt(1, session.getId());
           ps.setInt(2, user.getId());
           if(ps.executeUpdate() == 0)
               throw new DataAccessException("Failed to register user to infosession. 0 rows affected.");
           session.addEnrollee(new Enrollee(user, EnrollementStatus.ENROLLED));

       } catch(SQLException ex) {
            throw new DataAccessException("Failed to prepare statement for user registration with infosession.", ex);
       }
    }

    @Override
    public void setUserEnrollmentStatus(InfoSession session, User user, EnrollementStatus status) throws DataAccessException {
        if(session.getId() == 0 || user.getId() == 0)
            throw new DataAccessException("Cannot update enrollmentstatus for unsaved session or user.");
        try {
            PreparedStatement ps = getSetUserEnrollmentStatusForSession();
            ps.setString(1, status.name());
            ps.setInt(2, user.getId());
            ps.setInt(3, session.getId());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Failed to update enrollment status. Affected rows = 0");

        } catch(SQLException ex){
            throw new DataAccessException("Failed to update enrollment status.", ex);
        }
    }

    @Override
    public void unregisterUser(InfoSession session, User user) throws DataAccessException {
        unregisterUser(session.getId(), user.getId());
    }

    @Override
    public void unregisterUser(int infoSessionId, int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getUnregisterUserForSession();
            ps.setInt(1,infoSessionId);
            ps.setInt(2, userId);
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Failed to unregister user from infosession.");

        } catch(SQLException ex){
            throw new DataAccessException("Invalid unregister query for infosession.", ex);
        }
    }

    @Override
    public InfoSession getAttendingInfoSession(User user) throws DataAccessException {
        try {
            PreparedStatement ps = getGetInfoSessionForUserStatement();
            ps.setInt(1, user.getId());
            ps.setTimestamp(2, new Timestamp(DateTime.now().getMillis())); //TODO: pass date as argument instead of 'now' ??

            try(ResultSet rs = ps.executeQuery()) {
                if(!rs.next())
                    return null;
                else return populateInfoSession(rs);
            } catch(SQLException ex){
                throw new DataAccessException("Invalid query for attending infosession.", ex);
            }
        } catch(SQLException ex){
            throw new DataAccessException("Failed to fetch infosession for user", ex);
        }
    }
}
