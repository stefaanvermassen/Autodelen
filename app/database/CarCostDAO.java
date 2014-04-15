package database;

import models.*;
import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * Created by Stefaan Vermassen on 15/04/14.
 */
public interface CarCostDAO {

    public CarCost createCarCost(Car car, int amount, BigDecimal mileage, String description, DateTime time) throws DataAccessException;
}
