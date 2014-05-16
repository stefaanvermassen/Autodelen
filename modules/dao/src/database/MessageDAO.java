package database;

import models.Message;
import models.User;
import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by Stefaan Vermassen on 22/03/14.
 */
public interface MessageDAO {

    public int getAmountOfMessages(Filter filter) throws DataAccessException;
    public List<Message> getMessageList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;
    public int getNumberOfUnreadMessages(int userId) throws DataAccessException;
    public Message createMessage(User sender, User receiver, String subject, String body) throws DataAccessException;
    public void markMessageAsRead(int messageID) throws DataAccessException;
    public void markAllMessagesAsRead(int userId) throws DataAccessException;


}
