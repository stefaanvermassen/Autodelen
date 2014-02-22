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

    private static final String[] AUTO_GENERATED_KEYS = {"ID"};

    private Connection connection;
    private PreparedStatement createReservationStatement;
    private PreparedStatement freePeriodStatement;

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

    @Override
    public boolean freePeriod(String from, String to, Car car) throws DataAccessException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    private PreparedStatement createReservationStatement() throws SQLException {
        if (createReservationStatement == null) {
            createReservationStatement = connection.prepareStatement("INSERT INTO carreservations (reservations_user_id, reservations_car_id, reservations_status"
                    + "reservations_from, reservations_to) VALUES (?,?,?,?,?)");
        }
        return createReservationStatement;
    }

    private PreparedStatement freePeriodStatement() throws SQLException {
        //TODO
        return null;
    }

    @Override
    public void updateReservation(Reservation reservation) throws DataAccessException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
