package database.jdbc;

import database.DataAccessException;
import database.SchedulerDAO;
import models.Refuel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 27/04/14.
 */
public class JDBCSchedulerDAO implements SchedulerDAO {

    private Connection connection;
    private PreparedStatement getReminderEmailListStatement;

    public JDBCSchedulerDAO(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getGetReminderEmailListStatement() throws SQLException {
        if (getReminderEmailListStatement == null) {
            getReminderEmailListStatement = connection.prepareStatement("SELECT notification_email AS user_email " +
                    "FROM (SELECT a.notification_user, a.notification_email, a.notification_last_notified, " +
                    "a.number_of_notifications, COUNT(b.message_id) AS number_of_messages " +
                    "FROM (SELECT p.user_id AS notification_user, p.user_email " +
                    "AS notification_email, p.user_last_notified AS notification_last_notified, " +
                    "COUNT(o.notification_id) AS number_of_notifications " +
                    "FROM Users p LEFT JOIN Notifications o ON o.notification_user_id = p.user_id GROUP BY p.user_id) a " +
                    "LEFT JOIN MESSAGES b ON a.notification_user = b.message_from_user_id " +
                    "GROUP BY a.notification_user) AS notfs " +
                    "WHERE  (number_of_notifications > ? OR number_of_messages > ?) " +
                    "AND notification_last_notified < DATE_SUB(NOW(),INTERVAL 7 DAY)");
        }
        return getReminderEmailListStatement;
    }

    @Override
    public List<String> getReminderEmailList(int maxMessages) throws DataAccessException {
        try {
            PreparedStatement ps = getGetReminderEmailListStatement();
            ps.setInt(1, maxMessages);
            ps.setInt(2, maxMessages);
            return getEmailList(ps);
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the email list.", e);
        }
    }

    private List<String> getEmailList(PreparedStatement ps) throws DataAccessException {
        List<String> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("user_email"));
            }
            return list;
        }catch (SQLException e){
            throw new DataAccessException("Error while reading email resultset", e);

        }
    }
}
