/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database.jdbc;

import database.CarDAO;
import database.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import models.Car;
import models.CarFuel;
import models.Location;
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
            getCarStatement = connection.prepareStatement("SELECT * FROM Cars WHERE car_id=?");
        }
        return getCarStatement;
    }
    
    @Override
    public Car createCar(String brand, String type, Location location, int seats, int doors, int year, boolean gps, boolean hook, CarFuel fuel, int fuelEconomy, int estimatedValue, int ownerAnnualKm, User owner, String comments) throws DataAccessException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateCar(Car car) throws DataAccessException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Car getCar(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getCarStatement();
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();

                Car car = new Car();
                car.setId(id);
                car.setBrand(rs.getString("car_brand"));
                car.setType(rs.getString("car_brand"));
                car.setComments(rs.getString("car_comments"));
                car.setDoors(rs.getInt("car_doord"));
                car.setEstimatedValue(rs.getInt("car_estimated_value"));
                car.setFuelEconomy(rs.getInt("car_fuel_economy"));
                car.setGps(rs.getBoolean("car_gps"));
                car.setHook(rs.getBoolean("car_hook"));
                

                //TODO: verder afwerken

                return car;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading car resultset", ex);
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch car by id.", ex);
        }
    }
    
}
