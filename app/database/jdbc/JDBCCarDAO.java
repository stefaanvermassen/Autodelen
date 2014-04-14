/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database.jdbc;

import database.CarDAO;
import database.DataAccessException;
import database.Filter;

import java.sql.*;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;

import database.FilterField;
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

    public static final String CAR_QUERY = "SELECT * FROM Cars INNER JOIN Addresses ON Addresses.address_id=Cars.car_location " +
            "INNER JOIN Users ON Users.user_id=Cars.car_owner_user_id ";

    public static final String FILTER_FRAGMENT = " WHERE Cars.car_name LIKE ? AND Cars.car_brand LIKE ? AND Cars.car_gps >= ? " +
            "AND Cars.car_hook >= ? AND Cars.car_seats >= ? AND Addresses.address_zipcode LIKE ? " +
            "AND Cars.car_id NOT IN (SELECT DISTINCT(car_id) FROM Cars INNER JOIN Carreservations " +
            "ON Carreservations.reservation_car_id = Cars.car_id " +
            "WHERE ? < CarReservations.reservation_to  AND ? >  CarReservations.reservation_from)";

    private void fillFragment(PreparedStatement ps, Filter filter, int start) throws SQLException {
        if(filter == null) {
            // getFieldContains on a "empty" filter will return the default string "%%", so this does not filter anything
            filter = createCarFilter();
        }
        ps.setString(start, filter.getValue(FilterField.CAR_NAME));
        ps.setString(start+1, filter.getValue(FilterField.CAR_BRAND));
        ps.setString(start+2, filter.getValue(FilterField.CAR_GPS));
        ps.setString(start+3, filter.getValue(FilterField.CAR_HOOK));
        ps.setString(start+4, filter.getValue(FilterField.CAR_SEATS));
        ps.setString(start+5, filter.getValue(FilterField.ZIPCODE));
        ps.setString(start+6, filter.getValue(FilterField.FROM));
        ps.setString(start+7, filter.getValue(FilterField.UNTIL));
    }


    private Connection connection;
    private PreparedStatement createCarStatement;
    private PreparedStatement updateCarStatement;
    private PreparedStatement getCarStatement;
    private PreparedStatement getCarsOfUserStatement;
    private PreparedStatement deleteCarStatement;
    private PreparedStatement getGetCarListPageByNameAscStatement;
    private PreparedStatement getGetCarListPageByNameDescStatement;
    private PreparedStatement getGetCarListPageByBrandAscStatement;
    private PreparedStatement getGetCarListPageByBrandDescStatement;
    private PreparedStatement getGetAmountOfCarsStatement;

    public JDBCCarDAO(Connection connection) {
        this.connection = connection;
    }

    public static Car populateCar(ResultSet rs, boolean withAddress, boolean withUser) throws SQLException {
        // Extra check if car actually exists
        if(rs.getObject("car_id") != null) {
            Car car = new Car();
            car.setId(rs.getInt("car_id"));
            car.setName(rs.getString("car_name"));
            car.setBrand(rs.getString("car_brand"));
            car.setType(rs.getString("car_type"));
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
            if(withUser) {
                user = JDBCUserDAO.populateUser(rs, false, false);
            } else {
                user = null;
            }
            car.setOwner(user);

            car.setFuel(CarFuel.valueOf(rs.getString("car_fuel")));

            return car;
        } else {
            return null;
        }
    }

    private PreparedStatement getDeleteCarStatement() throws SQLException {
    	if(deleteCarStatement == null){
    		deleteCarStatement = connection.prepareStatement("DELETE FROM Cars WHERE car_id = ?");
    	}
    	return deleteCarStatement;
    }
    
    private PreparedStatement createCarStatement() throws SQLException {
        if (createCarStatement == null) {
            createCarStatement = connection.prepareStatement("INSERT INTO Cars(car_name, car_type, car_brand, car_location, car_seats, car_doors, car_year, car_gps, car_hook, car_fuel, car_fuel_economy, car_estimated_value, car_owner_annual_km, car_owner_user_id, car_comments) VALUES (?, ?,?,?,?,?,?,?,?,?,?,?,?,?,?)", AUTO_GENERATED_KEYS);
        }
        return createCarStatement;
    }
    
    private PreparedStatement updateCarStatement() throws SQLException {
        if (updateCarStatement == null) {
            updateCarStatement = connection.prepareStatement("UPDATE Cars SET car_name=?, car_type=? , car_brand=? , car_location=? , car_seats=? , car_doors=? , car_year=? , car_gps=? , car_hook=? , car_fuel=? , car_fuel_economy=? , car_estimated_value=? , car_owner_annual_km=?, car_owner_user_id=? , car_comments=? WHERE car_id = ?");
        }
        return updateCarStatement;
    }
    
    private PreparedStatement getCarStatement() throws SQLException {
        if (getCarStatement == null) {
            getCarStatement = connection.prepareStatement("SELECT * FROM Cars LEFT JOIN Addresses ON Addresses.address_id=Cars.car_location LEFT JOIN Users ON Users.user_id=Cars.car_owner_user_id WHERE car_id=?");
        }
        return getCarStatement;
    }

    private PreparedStatement getGetCarsOfUserStatement() throws SQLException {
        if (getCarsOfUserStatement == null) {
            getCarsOfUserStatement = connection.prepareStatement("SELECT * FROM Cars LEFT JOIN Addresses ON Addresses.address_id=Cars.car_location LEFT JOIN Users ON Users.user_id=Cars.car_owner_user_id WHERE user_id=?");
        }
        return getCarsOfUserStatement;
    }


    private PreparedStatement getGetCarListPageByNameAscStatement() throws SQLException {
        if(getGetCarListPageByNameAscStatement == null) {
            getGetCarListPageByNameAscStatement = connection.prepareStatement(CAR_QUERY + FILTER_FRAGMENT + "ORDER BY car_name asc LIMIT ?, ?");
        }
        return getGetCarListPageByNameAscStatement;
    }

    private PreparedStatement getGetCarListPageByNameDescStatement() throws SQLException {
        if(getGetCarListPageByNameDescStatement == null) {
            getGetCarListPageByNameDescStatement = connection.prepareStatement(CAR_QUERY + FILTER_FRAGMENT +"ORDER BY car_name desc LIMIT ?, ?");
        }
        return getGetCarListPageByNameDescStatement;
    }
    private PreparedStatement getGetCarListPageByBrandAscStatement() throws SQLException {
        if(getGetCarListPageByBrandAscStatement == null) {
            getGetCarListPageByBrandAscStatement = connection.prepareStatement(CAR_QUERY + FILTER_FRAGMENT + "ORDER BY car_brand asc LIMIT ?, ?");
        }
        return getGetCarListPageByBrandAscStatement;
    }

    private PreparedStatement getGetCarListPageByBrandDescStatement() throws SQLException {
        if(getGetCarListPageByBrandDescStatement == null) {
            getGetCarListPageByBrandDescStatement = connection.prepareStatement(CAR_QUERY + FILTER_FRAGMENT + "ORDER BY car_brand desc LIMIT ?, ?");
        }
        return getGetCarListPageByBrandDescStatement;
    }

    private PreparedStatement getGetAmountOfCarsStatement() throws SQLException {
        if(getGetAmountOfCarsStatement == null) {
            getGetAmountOfCarsStatement = connection.prepareStatement("SELECT COUNT(car_id) AS amount_of_cars FROM Cars LEFT JOIN Addresses ON Addresses.address_id=Cars.car_location" + FILTER_FRAGMENT);
        }
        return getGetAmountOfCarsStatement;
    }
    
    @Override
    public Car createCar(String name, String brand, String type, Address location, int seats, int doors, int year, boolean gps, boolean hook, CarFuel fuel, int fuelEconomy, int estimatedValue, int ownerAnnualKm, User owner, String comments) throws DataAccessException {
        try {
            PreparedStatement ps = createCarStatement();
            ps.setString(1, name);
            ps.setString(2, type);
            ps.setString(3, brand);
            if(location != null) {
                ps.setInt(4, location.getId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setInt(5, seats);
            ps.setInt(6, doors);
            ps.setInt(7, year);
            //Calendar cal = Calendar.getInstance();
            //cal.set(year, 0,0);
            //ps.setDate(6, new Date(cal.getTime().getTime()));

            ps.setBoolean(8, gps);
            ps.setBoolean(9, hook);
            ps.setString(10, fuel.toString());
            ps.setInt(11, fuelEconomy);
            ps.setInt(12, estimatedValue);
            ps.setInt(13, ownerAnnualKm);
            // Owner cannot be null according to SQL script so this will throw an Exception
            if(owner != null) {
                ps.setInt(14, owner.getId());
            } else {
                ps.setNull(14, Types.INTEGER);
            }
            ps.setString(15, comments);

            java.sql.Date sqlDate = new Date(new java.util.Date().getTime());
            String currentDatetime = sqlDate.toString();
            //ps.setDate(15,sqlDate);

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating car.");
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new Car(keys.getInt(1), name, brand, type, location, seats, doors, year, gps, hook, fuel, fuelEconomy, estimatedValue, ownerAnnualKm, owner, comments, currentDatetime);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new car.", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create new car.", ex);
        }
    }

    @Override
    public void updateCar(Car car) throws DataAccessException {
        try {
            PreparedStatement ps = updateCarStatement();
            ps.setString(1, car.getName());
            ps.setString(2, car.getType());
            ps.setString(3, car.getBrand());
            if(car.getLocation() != null) {
                ps.setInt(4, car.getLocation().getId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setInt(5, car.getSeats());
            ps.setInt(6, car.getDoors());
            //Calendar cal = Calendar.getInstance();
            //cal.set(car.getYear(), 0,0);
            //ps.setDate(6, new Date(cal.getTime().getTime()));
            ps.setInt(7, car.getYear());
            ps.setBoolean(8, car.isGps());
            ps.setBoolean(9, car.isHook());
            ps.setString(10, car.getFuel().toString());
            ps.setInt(11, car.getFuelEconomy());
            ps.setInt(12,car.getEstimatedValue());
            ps.setInt(13,car.getOwnerAnnualKm());
            // If Owner == null, this should throw an error on execution
            if(car.getOwner() != null) {
                ps.setInt(14,car.getOwner().getId());
            } else {
                ps.setNull(14, Types.INTEGER);
            }
            ps.setString(15,car.getComments());
            //ps.setDate(15,new Date(new java.util.Date().getTime()));

            ps.setInt(16, car.getId());

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when updating car.");

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update car.", ex);
        }
    }

    @Override
    public Car getCar(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getCarStatement();
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return populateCar(rs, true, true);
                else return null;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading car resultset", ex);
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch car by id.", ex);
        }
    }

	@Override
	public void deleteCar(Car car) throws DataAccessException {
		try {
			PreparedStatement ps = getDeleteCarStatement();
			ps.setInt(1, car.getId());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when deleting car.");
		} catch (SQLException ex){
			throw new DataAccessException("Could not delete car",ex);
		}
		
	}

    /**
     * @param filter The filter to apply to
     * @return The amount of filtered cars
     * @throws DataAccessException
     */
    @Override
    public int getAmountOfCars(Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = getGetAmountOfCarsStatement();
            fillFragment(ps, filter, 1);

            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return rs.getInt("amount_of_cars");
                else return 0;

            } catch (SQLException ex) {
                throw new DataAccessException("Error reading count of cars", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of cars", ex);
        }
    }

    @Override
    public Filter createCarFilter() {
        return new JDBCFilter();
    }

    /**
     * Get a carlist, with the default ordering and without filtering
     * @param page The page you want to see
     * @param pageSize The page size
     * @return The page of list of cars
     */
    @Override
    public List<Car> getCarList(int page, int pageSize) throws DataAccessException {
        return getCarList(FilterField.CAR_NAME, true, page, pageSize, null);
    }

    /**
     * @param orderBy The field you want to order by
     * @param asc Ascending
     * @param page The page you want to see
     * @param pageSize The page size
     * @param filter The filter you want to apply
     * @return List of cars with custom ordering and filtering
     */
    @Override
    public List<Car> getCarList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = null;
            switch(orderBy) {
                case CAR_NAME:
                    ps = asc ? getGetCarListPageByNameAscStatement() : getGetCarListPageByNameDescStatement();
                    break;
                case CAR_BRAND:
                    ps = asc ? getGetCarListPageByBrandAscStatement() : getGetCarListPageByBrandDescStatement();
                    break;
            }
            if(ps == null) {
                throw new DataAccessException("Could not create getCarList statement");
            }

            fillFragment(ps, filter, 1);
            int first = (page-1)*pageSize;
            ps.setInt(9, first);
            ps.setInt(10, pageSize);
            return getCars(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of cars", ex);
        }
    }

    /**
     *
     * @param user_id The id of the user
     * @return The cars of the user (without pagination)
     * @throws DataAccessException
     */
    @Override
    public List<Car> getCarsOfUser(int user_id) throws DataAccessException {
        try {
            PreparedStatement ps = getGetCarsOfUserStatement();
            ps.setInt(1, user_id);
            return getCars(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of cars for user with id " + user_id, ex);
        }
    }


    private List<Car> getCars(PreparedStatement ps) {
        List<Car> cars = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cars.add(populateCar(rs, true, true));
            }
            return cars;
        } catch (SQLException ex) {
            throw new DataAccessException("Error reading cars resultset", ex);
        }
    }
}
