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
                         TechnicalCarDetails technicalCarDetails, CarInsurance insurance, User owner, String comments, boolean active) throws DataAccessException;
    public void updateCar(Car car) throws DataAccessException;
    public Car getCar(int id) throws DataAccessException;
    public void deleteCar(Car car) throws DataAccessException;

    public List<CarAvailabilityInterval> getAvailabilities(Car car) throws DataAccessException;
    public void addOrUpdateAvailabilities(Car car, List<CarAvailabilityInterval> availabilities) throws DataAccessException;
    public void deleteAvailabilties(List<CarAvailabilityInterval> availabilities) throws DataAccessException;

    public List<User> getPriviliged(Car car) throws DataAccessException;
    public void addPriviliged(Car car, List<User> users) throws DataAccessException;
    public void deletePriviliged(Car car, List<User> users) throws DataAccessException;

    public int getAmountOfCars(Filter filter) throws DataAccessException;
    public List<Car> getCarList(int page, int pageSize) throws DataAccessException;
    public List<Car> getCarList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;
    public List<Car> getCarsOfUser(int user_id) throws DataAccessException;
}
