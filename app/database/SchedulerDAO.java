package database;

import java.util.List;

/**
 * Created by Stefaan Vermassen on 27/04/14.
 */
public interface SchedulerDAO {

    public List<String> getReminderEmailList(int maxMessages) throws DataAccessException;
}
