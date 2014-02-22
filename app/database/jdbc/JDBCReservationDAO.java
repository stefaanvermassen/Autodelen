/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database.jdbc;

import database.DataAccessException;
import database.ReservationDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import models.Car;
import models.Reservation;
import models.User;

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

    public JDBCReservationDAO(Connection connection) {
        this.connection = connection;
    }
    
    @Override
    public Reservation createReservation(String from, String to, Car car, User user) throws DataAccessException {
        try{
            PreparedStatement ps = createReservationStatement();
            ps.setInt(1, user.getId());
            ps.setInt(2, car.getId());
            ps.setString(3,"REQUEST");
            ps.setString(4, from);
            ps.setString(5, to);
            
            ps.executeUpdate();
            
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new Reservation(keys.getInt(1), car, user, from, to);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new reservation.", ex);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to create reservation", e);
        }
    }    
    
    private PreparedStatement createReservationStatement() throws SQLException {
        if (createReservationStatement == null) {
            createReservationStatement = connection.prepareStatement("INSERT INTO CarReservations (reservations_user_id, reservations_car_id, reservations_status"
                    + "reservations_from, reservations_to) VALUES (?,?,?,?,?)");
        }
        return createReservationStatement;
    }
    
    private PreparedStatement updateReservationStatement() throws SQLException {
       if (updateReservationStatement == null) {
            updateReservationStatement = connection.prepareStatement("UPDATE carreservations SET reservations_user_id=? , reservations_car_id=? , reservations_status =? ,"
                    + "reservations_from=? , reservations_to=? )");
        }
        return updateReservationStatement; 
    }
    
    private PreparedStatement getReservationStatement() throws SQLException {
       if (getReservationStatement == null) {
            getReservationStatement = connection.prepareStatement("SELECT * FROM carreservations WHERE reservation_id=?");
        }
        return getReservationStatement; 
    }


    @Override
    public void updateReservation(Reservation reservation) throws DataAccessException {
        try {
            PreparedStatement ps = updateReservationStatement();
            ps.setInt(1, reservation.getUser().getId());
            ps.setInt(2, reservation.getCar().getId());
            ps.setString(3, reservation.getStatus().toString());
            ps.setString(4, reservation.getFrom());
            ps.setString(5, reservation.getTo());

            ps.executeUpdate();
        } catch (SQLException e){
            throw new DataAccessException("Unable to update reservation", e);
        }
    }

    @Override
    public Reservation getReservation(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getReservationStatement();
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();

                throw new RuntimeException();
                /*
                Reservation reservation = new Reservation(id,
                        , 
                        , 
                        rs.getString("reservation_from"), 
                        rs.getString("reservation_to"));
            }catch (SQLException e){
                throw new DataAccessException("Error reading reservation resultset", e);
                */
            }
            
            
        } catch (SQLException e){
            throw new DataAccessException("Unable to update reservation", e);
        }
    }
    
}
