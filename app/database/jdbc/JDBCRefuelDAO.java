package database.jdbc;

import database.CarRideDAO;
import database.DataAccessException;
import database.RefuelDAO;
import models.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 26/04/14.
 */
public class JDBCRefuelDAO implements RefuelDAO {

    private static final String[] AUTO_GENERATED_KEYS = {"refuel_id"};
    private Connection connection;
    private PreparedStatement createRefuelStatement;
    private PreparedStatement statusRefuelStatement;
    private PreparedStatement deleteRefuelStatement;
    private PreparedStatement getRefuelsForUserStatement;
    private PreparedStatement getRefuelsForOwnerStatement;
    private PreparedStatement getRefuelStatement;
    private PreparedStatement updateRefuelStatement;

    public JDBCRefuelDAO(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getGetRefuelStatement() throws SQLException {
        if (getRefuelStatement == null) {
            getRefuelStatement = connection.prepareStatement("SELECT * FROM Refuels " +
                    "JOIN CarRides ON refuel_car_ride_id = car_ride_car_reservation_id " +
                    "JOIN CarReservations ON refuel_car_ride_id = reservation_id " +
                    "JOIN Cars ON reservation_car_id = car_id " +
                    "JOIN Users ON reservation_user_id = user_id " +
                    "LEFT JOIN FILES ON refuel_file_id = file_id WHERE refuel_id = ? ");
        }
        return getRefuelStatement;
    }

    private PreparedStatement getUpdateRefuelStatement() throws SQLException {
        if (updateRefuelStatement == null) {
            updateRefuelStatement = connection.prepareStatement("UPDATE Refuels SET refuel_file_id = ? , refuel_amount = ? , refuel_status = ?"
                    + " WHERE refuel_id = ?");
        }
        return updateRefuelStatement;
    }

    private PreparedStatement getCreateRefuelStatement() throws SQLException {
        if (createRefuelStatement == null) {
            createRefuelStatement = connection.prepareStatement("INSERT INTO Refuels (refuel_car_ride_id) VALUES (?)", AUTO_GENERATED_KEYS);
        }
        return createRefuelStatement;
    }

    private PreparedStatement getStatusRefuelStatement() throws SQLException {
        if (statusRefuelStatement == null) {
            statusRefuelStatement = connection.prepareStatement("UPDATE Refuels SET refuel_status = ?"
                    + " WHERE refuel_id = ?");
        }
        return statusRefuelStatement;
    }

    private PreparedStatement getDeleteRefuelStatement() throws SQLException {
        if (deleteRefuelStatement == null) {
            deleteRefuelStatement = connection.prepareStatement("DELETE FROM Refuels WHERE refuel_id = ?");
        }
        return deleteRefuelStatement;
    }

    private PreparedStatement getGetRefuelsForUserStatement() throws SQLException {
        if (getRefuelsForUserStatement == null) {
            getRefuelsForUserStatement = connection.prepareStatement("SELECT * FROM Refuels " +
                    "JOIN CarRides ON refuel_car_ride_id = car_ride_car_reservation_id " +
                    "JOIN CarReservations ON refuel_car_ride_id = reservation_id " +
                    "JOIN Cars ON reservation_car_id = car_id " +
                    "JOIN Users ON reservation_user_id = user_id " +
                    "LEFT JOIN Files ON refuel_file_id=file_id WHERE reservation_user_id = ? " +
                    "ORDER BY CASE refuel_status WHEN 'CREATED' THEN 1 WHEN 'REQUEST' THEN 2 WHEN 'REFUSED' THEN 3 " +
                    "WHEN 'ACCEPTED' THEN 4 END");
        }
        return getRefuelsForUserStatement;
    }

    private PreparedStatement getGetRefuelsForOwnerStatement() throws SQLException {
        if (getRefuelsForOwnerStatement == null) {
            getRefuelsForOwnerStatement = connection.prepareStatement("SELECT * FROM Refuels " +
                    "JOIN CarRides ON refuel_car_ride_id = car_ride_car_reservation_id " +
                    "JOIN CarReservations ON refuel_car_ride_id = reservation_id " +
                    "JOIN Cars ON reservation_car_id = car_id " +
                    "JOIN Users ON car_owner_user_id = user_id " +
                    "LEFT JOIN Files ON refuel_file_id=file_id WHERE car_owner_user_id = ? AND refuel_status <> 'CREATED' " +
                    "ORDER BY CASE refuel_status WHEN 'REQUEST' THEN 1 WHEN 'REFUSED' THEN 3 " +
                    "WHEN 'ACCEPTED' THEN 2 END");
        }
        return getRefuelsForOwnerStatement;
    }

    public static Refuel populateRefuel(ResultSet rs) throws SQLException {
        Refuel refuel;
        if(rs.getString("refuel_status").equals("CREATED")){
            refuel = new Refuel(rs.getInt("refuel_id"), JDBCCarRideDAO.populateCarRide(rs), RefuelStatus.valueOf(rs.getString("refuel_status")));
        }else{
            refuel = new Refuel(rs.getInt("refuel_id"), JDBCCarRideDAO.populateCarRide(rs), JDBCFileDAO.populateFile(rs), rs.getBigDecimal("refuel_amount"), RefuelStatus.valueOf(rs.getString("refuel_status")));
        }

        return refuel;
    }



    @Override
    public Refuel createRefuel(CarRide carRide) throws DataAccessException {
        try{
            PreparedStatement ps = getCreateRefuelStatement();
            ps.setInt(1, carRide.getReservation().getId());

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating refuel.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new Refuel(keys.getInt(1), carRide, RefuelStatus.CREATED);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for refuel.", ex);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to create refuel", e);
        }
    }

    @Override
    public void acceptRefuel(int refuelId) throws DataAccessException {
        try {
            PreparedStatement ps = getStatusRefuelStatement();
            ps.setString(1, RefuelStatus.ACCEPTED.toString());
            ps.setInt(2, refuelId);
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("CarCost update affected 0 rows.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to update refuel", e);
        }
    }

    @Override
    public void rejectRefuel(int refuelId) throws DataAccessException {
        try {
            PreparedStatement ps = getStatusRefuelStatement();
            ps.setString(1, RefuelStatus.REFUSED.toString());
            ps.setInt(2, refuelId);
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("CarCost update affected 0 rows.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to update refuel", e);
        }
    }

    @Override
    public void deleteRefuel(int refuelId) throws DataAccessException {
        try {
            PreparedStatement ps = getDeleteRefuelStatement();
            ps.setInt(1, refuelId);
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when deleting refuel.");
        } catch (SQLException e){
            throw new DataAccessException("Could not delete refuel.", e);
        }

    }

    @Override
    public Refuel getRefuel(int refuelId) throws DataAccessException {
        try {
            PreparedStatement ps = getGetRefuelStatement();
            ps.setInt(1, refuelId);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return populateRefuel(rs);
                else return null;
            }catch (SQLException e){
                throw new DataAccessException("Error reading reservation resultset", e);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to get reservation", e);
        }
    }

    @Override
    public void updateRefuel(Refuel refuel) throws DataAccessException {
        try {
            PreparedStatement ps = getUpdateRefuelStatement();
            ps.setInt(1, refuel.getProof().getId());
            ps.setBigDecimal(2, refuel.getAmount());
            ps.setString(3, refuel.getStatus().toString());
            ps.setInt(4, refuel.getId());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Refuel update affected 0 rows.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to update refuel", e);
        }

    }

    @Override
    public List<Refuel> getRefuelsForUser(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getGetRefuelsForUserStatement();
            ps.setInt(1, userId);
            return getRefuelList(ps);
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the list of refuels for user.", e);
        }
    }

    @Override
    public List<Refuel> getRefuelsForOwner(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getGetRefuelsForOwnerStatement();
            ps.setInt(1, userId);
            return getRefuelList(ps);
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the list of refuels for user.", e);
        }
    }

    private List<Refuel> getRefuelList(PreparedStatement ps) throws DataAccessException {
        List<Refuel> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(populateRefuel(rs));
            }
            return list;
        }catch (SQLException e){
            throw new DataAccessException("Error while reading refuel resultset", e);

        }
    }
}
