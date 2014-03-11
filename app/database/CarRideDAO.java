package database;

import models.CarRide;
import models.Reservation;

/**
 * Created by HannesM on 10/03/14.
 */
public interface CarRideDAO {
    public CarRide createCarRide(Reservation reservation) throws DataAccessException;
    public CarRide getCarRide(int id) throws DataAccessException;
    public void updateCarRide(CarRide carRide) throws DataAccessException;
}
