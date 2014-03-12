/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import models.Car;
import models.CarFuel;
import models.Address;
import models.User;

import java.util.List;

/**
 *
 * @author Laurent
 */
public interface CarDAO {
    
    public Car createCar(String name, String brand, String type, Address location, int seats, int doors, int year, boolean gps, boolean hook, CarFuel fuel, int fuelEconomy, int estimatedValue, int ownerAnnualKm, User owner, String comments) throws DataAccessException;
    public void updateCar(Car car) throws DataAccessException;
    public Car getCar(int id) throws DataAccessException;
    public void deleteCar(Car car) throws DataAccessException;
    public List<Car> getCarList() throws DataAccessException;
    public List<Car> getCarsOfUser(int user_id) throws DataAccessException;
}
