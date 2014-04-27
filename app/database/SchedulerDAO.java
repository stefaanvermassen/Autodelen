package database;

import models.User;

import java.util.List;

/**
 * Created by Stefaan Vermassen on 27/04/14.
 */
public interface SchedulerDAO {

    public List<User> getReminderEmailList(int maxMessages) throws DataAccessException;
}
