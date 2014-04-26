package database.mocking;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.*;
import org.joda.time.DateTime;

import database.CarDAO;
import database.DataAccessException;
import database.Filter;
import database.FilterField;
import database.jdbc.JDBCFilter;

public class TestCarDAO implements CarDAO{
	
	private List<Car> cars;
	private int idCounter;
	
	
	public TestCarDAO(){
		cars = new ArrayList<>();
		idCounter=0;
	}

	@Override
	public void updateCar(Car car) throws DataAccessException {
		// ok		
	}

	@Override
	public Car getCar(int id) throws DataAccessException {
		for(Car car : cars){
			if(car.getId()==id){
				return new Car(car.getId(),car.getName(), car.getBrand(),car.getType(), car.getLocation(),car.getSeats(),
						car.getDoors(),car.getYear(),car.isGps(),car.isHook(),car.getFuel(),
						car.getFuelEconomy(),car.getEstimatedValue(),car.getOwnerAnnualKm(),
						car.getTechnicalCarDetails(), car.getOwner(),car.getComments());
				}
		}
		return null;
	}

	@Override
	public void deleteCar(Car car) throws DataAccessException {
		if(cars.contains(car)) cars.remove(car);
	}

	@Override
	public Car createCar(String name, String brand, String type,
			Address location, Integer seats, Integer doors, Integer year, boolean gps,
			boolean hook, CarFuel fuel, Integer fuelEconomy, Integer estimatedValue,
            Integer ownerAnnualKm, TechnicalCarDetails technicalCarDetails, User owner, String comments)
			throws DataAccessException {
		Car car = new Car(idCounter++,name, brand, type, location, seats, doors, year, gps, hook, fuel, fuelEconomy, estimatedValue, ownerAnnualKm, technicalCarDetails, owner, comments);
		cars.add(car);
		return car;
	}

	@Override
	public int getAmountOfCars(Filter filter) throws DataAccessException {
		return getCarList().size(); // TODO: implement Filter methods
	}

	public List<Car> getCarList() throws DataAccessException {
		return cars;
	}

	@Override
	public List<Car> getCarList(int page, int pageSize) throws DataAccessException {
		return cars.subList((page-1)*pageSize, page*pageSize > cars.size() ? cars.size() : page*pageSize );
	}

	@Override
	public List<Car> getCarList(FilterField orderBy, boolean asc, int page,	int pageSize, Filter filter) throws DataAccessException {
        return getCarList(page, pageSize); // TODO: implement Filter methods
	}

	@Override
	public List<Car> getCarsOfUser(int user_id) throws DataAccessException {
		List<Car> list = new ArrayList<>();
		for(Car car : cars){
			if(car.getOwner().getId()==user_id){
				list.add(car);
			}
		}
		return list;
	}

}
