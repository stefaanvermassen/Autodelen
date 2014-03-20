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


    public Filter<CarField> createCarListFilter();

    public Car createCar(String name, String brand, String type, Address location, int seats, int doors, int year, boolean gps, boolean hook, CarFuel fuel, int fuelEconomy, int estimatedValue, int ownerAnnualKm, User owner, String comments) throws DataAccessException;
    public void updateCar(Car car) throws DataAccessException;
    public Car getCar(int id) throws DataAccessException;
    public void deleteCar(Car car) throws DataAccessException;
    public int getAmountOfCars(Filter<CarField> filter) throws DataAccessException;
    public List<Car> getCarList() throws DataAccessException; // TODO: delete this method, use with pages
    public List<Car> getCarList(int page, int pageSize) throws DataAccessException;
    public List<Car> getCarList(CarField orderBy, boolean asc, int page, int pageSize, Filter<CarField> filter) throws DataAccessException;
    public List<Car> getCarsOfUser(int user_id) throws DataAccessException;
}
