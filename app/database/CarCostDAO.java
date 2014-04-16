package database;

import models.*;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 15/04/14.
 */
public interface CarCostDAO {

    public CarCost createCarCost(Car car, BigDecimal amount, BigDecimal mileage, String description, DateTime time) throws DataAccessException;
    public List<CarCost> getCarCostListForCar(Car car) throws DataAccessException;
    public List<CarCost> getRequestedCarCostList() throws DataAccessException;
    public void updateCarCost(CarCost carCost) throws DataAccessException;
}
