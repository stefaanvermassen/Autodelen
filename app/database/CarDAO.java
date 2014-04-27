/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import models.*;

import java.util.List;

/**
 *
 * @author Laurent
 */
public interface CarDAO {

    public Car createCar(String name, String brand, String type, Address location, Integer seats, Integer doors, Integer year, boolean gps, boolean hook,
                         CarFuel fuel, Integer fuelEconomy, Integer estimatedValue, Integer ownerAnnualKm,
                         TechnicalCarDetails technicalCarDetails, CarInsurance insurance, User owner, String comments) throws DataAccessException;
    public void updateCar(Car car) throws DataAccessException;
    public Car getCar(int id) throws DataAccessException;
    public void deleteCar(Car car) throws DataAccessException;

    public int getAmountOfCars(Filter filter) throws DataAccessException;
    public List<Car> getCarList(int page, int pageSize) throws DataAccessException;
    public List<Car> getCarList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;
    public List<Car> getCarsOfUser(int user_id) throws DataAccessException;
}
