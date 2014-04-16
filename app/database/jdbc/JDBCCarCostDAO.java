package database.jdbc;

import database.CarCostDAO;
import database.DataAccessException;
import models.Car;
import models.CarCost;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 15/04/14.
 */
public class JDBCCarCostDAO implements CarCostDAO {

    private static final String[] AUTO_GENERATED_KEYS = {"carcost_id"};
    private Connection connection;
    private PreparedStatement createCarCostStatement;
    private PreparedStatement getCarCostListForCarStatement;

    public JDBCCarCostDAO(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getCreateCarCostStatement() throws SQLException {
        if (createCarCostStatement == null) {
            createCarCostStatement = connection.prepareStatement("INSERT INTO CarCosts (car_cost_car_id, car_cost_amount, " +
                    "car_cost_description, car_cost_time, car_cost_mileage) VALUES (?,?,?,?,?)", AUTO_GENERATED_KEYS);
        }
        return createCarCostStatement;
    }

    private PreparedStatement getGetCarCostListForCarStatement() throws SQLException {
        if (getCarCostListForCarStatement == null) {
            getCarCostListForCarStatement = connection.prepareStatement("SELECT * FROM CarCosts " +
                    "JOIN Cars ON car_cost_car_id = car_id "+
                    "WHERE car_cost_car_id=? ORDER BY car_cost_created_at DESC;");
        }
        return getCarCostListForCarStatement;
    }

    public static CarCost populateCarCost(ResultSet rs, Car car) throws SQLException {
        CarCost carCost = new CarCost(rs.getInt("car_cost_id"), car, rs.getBigDecimal("car_cost_amount"), rs.getBigDecimal("car_cost_mileage"), rs.getString("car_cost_description"), new DateTime(rs.getTimestamp("car_cost_time")));
        return carCost;
    }

    @Override
    public CarCost createCarCost(Car car, BigDecimal amount, BigDecimal mileage, String description, DateTime time) throws DataAccessException {
        try{
            PreparedStatement ps = getCreateCarCostStatement();
            ps.setInt(1, car.getId());
            ps.setBigDecimal(2, amount);
            ps.setString(3, description);
            ps.setTimestamp(4, new Timestamp(time.getMillis()));
            ps.setBigDecimal(5, mileage);

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating carcost.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new CarCost(keys.getInt(1), car, amount, mileage, description, time);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for carcost.", ex);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to create carcost", e);
        }
    }

    @Override
    public List<CarCost> getCarCostListForCar(Car car) throws DataAccessException {
        try {
            PreparedStatement ps = getGetCarCostListForCarStatement();
            ps.setInt(1, car.getId());
            return getCarCostList(ps, car);
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the list of carcosts", e);
        }
    }

    @Override
    public List<CarCost> getRequestedCarCostList() throws DataAccessException {
        return null;
    }

    @Override
    public void updateCarCost(CarCost carCost) throws DataAccessException {

    }

    private List<CarCost> getCarCostList(PreparedStatement ps, Car car) throws DataAccessException {
        List<CarCost> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(populateCarCost(rs, car));
            }
            return list;
        }catch (SQLException e){
            throw new DataAccessException("Error while reading carcost resultset", e);

        }
    }
}
