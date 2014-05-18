package database;

import models.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 26/04/14.
 */
public interface RefuelDAO {

    public Refuel createRefuel(CarRide carRide) throws DataAccessException;
    public void acceptRefuel(int refuelId) throws DataAccessException;
    public void rejectRefuel(int refuelId) throws DataAccessException;
    public void deleteRefuel(int refuelId) throws DataAccessException;
    public Refuel getRefuel(int refuelId) throws DataAccessException;
    public void updateRefuel(Refuel refuel) throws DataAccessException;

    public List<Refuel> getRefuels(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;
    public int getAmountOfRefuels(Filter filter) throws DataAccessException;
    public List<Refuel> getRefuelsForUser(int userId) throws DataAccessException;
    public List<Refuel> getRefuelsForOwner(int userId) throws DataAccessException;
    public int getAmountOfRefuelsWithStatus(RefuelStatus status, int userId) throws DataAccessException;
}
