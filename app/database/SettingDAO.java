package database;

import models.Setting;

import java.util.Date;
import java.util.List;

/**
 * Created by Cedric on 4/21/2014.
 */
public interface SettingDAO {
    public String getSettingForDate(String name, Date date) throws DataAccessException;
    public void createSettingAfterDate(String name, String value, Date after);
    public List<Setting> getSettings() throws DataAccessException;
}
