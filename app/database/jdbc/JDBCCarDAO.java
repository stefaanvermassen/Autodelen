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
import models.*;
import org.h2.command.Prepared;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author Laurent
 */
public class JDBCCarDAO implements CarDAO{

    private static final DateTimeFormatter DATEFORMATTER =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
    private static final String[] AUTO_GENERATED_KEYS = {"car_id"};

    public static final String CAR_QUERY = "SELECT * FROM Cars " +
            "LEFT JOIN Addresses ON Addresses.address_id=Cars.car_location " +
            "LEFT JOIN Users ON Users.user_id=Cars.car_owner_user_id " +
            "LEFT JOIN TechnicalCarDetails ON TechnicalCarDetails.details_id = Cars.car_technical_details ";

    public static final String FILTER_FRAGMENT = " WHERE Cars.car_name LIKE ? AND Cars.car_brand LIKE ? AND Cars.car_gps >= ? " +
            "AND Cars.car_hook >= ? AND Cars.car_seats >= ? AND Addresses.address_zipcode LIKE ? AND Cars.car_fuel LIKE ? " +
            "AND Cars.car_id NOT IN (SELECT DISTINCT(car_id) FROM Cars INNER JOIN Carreservations " +
            "ON Carreservations.reservation_car_id = Cars.car_id " +
            "WHERE ? < CarReservations.reservation_to  AND ? >  CarReservations.reservation_from)";

    private void fillFragment(PreparedStatement ps, Filter filter, int start) throws SQLException {
        if(filter == null) {
            // getFieldContains on a "empty" filter will return the default string "%%", so this does not filter anything
            filter = new JDBCFilter();
        }
        ps.setString(start, filter.getValue(FilterField.CAR_NAME));
        ps.setString(start+1, filter.getValue(FilterField.CAR_BRAND));
        ps.setString(start+2, filter.getValue(FilterField.CAR_GPS));
        ps.setString(start+3, filter.getValue(FilterField.CAR_HOOK));
        ps.setString(start+4, filter.getValue(FilterField.CAR_SEATS));
        ps.setString(start+5, filter.getValue(FilterField.ZIPCODE));
        String fuel = filter.getValue(FilterField.CAR_FUEL);
        if(fuel.equals("All") || fuel.equals(""))
            fuel = "%%";
        ps.setString(start+6, fuel);
        ps.setString(start+7, filter.getValue(FilterField.FROM));
        ps.setString(start+8, filter.getValue(FilterField.UNTIL));
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
    private PreparedStatement createTechnicalCarDetailsStatement;
    private PreparedStatement updateTechnicalCarDetailsStatement;


    public JDBCCarDAO(Connection connection) {
        this.connection = connection;
    }

    public static Car populateCar(ResultSet rs, boolean withRest) throws SQLException {
        // Extra check if car actually exists
        if(rs.getObject("car_id") != null) {
            Car car = new Car();
            car.setId(rs.getInt("car_id"));
            car.setName(rs.getString("car_name"));
            car.setBrand(rs.getString("car_brand"));
            car.setType(rs.getString("car_type"));
            Integer seats = rs.getInt("car_seats");
            if(!rs.wasNull())
                car.setSeats(seats);
            Integer doors = rs.getInt("car_doors");
            if(!rs.wasNull())
                car.setDoors(doors);
            car.setGps(rs.getBoolean("car_gps"));
            car.setHook(rs.getBoolean("car_hook"));
            Integer year = rs.getInt("car_year");
            if(!rs.wasNull())
                car.setYear(year);
            Integer estimatedValue = rs.getInt("car_estimated_value");
            if(!rs.wasNull())
                car.setEstimatedValue(estimatedValue);
            Integer fuelEconomy = rs.getInt("car_fuel_economy");
            if(!rs.wasNull())
                car.setFuelEconomy(fuelEconomy);
            Integer ownerAnnualKm = rs.getInt("car_owner_annual_km");
            if(!rs.wasNull())
                car.setOwnerAnnualKm(ownerAnnualKm);
            car.setComments(rs.getString("car_comments"));
            Address location = null;
            User user = null;
            TechnicalCarDetails technicalCarDetails = null;
            if(withRest) {
                location = JDBCAddressDAO.populateAddress(rs);
                user = JDBCUserDAO.populateUser(rs, false, false);
                rs.getInt("car_technical_details");
                if(!rs.wasNull())
                    technicalCarDetails = new TechnicalCarDetails(rs.getInt("details_id"), rs.getString("details_car_license_plate"), rs.getString("details_car_registration"), rs.getInt("details_car_chassis_number"));
            }
            car.setLocation(location);
            car.setOwner(user);
            car.setTechnicalCarDetails(technicalCarDetails);

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
            createCarStatement = connection.prepareStatement("INSERT INTO Cars(car_name, car_type, car_brand, car_location, " +
                    "car_seats, car_doors, car_year, car_gps, car_hook, car_fuel, " +
                    "car_fuel_economy, car_estimated_value, car_owner_annual_km, " +
                    "car_technical_details, car_owner_user_id, car_comments) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", AUTO_GENERATED_KEYS);
        }
        return createCarStatement;
    }
    
    private PreparedStatement updateCarStatement() throws SQLException {
        if (updateCarStatement == null) {
            updateCarStatement = connection.prepareStatement("UPDATE Cars SET car_name=?, car_type=? , car_brand=? , car_location=? , " +
                    "car_seats=? , car_doors=? , car_year=? , car_gps=? , car_hook=? , car_fuel=? , " +
                    "car_fuel_economy=? , car_estimated_value=? , car_owner_annual_km=? , " +
                    "car_technical_details=?, car_owner_user_id=? , car_comments=? WHERE car_id = ?");
        }
        return updateCarStatement;
    }
    
    private PreparedStatement getCarStatement() throws SQLException {
        if (getCarStatement == null) {
            getCarStatement = connection.prepareStatement("SELECT * FROM Cars LEFT JOIN Addresses ON Addresses.address_id=Cars.car_location " +
                    "LEFT JOIN Users ON Users.user_id=Cars.car_owner_user_id " +
                    "LEFT JOIN TechnicalCarDetails ON TechnicalCarDetails.details_id = Cars.car_technical_details WHERE car_id=?");
        }
        return getCarStatement;
    }

    private PreparedStatement getGetCarsOfUserStatement() throws SQLException {
        if (getCarsOfUserStatement == null) {
            getCarsOfUserStatement = connection.prepareStatement("SELECT * FROM Cars " +
                    "LEFT JOIN Addresses ON Addresses.address_id=Cars.car_location " +
                    "LEFT JOIN Users ON Users.user_id=Cars.car_owner_user_id " +
                    "LEFT JOIN TechnicalCarDetails ON TechnicalCarDetails.details_id = Cars.car_technical_details WHERE user_id=?");
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
            getGetAmountOfCarsStatement = connection.prepareStatement("SELECT COUNT(car_id) AS amount_of_cars FROM Cars " +
                    "LEFT JOIN Addresses ON Addresses.address_id=Cars.car_location " +
                            "LEFT JOIN Users ON Users.user_id=Cars.car_owner_user_id " +
                            "LEFT JOIN TechnicalCarDetails ON TechnicalCarDetails.details_id = Cars.car_technical_details " + FILTER_FRAGMENT);
        }
        return getGetAmountOfCarsStatement;
    }

    private PreparedStatement createTechnicalCarDetailsStatement() throws SQLException {
        if (createTechnicalCarDetailsStatement == null) {
            createTechnicalCarDetailsStatement = connection.prepareStatement("INSERT INTO TechnicalCarDetails(details_car_license_plate, " +
                    "details_car_registration, details_car_chassis_number) VALUES (?,?,?)", new String[] {"details_id"});
        }
        return createTechnicalCarDetailsStatement;
    }

    private PreparedStatement updateTechnicalCarDetailsStatement() throws SQLException {
        if (updateTechnicalCarDetailsStatement == null) {
            updateTechnicalCarDetailsStatement = connection.prepareStatement("UPDATE TechnicalCarDetails SET details_car_license_plate=?, " +
                    "details_car_registration=?, details_car_chassis_number=? WHERE details_id = ?");
        }
        return updateTechnicalCarDetailsStatement;
    }
    
    @Override
    public Car createCar(String name, String brand, String type, Address location, Integer seats, Integer doors, Integer year, boolean gps, boolean hook, CarFuel fuel, Integer fuelEconomy, Integer estimatedValue, Integer ownerAnnualKm, TechnicalCarDetails technicalCarDetails, User owner, String comments) throws DataAccessException {
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
            if(seats != null) {
                ps.setInt(5, seats);
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            if(doors != null) {
                ps.setInt(6, doors);
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            if(year != null) {
                ps.setInt(7, year);
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setBoolean(8, gps);
            ps.setBoolean(9, hook);
            ps.setString(10, fuel.toString());
            if(fuelEconomy != null) {
                ps.setInt(11, fuelEconomy);
            } else {
                ps.setNull(11, Types.INTEGER);
            }
            if(estimatedValue!= null) {
                ps.setInt(12, estimatedValue);
            } else {
                ps.setNull(12, Types.INTEGER);
            }
            if(ownerAnnualKm != null) {
                ps.setInt(13, ownerAnnualKm);
            } else {
                ps.setNull(13, Types.INTEGER);
            }
            // TechnicalCarDetails is in seperate table
            if(technicalCarDetails != null) {
                createOrUpdatetechnicalCarDetails(technicalCarDetails);
                ps.setInt(14, technicalCarDetails.getId());
            } else {
                ps.setNull(14, Types.INTEGER);
            }

            // Owner cannot be null according to SQL script so this will throw an Exception
            if(owner != null) {
                ps.setInt(15, owner.getId());
            } else {
                ps.setNull(15, Types.INTEGER);
            }
            ps.setString(16, comments);

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating car.");
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                int id = keys.getInt(1);
                return new Car(id, name, brand, type, location, seats, doors, year, gps, hook, fuel, fuelEconomy, estimatedValue, ownerAnnualKm, technicalCarDetails, owner, comments);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new car.", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create new car.", ex);
        }
    }

    private void createOrUpdatetechnicalCarDetails(TechnicalCarDetails technicalCarDetails) throws DataAccessException {
        try {
            if(technicalCarDetails.getId() == null) { // create
                PreparedStatement ps = createTechnicalCarDetailsStatement();
                if(technicalCarDetails.getLicensePlate() != null)
                    ps.setString(1, technicalCarDetails.getLicensePlate());
                else
                    ps.setNull(1, Types.VARCHAR);
                if(technicalCarDetails.getRegistration() != null)
                    ps.setString(2, technicalCarDetails.getRegistration());
                else
                    ps.setNull(2, Types.VARCHAR);
                if(technicalCarDetails.getChassisNumber() != null)
                    ps.setInt(3, technicalCarDetails.getChassisNumber());
                else
                    ps.setNull(3, Types.INTEGER);

                if(ps.executeUpdate() == 0)
                    throw new DataAccessException("No rows were affected when creating technicalCarDetails.");
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    int id = keys.getInt(1);
                    technicalCarDetails.setId(id);
                } catch (SQLException ex) {
                    throw new DataAccessException("Failed to get primary key for new technicalCarDetails.", ex);
                }
            } else { // update
                PreparedStatement ps = updateTechnicalCarDetailsStatement();
                if(technicalCarDetails.getLicensePlate() != null)
                    ps.setString(1, technicalCarDetails.getLicensePlate());
                else
                    ps.setNull(1, Types.VARCHAR);
                if(technicalCarDetails.getRegistration() != null)
                    ps.setString(2, technicalCarDetails.getRegistration());
                else
                    ps.setNull(2, Types.VARCHAR);
                if(technicalCarDetails.getChassisNumber() != null)
                    ps.setInt(3, technicalCarDetails.getChassisNumber());
                else
                    ps.setNull(3, Types.INTEGER);
                ps.setInt(4, technicalCarDetails.getId());

                if(ps.executeUpdate() == 0)
                    throw new DataAccessException("No rows were affected when updating technicalCarDetails.");

            }
        } catch(SQLException ex) {
            throw new DataAccessException("Failed to create new TechnicalCarDetails");
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
            if(car.getSeats() != null) {
                ps.setInt(5, car.getSeats());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            if(car.getDoors() != null) {
                ps.setInt(6, car.getDoors());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            if(car.getYear() != null) {
                ps.setInt(7, car.getYear());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setBoolean(8, car.isGps());
            ps.setBoolean(9, car.isHook());
            ps.setString(10, car.getFuel().toString());
            if(car.getFuelEconomy() != null) {
                ps.setInt(11, car.getFuelEconomy());
            } else {
                ps.setNull(11, Types.INTEGER);
            }
            if(car.getEstimatedValue()!= null) {
                ps.setInt(12, car.getEstimatedValue());
            } else {
                ps.setNull(12, Types.INTEGER);
            }
            if(car.getOwnerAnnualKm() != null) {
                ps.setInt(13, car.getOwnerAnnualKm());
            } else {
                ps.setNull(13, Types.INTEGER);
            }

            if(car.getTechnicalCarDetails() != null) {
                createOrUpdatetechnicalCarDetails(car.getTechnicalCarDetails());
                ps.setInt(14, car.getTechnicalCarDetails().getId());
            } else {
                ps.setNull(14, Types.INTEGER);
            }

            // If Owner == null, this should throw an error on execution
            if(car.getOwner() != null) {
                ps.setInt(15,car.getOwner().getId());
            } else {
                ps.setNull(15, Types.INTEGER);
            }
            ps.setString(16,car.getComments());

            ps.setInt(17, car.getId());

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
                    return populateCar(rs, true);
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
            ps.setInt(10, first);
            ps.setInt(11, pageSize);
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
                cars.add(populateCar(rs, true));
            }
            return cars;
        } catch (SQLException ex) {
            throw new DataAccessException("Error reading cars resultset", ex);
        }
    }
}
