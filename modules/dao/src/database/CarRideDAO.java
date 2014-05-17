package database;

import models.CarRide;
import models.Reservation;

import java.sql.Date;
import java.util.List;

/**
 * Created by HannesM on 10/03/14.
 */
public interface CarRideDAO {
    public CarRide createCarRide(Reservation reservation, int startMileage, int endMileage, boolean damaged, int refueling) throws DataAccessException;
    public CarRide getCarRide(int id) throws DataAccessException;
    public void updateCarRide(CarRide carRide) throws DataAccessException;
    public void endPeriod() throws DataAccessException;
    public List<CarRide> getBillRidesForLoaner(Date date, int user) throws DataAccessException;
    public List<CarRide> getBillRidesForCar(Date date, int car) throws DataAccessException;
}
