package database.providers;

import database.DataAccessContext;
import database.DataAccessException;
import database.DataAccessProvider;
import database.SettingDAO;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by Cedric on 4/21/2014.
 */
public class SettingProvider {

    //TODO: provide caching for all overview - WARNING - dates may vary on request!
    private static final String SETTING_KEY = "setting:%s";
    private static final DateFormat DATETIMEFORMATTER = DateFormat.getDateTimeInstance();

    private DataAccessProvider provider;

    public SettingProvider(DataAccessProvider provider) {
        this.provider = provider;
    }

    public void createSetting(String name, String value, Date afterDate) {
        try (DataAccessContext context = provider.getDataAccessContext()) {
            SettingDAO dao = context.getSettingDAO();
            dao.createSettingAfterDate(name, value, afterDate);
        }
    }

    public void createSetting(String name, int value, Date afterDate) {
        createSetting(name, Integer.toString(value), afterDate);
    }

    public void createSetting(String name, double value, Date afterDate) {
        createSetting(name, Double.toString(value), afterDate);
    }

    public void createSetting(String name, Date value, Date afterDate) {
        createSetting(name, DATETIMEFORMATTER.format(value), afterDate);
    }

    public String getString(String name, Date forDate) {
        try (DataAccessContext context = provider.getDataAccessContext()) {
            SettingDAO dao = context.getSettingDAO();
            return dao.getSettingForDate(name, forDate);
        }
    }

    public int getInt(String name, Date forDate) {
        return Integer.valueOf(getString(name, forDate));
    }

    public double getDouble(String name, Date forDate) {
        return Double.valueOf(getString(name, forDate));
    }

    public Date getDate(String name, Date forDate) {
        try {
            return DATETIMEFORMATTER.parse(getString(name, forDate));
        } catch (ParseException e) {
            throw new RuntimeException(e); //uncheck
        }
    }

    /**
     * Gets setting based on current timestamp
     *
     * @param name
     * @return
     */
    public String getString(String name) {
        return getString(name, new Date());
    }

    /**
     * Gets setting based on current timestamp
     *
     * @param name
     * @return
     */
    public int getInt(String name) {
        return getInt(name, new Date());
    }

    /**
     * Gets setting based on current timestamp
     *
     * @param name
     * @return
     */
    public double getDouble(String name) {
        return getDouble(name, new Date());
    }

    /**
     * Gets setting based on current timestamp
     *
     * @param name
     * @return
     */
    public Date getDate(String name) {
        return getDate(name, new Date());
    }

}
