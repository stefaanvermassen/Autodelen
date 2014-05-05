package database;

import models.Damage;
import models.DamageLog;

import java.util.List;

/**
 * Created by stefaan on 04/05/14.
 */
public interface DamageLogDAO {

    public DamageLog createDamageLog(Damage damage, String description) throws DataAccessException;
    public List<DamageLog> getDamageLogsForDamage(int damageId) throws DataAccessException;
}
