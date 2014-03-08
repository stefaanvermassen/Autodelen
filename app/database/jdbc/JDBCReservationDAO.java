/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database.jdbc;

import database.DataAccessException;
import database.ReservationDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import models.Car;
import models.Reservation;
import models.ReservationStatus;
import models.User;

import org.joda.time.DateTime;

/**
 *
 * @author Laurent
 */
public class JDBCReservationDAO implements ReservationDAO{

    private static final String[] AUTO_GENERATED_KEYS = {"reservation_id"};

    private Connection connection;
    private PreparedStatement createReservationStatement;
    private PreparedStatement updateReservationStatement;
    private PreparedStatement getReservationStatement;
    private PreparedStatement deleteReservationStatement;
    private PreparedStatement getReservationListStatement;

    public JDBCReservationDAO(Connection connection) {
        this.connection = connection;
    }

    public static Reservation populateReservation(ResultSet rs) throws SQLException {

        Reservation reservation = new Reservation(rs.getInt("reservation_id"), JDBCCarDAO.populateCar(rs, false, false), JDBCUserDAO.populateUser(rs, false, false), new DateTime(rs.getTimestamp("reservation_from")), new DateTime(rs.getTimestamp("reservation_to")));
        reservation.setStatus(ReservationStatus.valueOf(rs.getString("reservation_status")));
        return reservation;
    }

    private PreparedStatement getDeleteReservationStatement() throws SQLException {
    	if(deleteReservationStatement == null){
    		deleteReservationStatement = connection.prepareStatement("DELETE FROM CarReservations WHERE reservation_id=?");
    	}
    	return deleteReservationStatement;
    }
    
    private PreparedStatement getCreateReservationStatement() throws SQLException {
        if (createReservationStatement == null) {
            createReservationStatement = connection.prepareStatement("INSERT INTO CarReservations (reservation_user_id, reservation_car_id, reservation_status,"
                    + "reservation_from, reservation_to) VALUES (?,?,?,?,?)", AUTO_GENERATED_KEYS);
        }
        return createReservationStatement;
    }

    private PreparedStatement getUpdateReservationStatement() throws SQLException {
        if (updateReservationStatement == null) {
            updateReservationStatement = connection.prepareStatement("UPDATE CarReservations SET reservation_user_id=? , reservation_car_id=? , reservation_status =? ,"
                    + "reservation_from=? , reservation_to=? WHERE reservation_id = ?");
        }
        return updateReservationStatement;
    }

    private PreparedStatement getGetReservationStatement() throws SQLException {
        if (getReservationStatement == null) {
            getReservationStatement = connection.prepareStatement("SELECT * FROM CarReservations INNER JOIN Cars ON CarReservations.reservation_car_id = Cars.car_id INNER JOIN Users ON CarReservations.reservation_user_id = Users.user_id WHERE reservation_id=?");
        }
        return getReservationStatement;
    }

    private PreparedStatement getGetReservationListStatement() throws SQLException {
        if (getReservationListStatement == null) {
            // Only request the reservations for which the current user is the loaner or the owner
            getReservationListStatement = connection.prepareStatement("SELECT * FROM CarReservations INNER JOIN Cars ON CarReservations.reservation_car_id = Cars.car_id INNER JOIN Users ON CarReservations.reservation_user_id = Users.user_id " +
                    "WHERE car_owner_user_id=? OR reservation_user_id=?");
        }
        return getReservationListStatement;
    }

    @Override
    public Reservation createReservation(DateTime from, DateTime to, Car car, User user) throws DataAccessException {
        try{
            PreparedStatement ps = getCreateReservationStatement();
            ps.setInt(1, user.getId());
            ps.setInt(2, car.getId());
            ps.setString(3,"REQUEST");
            ps.setTimestamp(4, new Timestamp(from.getMillis()));
            ps.setTimestamp(5, new Timestamp(to.getMillis()));
            
            ps.executeUpdate();
            
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                connection.commit();
                return new Reservation(keys.getInt(1), car, user, from, to);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new reservation.", ex);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to create reservation", e);
        }
    }


    @Override
    public void updateReservation(Reservation reservation) throws DataAccessException {
        try {
            PreparedStatement ps = getUpdateReservationStatement();
            ps.setInt(1, reservation.getUser().getId());
            ps.setInt(2, reservation.getCar().getId());
            ps.setString(3, reservation.getStatus().toString());
            ps.setTimestamp(4, new Timestamp(reservation.getFrom().getMillis()));
            ps.setTimestamp(5, new Timestamp(reservation.getTo().getMillis()));
            ps.setInt(6, reservation.getId());

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Reservation update affected 0 rows.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to update reservation", e);
        }
    }

    @Override
    public Reservation getReservation(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getGetReservationStatement();
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return populateReservation(rs);
            }catch (SQLException e){
                throw new DataAccessException("Error reading reservation resultset", e);

            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to update reservation", e);
        }
    }
    
    @Override
    public void deleteReservation(Reservation reservation){
    	try {
			PreparedStatement ps = getDeleteReservationStatement();
			ps.setInt(1, reservation.getId());
			ps.executeUpdate();
			connection.commit();
		} catch (SQLException ex){
			throw new DataAccessException("Could not delete reservation",ex);
		}
    }

    @Override
    public List<Reservation> getReservationList(int userId) throws DataAccessException {
        try {
            List<Reservation> list = new ArrayList<>();
            PreparedStatement ps = getGetReservationListStatement();
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(populateReservation(rs));
                }
                return list;
            }catch (SQLException e){
                throw new DataAccessException("Error while reading reservation resultset", e);

            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the list of reservations", e);
        }
    }


}
