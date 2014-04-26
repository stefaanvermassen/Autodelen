package database;

import models.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 26/04/14.
 */
public interface RefuelDAO {

    public Refuel createRefuel(CarRide carRide) throws DataAccessException;
    public void acceptRefuel(Refuel refuel) throws DataAccessException;
    public void rejectRefuel(Refuel refuel) throws DataAccessException;
    public void deleteRefuel(Refuel refuel) throws DataAccessException;
    public List<Refuel> getRefuelsForUser(int userId) throws DataAccessException;
}
