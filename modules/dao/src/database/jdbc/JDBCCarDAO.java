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
import java.util.Calendar;
import java.util.List;

import database.FilterField;
import models.*;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
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

    public static final String CAR_QUERY = "SELECT * FROM cars " +
            "LEFT JOIN addresses ON addresses.address_id=cars.car_location " +
            "LEFT JOIN users ON users.user_id=cars.car_owner_user_id " +
            "LEFT JOIN technicalcardetails ON technicalcardetails.details_id = cars.car_technical_details " +
            "LEFT JOIN carinsurances ON carinsurances.insurance_id = cars.car_insurance ";

    public static final String FILTER_FRAGMENT = " WHERE cars.car_name LIKE ? AND cars.car_brand LIKE ? AND cars.car_gps >= ? " +
            "AND cars.car_hook >= ? AND cars.car_seats >= ? AND addresses.address_zipcode LIKE ? AND cars.car_fuel LIKE ? " +
            "AND cars.car_id NOT IN (SELECT DISTINCT(car_id) FROM cars INNER JOIN Carreservations " +
            "ON Carreservations.reservation_car_id = cars.car_id " +
            "WHERE ? < carreservations.reservation_to  AND ? >  carreservations.reservation_from)";

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
    private PreparedStatement updateTechnicalCarDetailsStatement;
    private PreparedStatement createTechnicalCarDetailsStatement;
    private PreparedStatement updateInsuranceStatement;
    private PreparedStatement createInsuranceStatement;
    private PreparedStatement getAvailabilitiesStatement;
    private PreparedStatement createAvailabilityStatement;
    private PreparedStatement updateAvailabilityStatement;
    private PreparedStatement deleteAvailabilityStatement;
    private PreparedStatement getPriviligedStatement;
    private PreparedStatement createPriviligedStatement;
    private PreparedStatement deletePriviligedStatement;

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
            car.setActive(rs.getBoolean("car_active"));
            Address location = null;
            User user = null;
            TechnicalCarDetails technicalCarDetails = null;
            CarInsurance insurance = null;
            if(withRest) {
                location = JDBCAddressDAO.populateAddress(rs);
                user = JDBCUserDAO.populateUser(rs, false, false);
                rs.getInt("car_technical_details");
                if(!rs.wasNull()) {
                    FileGroup registration = new FileGroup(rs.getInt("details_car_registration"));
                    if(rs.wasNull()) registration = null;

                    Integer chassisNr = rs.getInt("details_car_chassis_number");
                    if(rs.wasNull()) chassisNr = null;
                    technicalCarDetails = new TechnicalCarDetails(rs.getInt("details_id"), rs.getString("details_car_license_plate"), registration, chassisNr);
                }
                rs.getInt("car_insurance");
                if(!rs.wasNull()) {
                    String name = rs.getString("insurance_name");
                    if(rs.wasNull()) name = null;
                    Date expiration = rs.getDate("insurance_expiration");
                    if(rs.wasNull()) expiration = null;
                    Integer bonusMalus = rs.getInt("insurance_bonus_malus");
                    if(rs.wasNull()) bonusMalus = null;
                    Integer contractId = rs.getInt("insurance_contract_id");
                    if(rs.wasNull()) contractId = null;
                    insurance = new CarInsurance(rs.getInt("insurance_id"), name, expiration, bonusMalus, contractId);
                }
            }
            car.setLocation(location);
            car.setOwner(user);
            car.setTechnicalCarDetails(technicalCarDetails);
            car.setInsurance(insurance);

            car.setFuel(CarFuel.valueOf(rs.getString("car_fuel")));

            return car;
        } else {
            return null;
        }
    }

    private PreparedStatement getDeleteCarStatement() throws SQLException {
    	if(deleteCarStatement == null){
    		deleteCarStatement = connection.prepareStatement("DELETE FROM cars WHERE car_id = ?");
    	}
    	return deleteCarStatement;
    }
    
    private PreparedStatement createCarStatement() throws SQLException {
        if (createCarStatement == null) {
            createCarStatement = connection.prepareStatement("INSERT INTO cars(car_name, car_type, car_brand, car_location, " +
                    "car_seats, car_doors, car_year, car_gps, car_hook, car_fuel, " +
                    "car_fuel_economy, car_estimated_value, car_owner_annual_km, " +
                    "car_technical_details, car_insurance, car_owner_user_id, car_comments, car_active) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", AUTO_GENERATED_KEYS);
        }
        return createCarStatement;
    }
    
    private PreparedStatement updateCarStatement() throws SQLException {
        if (updateCarStatement == null) {
            updateCarStatement = connection.prepareStatement("UPDATE cars SET car_name=?, car_type=? , car_brand=? , car_location=? , " +
                    "car_seats=? , car_doors=? , car_year=? , car_gps=? , car_hook=? , car_fuel=? , " +
                    "car_fuel_economy=? , car_estimated_value=? , car_owner_annual_km=? , " +
                    "car_technical_details=?, car_insurance=?, car_owner_user_id=? , car_comments=?, car_active=? WHERE car_id = ?");
        }
        return updateCarStatement;
    }
    
    private PreparedStatement getCarStatement() throws SQLException {
        if (getCarStatement == null) {
            getCarStatement = connection.prepareStatement(CAR_QUERY + " WHERE car_id=?");
        }
        return getCarStatement;
    }

    private PreparedStatement getGetCarsOfUserStatement() throws SQLException {
        if (getCarsOfUserStatement == null) {
            getCarsOfUserStatement = connection.prepareStatement(CAR_QUERY + " WHERE user_id=?");
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
            getGetAmountOfCarsStatement = connection.prepareStatement("SELECT COUNT(car_id) AS amount_of_cars FROM cars " +
                    "LEFT JOIN addresses ON addresses.address_id=cars.car_location " +
                    "LEFT JOIN users ON users.user_id=cars.car_owner_user_id " +
                    "LEFT JOIN technicalcardetails ON technicalcardetails.details_id = cars.car_technical_details " +
                    "LEFT JOIN carinsurances ON carinsurances.insurance_id = cars.car_insurance" + FILTER_FRAGMENT);
        }
        return getGetAmountOfCarsStatement;
    }

    private PreparedStatement createInsuranceStatement() throws SQLException {
        if (createInsuranceStatement == null) {
            createInsuranceStatement = connection.prepareStatement("INSERT INTO carinsurances(insurance_name, insurance_expiration, " +
                    "insurance_contract_id, insurance_bonus_malus) VALUES (?,?,?,?)", new String[] {"insurance_id"});
        }
        return createInsuranceStatement;
    }

    private PreparedStatement updateInsuranceStatement() throws SQLException {
        if (updateInsuranceStatement == null) {
            updateInsuranceStatement = connection.prepareStatement("UPDATE carinsurances SET insurance_name=?, insurance_expiration=?, " +
                    "insurance_contract_id=?, insurance_bonus_malus=? WHERE insurance_id = ?");
        }
        return updateInsuranceStatement;
    }

    private PreparedStatement createTechnicalCarDetailsStatement() throws SQLException {
        if (createTechnicalCarDetailsStatement == null) {
            createTechnicalCarDetailsStatement = connection.prepareStatement("INSERT INTO technicalcardetails(details_car_license_plate, " +
                    "details_car_registration, details_car_chassis_number) VALUES (?,?,?)", new String[] {"details_id"});
        }
        return createTechnicalCarDetailsStatement;
    }

    private PreparedStatement updateTechnicalCarDetailsStatement() throws SQLException {
        if (updateTechnicalCarDetailsStatement == null) {
            updateTechnicalCarDetailsStatement = connection.prepareStatement("UPDATE technicalcardetails SET details_car_license_plate=?, " +
                    "details_car_registration=?, details_car_chassis_number=? WHERE details_id = ?");
        }
        return updateTechnicalCarDetailsStatement;
    }

    private PreparedStatement getAvailabilitiesStatement() throws SQLException {
        if (getAvailabilitiesStatement == null) {
            getAvailabilitiesStatement = connection.prepareStatement("SELECT * FROM caravailabilities WHERE car_availability_car_id=?");
        }
        return getAvailabilitiesStatement;
    }

    private PreparedStatement createAvailabilityStatement() throws SQLException {
        if (createAvailabilityStatement == null) {
            createAvailabilityStatement = connection.prepareStatement("INSERT INTO caravailabilities(car_availability_car_id, " +
                    "car_availability_begin_day_of_week, car_availability_begin_time, car_availability_end_day_of_week, car_availability_end_time) " +
                    "VALUES (?,?,?,?,?)", new String[] {"car_availability_id"});
        }
        return createAvailabilityStatement;
    }

    private PreparedStatement updateAvailabilityStatement() throws SQLException {
        if (updateAvailabilityStatement == null) {
            updateAvailabilityStatement = connection.prepareStatement("UPDATE caravailabilities SET car_availability_car_id=?, " +
                    "car_availability_begin_day_of_week=?, car_availability_begin_time=?, car_availability_end_day_of_week=?, car_availability_end_time=? " +
                    "WHERE car_availability_id = ?");
        }
        return updateAvailabilityStatement;
    }

    private PreparedStatement deleteAvailabilityStatement() throws SQLException {
        if (deleteAvailabilityStatement == null) {
            deleteAvailabilityStatement = connection.prepareStatement("DELETE FROM caravailabilities WHERE car_availability_id = ?");
        }
        return deleteAvailabilityStatement;
    }

    private PreparedStatement getPriviligedStatement() throws SQLException {
        if (getPriviligedStatement == null) {
            getPriviligedStatement = connection.prepareStatement("SELECT * FROM carprivileges " +
                    "INNER JOIN users ON users.user_id = carprivileges.car_privilege_user_id WHERE car_privilege_car_id=?");
        }
        return getPriviligedStatement;
    }

    private PreparedStatement createPriviligedStatement() throws SQLException {
        if (createPriviligedStatement == null) {
            createPriviligedStatement = connection.prepareStatement("INSERT INTO carprivileges(car_privilege_user_id, " +
                    "car_privilege_car_id) " +
                    "VALUES (?,?)");
        }
        return createPriviligedStatement;
    }

    private PreparedStatement deletePriviligedStatement() throws SQLException {
        if (deletePriviligedStatement == null) {
            deletePriviligedStatement = connection.prepareStatement("DELETE FROM carprivileges WHERE car_privilege_user_id = ? AND car_privilege_car_id=?");
        }
        return deletePriviligedStatement;
    }
    
    @Override
    public Car createCar(String name, String brand, String type, Address location, Integer seats, Integer doors, Integer year,
                         boolean gps, boolean hook, CarFuel fuel, Integer fuelEconomy, Integer estimatedValue, Integer ownerAnnualKm,
                         TechnicalCarDetails technicalCarDetails, CarInsurance insurance, User owner, String comments, boolean active) throws DataAccessException {
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
                createOrUpdateTechnicalCarDetails(technicalCarDetails);
                ps.setInt(14, technicalCarDetails.getId());
            } else {
                ps.setNull(14, Types.INTEGER);
            }

            // CarInsurance is also in seperate table
            if(insurance != null) {
                createOrUpdateInsurance(insurance);
                ps.setInt(15, insurance.getId());
            } else {
                ps.setNull(15, Types.INTEGER);
            }

            // Owner cannot be null according to SQL script so this will throw an Exception
            if(owner != null) {
                ps.setInt(16, owner.getId());
            } else {
                ps.setNull(16, Types.INTEGER);
            }
            ps.setString(17, comments);
            ps.setBoolean(18, active);

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating car.");
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                int id = keys.getInt(1);
                Car car = new Car(id, name, brand, type, location, seats, doors, year, gps, hook, fuel, fuelEconomy, estimatedValue, ownerAnnualKm, technicalCarDetails, insurance, owner, comments);
                car.setActive(active);
                return car;
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new car.", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create new car.", ex);
        }
    }

    private void setTechnicalCarDetailsVariables(PreparedStatement ps, TechnicalCarDetails technicalCarDetails) throws SQLException {
        if(technicalCarDetails.getLicensePlate() != null)
            ps.setString(1, technicalCarDetails.getLicensePlate());
        else
            ps.setNull(1, Types.VARCHAR);
        if(technicalCarDetails.getRegistration() != null)
            ps.setInt(2, technicalCarDetails.getRegistration().getId());
        else
            ps.setNull(2, Types.INTEGER);
        if(technicalCarDetails.getChassisNumber() != null)
            ps.setInt(3, technicalCarDetails.getChassisNumber());
        else
            ps.setNull(3, Types.INTEGER);
    }

    private void createOrUpdateTechnicalCarDetails(TechnicalCarDetails technicalCarDetails) throws DataAccessException {
        try {
            if(technicalCarDetails.getId() == null) { // create
                PreparedStatement ps = createTechnicalCarDetailsStatement();
                setTechnicalCarDetailsVariables(ps, technicalCarDetails);

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
                setTechnicalCarDetailsVariables(ps, technicalCarDetails);

                ps.setInt(4, technicalCarDetails.getId());

                if(ps.executeUpdate() == 0)
                    throw new DataAccessException("No rows were affected when updating technicalCarDetails.");

            }
        } catch(SQLException ex) {
            throw new DataAccessException("Failed to create/update new TechnicalCarDetails");
        }
    }

    private void setCarInsuranceVariables(PreparedStatement ps, CarInsurance insurance) throws SQLException {
        if(insurance.getName() != null)
            ps.setString(1, insurance.getName());
        else
            ps.setNull(1, Types.VARCHAR);
        if(insurance.getExpiration() != null)
            ps.setDate(2, new Date(insurance.getExpiration().getTime()));
        else
            ps.setNull(2, Types.DATE);
        if(insurance.getPolisNr() != null)
            ps.setInt(3, insurance.getPolisNr());
        else
            ps.setNull(3, Types.INTEGER);
        if(insurance.getBonusMalus() != null)
            ps.setInt(4, insurance.getBonusMalus());
        else
            ps.setNull(4, Types.INTEGER);
    }

    private void createOrUpdateInsurance(CarInsurance insurance) throws DataAccessException {
        try {
            if(insurance.getId() == null) { // create
                PreparedStatement ps = createInsuranceStatement();
                setCarInsuranceVariables(ps, insurance);

                if(ps.executeUpdate() == 0)
                    throw new DataAccessException("No rows were affected when creating carInsurance.");
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    int id = keys.getInt(1);
                    insurance.setId(id);
                } catch (SQLException ex) {
                    throw new DataAccessException("Failed to get primary key for new carInsurance.", ex);
                }
            } else { // update
                PreparedStatement ps = updateInsuranceStatement();
                setCarInsuranceVariables(ps, insurance);

                ps.setInt(5, insurance.getId());

                if(ps.executeUpdate() == 0)
                    throw new DataAccessException("No rows were affected when updating carInsurance.");

            }
        } catch(SQLException ex) {
            throw new DataAccessException("Failed to create new CarInsurance");
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
                createOrUpdateTechnicalCarDetails(car.getTechnicalCarDetails());
                ps.setInt(14, car.getTechnicalCarDetails().getId());
            } else {
                ps.setNull(14, Types.INTEGER);
            }

            if(car.getInsurance() != null) {
                createOrUpdateInsurance(car.getInsurance());
                ps.setInt(15, car.getInsurance().getId());
            } else {
                ps.setNull(15, Types.INTEGER);
            }

            // If Owner == null, this should throw an error on execution
            if(car.getOwner() != null) {
                ps.setInt(16,car.getOwner().getId());
            } else {
                ps.setNull(16, Types.INTEGER);
            }
            ps.setString(17, car.getComments());

            ps.setBoolean(18, car.isActive());

            ps.setInt(19, car.getId());

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
                if(rs.next()) {
                    Car car = populateCar(rs, true);
                    car.setAvailabilities(getAvailabilities(car));
                    car.setPriviliged(getPriviliged(car));
                    return car;
                } else return null;
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

    @Override
    public List<CarAvailabilityInterval> getAvailabilities(Car car) throws DataAccessException {
        try {
            PreparedStatement ps = getAvailabilitiesStatement();
            ps.setInt(1, car.getId());
            List<CarAvailabilityInterval> availabilities = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Time beginTime = rs.getTime("car_availability_begin_time");
                    LocalTime beginLocalTime = LocalTime.fromDateFields(beginTime);

                    Time endTime = rs.getTime("car_availability_end_time");
                    LocalTime endLocalTime = LocalTime.fromDateFields(endTime);

                    availabilities.add(new CarAvailabilityInterval(rs.getInt("car_availability_id"), DayOfWeek.getDayFromInt(rs.getInt("car_availability_begin_day_of_week")),
                            beginLocalTime, DayOfWeek.getDayFromInt(rs.getInt("car_availability_end_day_of_week")), endLocalTime));
                }
                return availabilities;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading availabilities resultset", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of availabilities", ex);
        }
    }

    private void setAvailabilityVariables(PreparedStatement ps, int carId, CarAvailabilityInterval availability) throws SQLException {
        ps.setInt(1, carId);
        ps.setInt(2, availability.getBeginDayOfWeek().getI());
        ps.setTime(3, new Time(availability.getBeginTime().toDateTimeToday().getMillis()));
        ps.setInt(4, availability.getEndDayOfWeek().getI());
        ps.setTime(5, new Time(availability.getEndTime().toDateTimeToday().getMillis()));
    }

    @Override
    public void addOrUpdateAvailabilities(Car car, List<CarAvailabilityInterval> availabilities) throws DataAccessException {
        try {
            for(CarAvailabilityInterval availability : availabilities) {
                if(availability.getId() == null) { // create
                    PreparedStatement ps = createAvailabilityStatement();
                    setAvailabilityVariables(ps, car.getId(), availability);

                    if(ps.executeUpdate() == 0)
                        throw new DataAccessException("No rows were affected when creating availability.");
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        int id = keys.getInt(1);
                        availability.setId(id);
                    } catch (SQLException ex) {
                        throw new DataAccessException("Failed to get primary key for new availability.", ex);
                    }
                } else { // update
                    PreparedStatement ps = updateAvailabilityStatement();
                    setAvailabilityVariables(ps, car.getId(), availability);

                    ps.setInt(6, availability.getId());

                    if(ps.executeUpdate() == 0)
                        throw new DataAccessException("No rows were affected when updating availability.");

                }
            }
        } catch(SQLException ex) {
            throw new DataAccessException("Failed to create/update new availabilitiy");
        }
    }

    @Override
    public void deleteAvailabilties(List<CarAvailabilityInterval> availabilities) throws DataAccessException {
        try {
            for(CarAvailabilityInterval availability : availabilities) {
                if(availability.getId() == null) {
                    throw new DataAccessException("No id is available to availability.");
                } else {
                    PreparedStatement ps = deleteAvailabilityStatement();

                    ps.setInt(1, availability.getId());

                    if(ps.executeUpdate() == 0)
                        throw new DataAccessException("No rows were affected when deleting availability.");
                }
            }
        } catch(SQLException ex) {
            throw new DataAccessException("Failed to delete new availabilitiy");
        }
    }

    @Override
    public List<User> getPriviliged(Car car) throws DataAccessException {
        try {
            PreparedStatement ps = getPriviligedStatement();
            ps.setInt(1, car.getId());
            List<User> users = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(JDBCUserDAO.populateUser(rs, false, false));
                }
                return users;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading priviliged resultset", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of priviliged", ex);
        }
    }

    @Override
    public void addPriviliged(Car car, List<User> users) throws DataAccessException {
        try {
            for(User user : users) {
                PreparedStatement ps = createPriviligedStatement();
                ps.setInt(1, user.getId());
                ps.setInt(2, car.getId());

                if(ps.executeUpdate() == 0)
                    throw new DataAccessException("No rows were affected when creating priviliged.");
            }
        } catch(SQLException ex) {
            throw new DataAccessException("Failed to create new priviliged");
        }
    }

    @Override
    public void deletePriviliged(Car car, List<User> users) throws DataAccessException {
        try {
            for(User user : users) {
                PreparedStatement ps = deletePriviligedStatement();

                ps.setInt(1, user.getId());
                ps.setInt(2, car.getId());

                if(ps.executeUpdate() == 0)
                    throw new DataAccessException("No rows were affected when deleting priviliged.");

            }
        } catch(SQLException ex) {
            throw new DataAccessException("Failed to delete priviliged");
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
