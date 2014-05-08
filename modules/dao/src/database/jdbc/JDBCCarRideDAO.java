package database.jdbc;

import database.CarRideDAO;
import database.DataAccessException;
import models.CarRide;
import models.Reservation;

import java.sql.*;

/**
 * Created by HannesM on 10/03/14.
 */
public class JDBCCarRideDAO implements CarRideDAO {

    private Connection connection;

    private PreparedStatement createCarRideStatement;
    private PreparedStatement updateCarRideStatement;
    private PreparedStatement getCarRideStatement;

    public JDBCCarRideDAO(Connection connection) {
        this.connection = connection;
    }


    public static CarRide populateCarRide(ResultSet rs) throws SQLException {
        CarRide carRide = new CarRide(JDBCReservationDAO.populateReservation(rs));
        carRide.setStatus(rs.getBoolean("car_ride_status"));
        carRide.setStartMileage(rs.getInt("car_ride_start_mileage"));
        carRide.setEndMileage(rs.getInt("car_ride_end_mileage"));
        carRide.setDamaged(rs.getBoolean("car_ride_damage"));
        carRide.setRefueling(rs.getInt("car_ride_refueling"));

        return carRide;
    }

    private PreparedStatement getCreateCarRideStatement() throws SQLException {
        if (createCarRideStatement == null) {
            createCarRideStatement = connection.prepareStatement("INSERT INTO carrides (car_ride_car_reservation_id, car_ride_start_mileage, " +
                    "car_ride_end_mileage, car_ride_damage, car_ride_refueling) VALUE (?, ?, ?, ?, ?)");
        }
        return createCarRideStatement;
    }
    private PreparedStatement getUpdateCarRideStatement() throws SQLException {
        if (updateCarRideStatement == null) {
            updateCarRideStatement = connection.prepareStatement("UPDATE carrides SET car_ride_status = ? , car_ride_start_mileage = ? , " +
                    "car_ride_end_mileage = ? , car_ride_damage = ? , car_ride_refueling = ? " +
                    "WHERE car_ride_car_reservation_id = ?");
        }
        return updateCarRideStatement;
    }

    private PreparedStatement getGetCarRideStatement() throws SQLException {
        if (getCarRideStatement == null) {
            getCarRideStatement = connection.prepareStatement("SELECT * FROM carrides INNER JOIN carreservations ON carrides.car_ride_car_reservation_id = carreservations.reservation_id " +
                    "INNER JOIN cars ON carreservations.reservation_car_id = cars.car_id INNER JOIN users ON carreservations.reservation_user_id = users.user_id" +
                    " WHERE car_ride_car_reservation_id = ?");
        }
        return getCarRideStatement;
    }


    @Override
    public CarRide createCarRide(Reservation reservation, int startMileage, int endMileage, boolean damaged, int refueling) throws DataAccessException {
        try{
            PreparedStatement ps = getCreateCarRideStatement();
            ps.setInt(1, reservation.getId());
            ps.setInt(2, startMileage);
            ps.setInt(3, endMileage);
            ps.setBoolean(4, damaged);
            ps.setInt(5, refueling);

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating car ride.");

                return new CarRide(reservation);
        } catch (SQLException e){
            throw new DataAccessException("Unable to create car ride", e);
        }
    }

    @Override
    public CarRide getCarRide(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getGetCarRideStatement();
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return populateCarRide(rs);
                else return null;
            }catch (SQLException e){
                throw new DataAccessException("Error reading car ride resultset", e);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to get car ride", e);
        }
    }

    @Override
    public void updateCarRide(CarRide carRide) throws DataAccessException {
        try {
            PreparedStatement ps = getUpdateCarRideStatement();
            ps.setBoolean(1, carRide.isStatus());
            ps.setInt(2, carRide.getStartMileage());
            ps.setInt(3, carRide.getEndMileage());
            ps.setBoolean(4, carRide.isDamaged());
            ps.setInt(5, carRide.getRefueling());

            ps.setInt(6, carRide.getReservation().getId());

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Car Ride update affected 0 rows.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to update car ride", e);
        }
    }
}
