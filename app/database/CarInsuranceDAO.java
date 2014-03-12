package database;

import java.util.Date;
import java.util.List;

import models.Car;
import models.CarInsurance;

public interface CarInsuranceDAO {
	public CarInsurance createCarInsurance(Date expiration, int bonus_malus, int polisNr, Car car) throws DataAccessException;
	public void updateCarInsurance(CarInsurance insurance) throws DataAccessException;
	public void deleteCarInsurance(CarInsurance insurance) throws DataAccessException;
	public List<CarInsurance> getAllCarInsurances(Car car) throws DataAccessException;
	public void deleteAllCarInsurances(Car car) throws DataAccessException;
}
