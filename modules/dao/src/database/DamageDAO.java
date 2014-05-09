package database;

import models.CarRide;
import models.Damage;
import models.FileGroup;

import java.util.List;

/**
 * Created by Stefaan Vermassen on 02/05/14.
 */
public interface DamageDAO {

    public Damage createDamage(CarRide carRide) throws DataAccessException;
    public Damage getDamage(int damageId) throws DataAccessException;
    public void updateDamage(Damage damage) throws DataAccessException;
    public void deleteDamage(int damageId);

    public int getAmountOfDamages(Filter filter) throws DataAccessException;
    public List<Damage> getDamages(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;
}

