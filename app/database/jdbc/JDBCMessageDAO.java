package database.jdbc;

import database.DataAccessException;
import database.MessageDAO;
import models.Message;
import models.User;
import org.joda.time.DateTime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stefaan on 22/03/14.
 */
public class JDBCMessageDAO implements MessageDAO {

    private static final String[] AUTO_GENERATED_KEYS = {"message_id"};

    private Connection connection;
    private PreparedStatement createMessageStatement;
    private PreparedStatement getReceivedMessageListByUseridStatement;
    private PreparedStatement getSentMessageListByUseridStatement;
    private PreparedStatement getNumberOfUnreadMessagesStatement;

    public JDBCMessageDAO(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getCreateMessageStatement() throws SQLException {
        if (createMessageStatement == null) {
            createMessageStatement = connection.prepareStatement("INSERT INTO Messages (message_from_user_id, message_to_user_id, " +
                    "message_read, message_subject, message_body, message_timestamp) VALUES (?,?,?,?,?,?)", AUTO_GENERATED_KEYS);
        }
        return createMessageStatement;
    }

    private PreparedStatement getGetReceivedMessageListByUseridStatement() throws SQLException {
        if (getReceivedMessageListByUseridStatement == null) {
            getReceivedMessageListByUseridStatement = connection.prepareStatement("SELECT * FROM Messages " +
                    "JOIN Users AS Sender ON message_from_user_id = Sender.user_id " +
                    "JOIN Users AS Receiver ON message_to_user_id = Receiver.user_id " +
                    "WHERE message_to_user_id=? ORDER BY message_timestamp DESC;");
        }
        return getReceivedMessageListByUseridStatement;
    }

    private PreparedStatement getGetSentMessageListByUseridStatement() throws SQLException {
        if (getSentMessageListByUseridStatement == null) {
            getSentMessageListByUseridStatement = connection.prepareStatement("SELECT * FROM Messages " +
                    "JOIN Users AS Sender ON message_from_user_id = Sender.user_id " +
                    "JOIN Users AS Receiver ON message_to_user_id = Receiver.user_id " +
                    "WHERE message_from_user_id=? ORDER BY message_timestamp DESC;");
        }
        return getSentMessageListByUseridStatement;
    }

    private PreparedStatement getNumberOfUnreadMessagesStatement() throws SQLException {
        if (getNumberOfUnreadMessagesStatement == null) {
            getNumberOfUnreadMessagesStatement = connection.prepareStatement("SELECT COUNT(*) AS unread_number FROM Messages " +
                    "WHERE message_to_user_id=? AND message_read=0;");
        }
        return getNumberOfUnreadMessagesStatement;
    }

    public static Message populateMessage(ResultSet rs) throws SQLException {
        Message message = new Message(rs.getInt("message_id"), JDBCUserDAO.populateUser(rs, false, false, "Sender"),
                JDBCUserDAO.populateUser(rs, false, false, "Receiver"), rs.getBoolean("message_read"),
                rs.getString("message_subject"), rs.getString("message_body"),
                new DateTime(rs.getTimestamp("message_timestamp")));
        return message;
    }

    @Override
    public List<Message> getReceivedMessageListForUser(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getGetReceivedMessageListByUseridStatement();
            ps.setInt(1, userId);
            return getMessageList(ps);
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the list of messages", e);
        }
    }

    @Override
    public int getNumberOfUnreadMessages(int userId) throws DataAccessException {
        return 0;
    }

    @Override
    public Message createMessage(User sender, User receiver, String subject, String body) throws DataAccessException {
        return null;
    }

    @Override
    public void markMessageAsRead(int messageID) throws DataAccessException {

    }

    private List<Message> getMessageList(PreparedStatement ps) throws DataAccessException {
        List<Message> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(populateMessage(rs));
            }
            return list;
        }catch (SQLException e){
            throw new DataAccessException("Error while reading message resultset", e);

        }
    }
}
