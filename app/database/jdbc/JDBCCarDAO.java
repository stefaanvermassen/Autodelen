/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database.jdbc;

import database.CarDAO;
import database.DataAccessException;
import database.providers.UserProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import models.Address;
import models.Car;
import models.CarFuel;
import models.User;

/**
 *
 * @author Laurent
 */
public class JDBCCarDAO implements CarDAO{
    
    private static final String[] AUTO_GENERATED_KEYS = {"car_id"};

    private Connection connection;
    private PreparedStatement createCarStatement;
    private PreparedStatement updateCarStatement;
    private PreparedStatement getCarStatement;

    public JDBCCarDAO(Connection connection) {
        this.connection = connection;
    }

    public static Car populateCar(ResultSet rs, boolean withAddress, boolean withUser) throws SQLException {
        Car car = new Car();
        car.setId(rs.getInt("car_id"));
        car.setBrand(rs.getString("car_brand"));
        car.setType(rs.getString("car_brand"));
        car.setComments(rs.getString("car_comments"));
        car.setDoors(rs.getInt("car_doors"));
        car.setEstimatedValue(rs.getInt("car_estimated_value"));
        car.setFuelEconomy(rs.getInt("car_fuel_economy"));
        car.setGps(rs.getBoolean("car_gps"));
        car.setHook(rs.getBoolean("car_hook"));
        car.setOwnerAnnualKm(rs.getInt("car_owner_annual_km"));
        car.setSeats(rs.getInt("car_seats"));
        car.setYear(rs.getInt("car_year"));
        Address location;
        if(withAddress) {
            location = JDBCAddressDAO.populateAddress(rs);
        } else {
            location = null;
        }
        car.setLocation(location);

        User user;
        if(withAddress) {
            UserProvider up = new UserProvider(new JDBCDataAccessProvider());
            user = up.getUser(rs.getString("user_id"));
        } else {
            user = null;
        }
        car.setOwner(user);

        car.setFuel(CarFuel.valueOf(rs.getString("car_fuel")));
        return car;
    }

    private PreparedStatement createCarStatement() throws SQLException {
        if (createCarStatement == null) {
            createCarStatement = connection.prepareStatement("INSERT INTO Cars(car_type, car_brand, car_location, car_seats, car_doors, car_year, car_gps, car_hook, car_fuel, car_fuel_economy, car_estimated_value, car_owner_annual_km, car_owner_user_id, car_comments, car_last_edit) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        }
        return createCarStatement;
    }
    
    private PreparedStatement updateCarStatement() throws SQLException {
        if (createCarStatement == null) {
            createCarStatement = connection.prepareStatement("UPDATE Cars SET car_type=? , car_brand=? , car_location=? , car_seats=? , car_doors=? , car_year=? , car_gps=? , car_hook=? , car_fuel=? , car_fuel_economy=? , car_estimated_value=? , car_owner_annual_km=?, car_owner_user_id=? , car_comments=? , car_last_edit=? ");
        }
        return createCarStatement;
    }
    
    private PreparedStatement getCarStatement() throws SQLException {
        if (getCarStatement == null) {
            getCarStatement = connection.prepareStatement("SELECT * FROM Cars INNER JOIN Addresses ON Addresses.car_location=Cars.address_id INNER JOIN Users ON Users.user_id=Cars.car_owner_user_id WHERE car_id=?");
        }
        return getCarStatement;
    }
    
    @Override
    public Car createCar(String brand, String type, Address location, int seats, int doors, int year, boolean gps, boolean hook, CarFuel fuel, int fuelEconomy, int estimatedValue, int ownerAnnualKm, User owner, String comments) throws DataAccessException {
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = createCarStatement()) {
                ps.setString(1, type);
                ps.setString(2, brand);
                ps.setInt(3, location.getId());
                ps.setInt(4, seats);
                ps.setInt(5, doors);
                ps.setInt(6, year);
                ps.setBoolean(7, gps);
                ps.setBoolean(8, hook);
                ps.setString(9, fuel.toString());
                ps.setInt(10, fuelEconomy);
                ps.setInt(11, estimatedValue);
                ps.setInt(12, ownerAnnualKm);
                ps.setInt(13, owner.getId());
                ps.setString(14, comments);
                
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String currentDatetime = dateFormat.format(new Date());
                ps.setString(15, currentDatetime);
                

                ps.executeUpdate();
                connection.commit();
                connection.setAutoCommit(true);
                
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();

                    return new Car(keys.getInt(1), type, brand, location, seats, doors, year, gps, hook, fuel, fuelEconomy, estimatedValue, ownerAnnualKm, owner, comments, currentDatetime);
                } catch (SQLException ex) {
                    throw new DataAccessException("Failed to get primary key for new user.", ex);
                }

            } catch (SQLException ex) {
                connection.rollback();
                connection.setAutoCommit(true);
                throw new DataAccessException("Failed to commit new user transaction.", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create user.", ex);
        }
    }

    @Override
    public void updateCar(Car car) throws DataAccessException {
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = updateCarStatement()) {
                ps.setString(1, car.getType());
                ps.setString(2, car.getBrand());
                ps.setInt(3, car.getLocation().getId());
                ps.setInt(4, car.getSeats());
                ps.setInt(5, car.getDoors());
                ps.setInt(6, car.getYear());
                ps.setBoolean(7, car.isGps());
                ps.setBoolean(8, car.isHook());
                ps.setString(9, car.getFuel().toString());
                ps.setInt(10, car.getFuelEconomy());
                ps.setInt(11,car.getEstimatedValue());
                ps.setInt(12,car.getOwnerAnnualKm());
                ps.setInt(13,car.getOwner().getId());
                ps.setString(14,car.getComments());
                
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                ps.setString(15, dateFormat.format(new Date()));
                

                ps.executeUpdate();
                connection.commit();
                connection.setAutoCommit(true);

            } catch (SQLException ex) {
                connection.rollback();
                connection.setAutoCommit(true);
                throw new DataAccessException("Failed to commit new user transaction.", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create user.", ex);
        }
    }

    @Override
    public Car getCar(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getCarStatement();
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();

                return populateCar(rs, true, true);
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading car resultset", ex);
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch car by id.", ex);
        }
    }
    
}
