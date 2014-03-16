package database.jdbc;

import database.DataAccessException;
import database.NotificationDAO;
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
                    "notification_user_id= user_id WHERE notification_user_id=?;");
        }
        return getNotificationListByUseridStatement;
    }


    @Override
    public List<Notification> getNotificationListForUser(int userId) throws DataAccessException {
        try {
            List<Notification> list = new ArrayList<>();
            PreparedStatement ps = getGetNotificationListByUseridStatement();
            ps.setInt(1, userId);
            return getNotificationList(ps);
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the list of notifications", e);
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
