package database.jdbc;

import database.*;
import models.Notification;
import models.User;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 15/03/14.
 */
public class JDBCNotificationDAO implements NotificationDAO{

    private static final String[] AUTO_GENERATED_KEYS = {"notification_id"};

    private Connection connection;
    private PreparedStatement createNotificationStatement;
    private PreparedStatement getNotificationListByUseridStatement;
    private PreparedStatement getNumberOfUnreadNotificationsStatement;
    private PreparedStatement getNotificationListPageByUseridDescStatement;
    private PreparedStatement getGetAmountOfNotificationsStatement;

    public static final String NOTIFICATION_QUERY = "SELECT * FROM Notifications JOIN Users ON " +
            "notification_user_id= user_id";

    public static final String FILTER_FRAGMENT = " WHERE notification_user_id=? ";

    private void fillFragment(PreparedStatement ps, Filter filter, int start) throws SQLException {
        if(filter == null) {
            // getFieldContains on a "empty" filter will return the default string "%%", so this does not filter anything
            filter = new JDBCFilter();
        }
        ps.setString(start, filter.getFieldContains(FilterField.USER_ID, true));
    }

    public JDBCNotificationDAO(Connection connection) {
        this.connection = connection;
    }

    public static Notification populateNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification(rs.getInt("notification_id"), JDBCUserDAO.populateUser(rs, false, false),
                rs.getBoolean("notification_read"), rs.getString("notification_subject"), rs.getString("notification_body"),
                new DateTime(rs.getTimestamp("notification_timestamp")));
        return notification;
    }

    private PreparedStatement getCreateNotificationStatement() throws SQLException {
        if (createNotificationStatement == null) {
            createNotificationStatement = connection.prepareStatement("INSERT INTO Notifications (notification_user_id, " +
                    "notification_read, notification_subject,"
                    + "notification_body, notification_timestamp) VALUES (?,?,?,?,?)", AUTO_GENERATED_KEYS);
        }
        return createNotificationStatement;
    }

    private PreparedStatement getGetNotificationListByUseridStatement() throws SQLException {
        if (getNotificationListByUseridStatement == null) {
            getNotificationListByUseridStatement = connection.prepareStatement("SELECT * FROM Notifications JOIN Users ON " +
                    "notification_user_id= user_id WHERE notification_user_id=? ORDER BY notification_timestamp DESC;");
        }
        return getNotificationListByUseridStatement;
    }

    private PreparedStatement getNotificationListPageByUseridDescStatement() throws SQLException {
        if(getNotificationListPageByUseridDescStatement == null) {
            getNotificationListPageByUseridDescStatement = connection.prepareStatement(NOTIFICATION_QUERY + FILTER_FRAGMENT +" ORDER BY notification_timestamp desc LIMIT ?, ?");
        }
        return getNotificationListPageByUseridDescStatement;
    }

    private PreparedStatement getNumberOfUnreadNotificationsStatement() throws SQLException {
        if (getNumberOfUnreadNotificationsStatement == null) {
            getNumberOfUnreadNotificationsStatement = connection.prepareStatement("SELECT COUNT(*) AS unread_number FROM Notifications JOIN Users ON " +
                    "notification_user_id= user_id WHERE notification_user_id=? AND notification_read=0;");
        }
        return getNumberOfUnreadNotificationsStatement;
    }

    private PreparedStatement getGetAmountOfNotificationsStatement() throws SQLException {
        if(getGetAmountOfNotificationsStatement == null) {
            getGetAmountOfNotificationsStatement = connection.prepareStatement("SELECT count(*) as amount_of_notifications FROM Notifications JOIN Users ON " +
                    "notification_user_id= user_id" + FILTER_FRAGMENT);
        }
        return getGetAmountOfNotificationsStatement;
    }


    @Override
    public int getAmountOfNotifications(Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = getGetAmountOfNotificationsStatement();
            fillFragment(ps, filter, 1);

            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return rs.getInt("amount_of_notifications");
                else return 0;

            } catch (SQLException ex) {
                throw new DataAccessException("Error reading count of notifications", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of notifications", ex);
        }
    }

    @Override
    public List<Notification> getNotificationListForUser(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getGetNotificationListByUseridStatement();
            ps.setInt(1, userId);
            return getNotificationList(ps);
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the list of notifications", e);
        }
    }

    @Override
    public List<Notification> getNotificationList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = getNotificationListPageByUseridDescStatement();

            fillFragment(ps, filter, 1);
            int first = (page-1)*pageSize;
            ps.setInt(2, first);
            ps.setInt(3, pageSize);
            return getNotificationList(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of notifications", ex);
        }
    }

    @Override
    public int getNumberOfUnreadNotifications(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getNumberOfUnreadNotificationsStatement();
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("unread_number");
                }else{
                    return 0;
                }
            }catch (SQLException e){
                throw new DataAccessException("Error while reading notification number resultset", e);

            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the number of unread notifications", e);
        }
    }

    @Override
    public Notification createNotification(User user, String subject, String body, DateTime timestamp) throws DataAccessException {
        try{
            PreparedStatement ps = getCreateNotificationStatement();
            ps.setInt(1, user.getId());
            ps.setBoolean(2, false);
            ps.setString(3, subject);
            ps.setString(4,body);
            ps.setTimestamp(5, new Timestamp(timestamp.getMillis()));

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating notification.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                DatabaseHelper.getCommunicationProvider().invalidateNotifications(user.getId());
                DatabaseHelper.getCommunicationProvider().invalidateNotificationNumber(user.getId());
                return new Notification(keys.getInt(1), user, false, subject, body, timestamp);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new notification.", ex);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to create notification", e);
        }
    }


    private List<Notification> getNotificationList(PreparedStatement ps) throws DataAccessException {
        List<Notification> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(populateNotification(rs));
            }
            return list;
        }catch (SQLException e){
            throw new DataAccessException("Error while reading notification resultset", e);

        }
    }
}
