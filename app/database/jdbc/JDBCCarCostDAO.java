package database.jdbc;

import database.CarCostDAO;
import database.DataAccessException;
import models.*;
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
    private PreparedStatement updateCarCostStatement;
    private PreparedStatement getCarCostListForCarStatement;
    private PreparedStatement getRequestedCarCostListStatement;
    private PreparedStatement getCarCostStatement;

    public JDBCCarCostDAO(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getGetCarCostStatement() throws SQLException {
        if (getCarCostStatement == null) {
            getCarCostStatement = connection.prepareStatement("SELECT * FROM CarCosts JOIN Cars ON car_cost_car_id = car_id WHERE car_cost_id=?");
        }
        return getCarCostStatement;
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

    private PreparedStatement getGetRequestedCarCostListStatement() throws SQLException {
        if (getRequestedCarCostListStatement == null) {
            getRequestedCarCostListStatement = connection.prepareStatement("SELECT * FROM CarCosts " +
                    "JOIN Cars ON car_cost_car_id = car_id "+
                    "WHERE car_cost_status = 'REQUEST' ORDER BY car_cost_created_at DESC;");
        }
        return getRequestedCarCostListStatement;
    }

    private PreparedStatement getUpdateCarCostStatement() throws SQLException {
        if (updateCarCostStatement == null) {
            updateCarCostStatement = connection.prepareStatement("UPDATE CarCosts SET car_cost_amount = ? , car_cost_description = ? , car_cost_status = ? , car_cost_time = ? , car_cost_mileage = ?"
                    + " WHERE car_cost_id = ?");
        }
        return updateCarCostStatement;
    }

    public static CarCost populateCarCost(ResultSet rs, Car car) throws SQLException {
        CarCost carCost = new CarCost(rs.getInt("car_cost_id"), car, rs.getBigDecimal("car_cost_amount"), rs.getBigDecimal("car_cost_mileage"), rs.getString("car_cost_description"), new DateTime(rs.getTimestamp("car_cost_time")));
        carCost.setStatus(CarCostStatus.valueOf(rs.getString("car_cost_status")));
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
        try {
            PreparedStatement ps = getGetRequestedCarCostListStatement();
            return getRequestedCarCostList(ps);
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the list of carcosts", e);
        }
    }

    @Override
    public void updateCarCost(CarCost carCost) throws DataAccessException {
        try {
            PreparedStatement ps = getUpdateCarCostStatement();
            ps.setBigDecimal(1, carCost.getAmount());
            ps.setString(2, carCost.getDescription());
            ps.setString(3, carCost.getStatus().toString());
            ps.setTimestamp(4, new Timestamp(carCost.getTime().getMillis()));
            ps.setBigDecimal(5, carCost.getMileage());
            ps.setInt(6, carCost.getId());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("CarCost update affected 0 rows.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to update CarCost", e);
        }

    }

    @Override
    public CarCost getCarCost(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getGetCarCostStatement();
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return populateCarCost(rs, JDBCCarDAO.populateCar(rs, false, false));
                else return null;
            }catch (SQLException e){
                throw new DataAccessException("Error reading reservation resultset", e);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to get reservation", e);
        }
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

    private List<CarCost> getRequestedCarCostList(PreparedStatement ps) throws DataAccessException {
        List<CarCost> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(populateCarCost(rs, JDBCCarDAO.populateCar(rs, false, false)));
            }
            return list;
        }catch (SQLException e){
            throw new DataAccessException("Error while reading carcost resultset", e);

        }
    }
}
