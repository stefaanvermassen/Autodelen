package database;

import models.Message;
import models.User;

import java.util.List;

/**
 * Created by stefaan on 22/03/14.
 */
public interface MessageDAO {

    public List<Message> getReceivedMessageListForUser(int userId) throws DataAccessException;
    public int getNumberOfUnreadMessages(int userId) throws DataAccessException;
    public Message createMessage(User sender, User receiver, String subject, String body) throws DataAccessException;
    public void markMessageAsRead(int messageID) throws DataAccessException;


}
