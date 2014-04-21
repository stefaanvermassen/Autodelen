package database;

import java.util.Date;

/**
 * Created by Cedric on 4/21/2014.
 */
public interface SettingDAO {
    public String getSettingForDate(String name, Date date) throws DataAccessException;
    public void createSettingAfterDate(String name, String value, Date after);
}
