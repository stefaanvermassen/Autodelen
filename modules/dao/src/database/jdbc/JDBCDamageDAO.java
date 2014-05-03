package database.jdbc;

import database.DamageDAO;
import database.DataAccessException;
import models.CarRide;
import models.Damage;
import models.FileGroup;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 02/05/14.
 */
public class JDBCDamageDAO implements DamageDAO {

    private static final String[] AUTO_GENERATED_KEYS = {"damage_id"};
    private Connection connection;
    private PreparedStatement createDamageStatement;
    private PreparedStatement getUnfinishedDamagesStatement;
    private PreparedStatement getFinishedDamagesStatement;
    private PreparedStatement deleteDamageStatement;
    private PreparedStatement getDamageStatement;
    private PreparedStatement getUserDamagesStatement;
    private PreparedStatement updateDamageStatement;
    private static final String DAMAGE_QUERY = "SELECT * FROM Damages " +
            "JOIN CarRides ON damage_car_ride_id = car_ride_car_reservation_id " +
            "JOIN CarReservations ON damage_car_ride_id = reservation_id " +
            "LEFT JOIN FileGroups ON damage_filegroup_id = file_group_id " +
            "JOIN Cars ON reservation_car_id = car_id " +
            "JOIN Users ON reservation_user_id = user_id ";

    public JDBCDamageDAO(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getGetDamageStatement() throws SQLException {
        if (getDamageStatement == null) {
            getDamageStatement = connection.prepareStatement(DAMAGE_QUERY +
                    "WHERE damage_id = ?");
        }
        return getDamageStatement;
    }

    private PreparedStatement getCreateDamageStatement() throws SQLException {
        if (createDamageStatement == null) {
            createDamageStatement = connection.prepareStatement("INSERT INTO Damages " +
                    "(damage_car_ride_id, damage_time) VALUES(?, ?)", AUTO_GENERATED_KEYS);
        }
        return createDamageStatement;
    }

    private PreparedStatement getDeleteDamageStatement() throws SQLException {
        if (deleteDamageStatement == null) {
            deleteDamageStatement = connection.prepareStatement("DELETE FROM Damages WHERE damage_id = ?");
        }
        return deleteDamageStatement;
    }

    private PreparedStatement getGetUnfinishedDamagesStatement() throws SQLException {
        if (getUnfinishedDamagesStatement == null) {
            getUnfinishedDamagesStatement = connection.prepareStatement(DAMAGE_QUERY +
                    "WHERE damage_finished = 0");
        }
        return getUnfinishedDamagesStatement;
    }

    private PreparedStatement getGetFinishedDamagesStatement() throws SQLException {
        if (getFinishedDamagesStatement == null) {
            getFinishedDamagesStatement = connection.prepareStatement(DAMAGE_QUERY +
                    "WHERE damage_finished = 1");
        }
        return getFinishedDamagesStatement;
    }

    private PreparedStatement getGetUserDamagesStatement() throws SQLException {
        if (getUserDamagesStatement == null) {
            getUserDamagesStatement = connection.prepareStatement(DAMAGE_QUERY +
                    "WHERE reservation_user_id = ?");
        }
        return getUserDamagesStatement;
    }

    private PreparedStatement getUpdateDamageStatement() throws SQLException {
        if (updateDamageStatement == null) {
            updateDamageStatement = connection.prepareStatement("UPDATE Damages SET damage_car_ride_id = ? , " +
                    "damage_filegroup_id = ? , damage_description = ? , damage_finished = ? , damage_time = ? "
                    + "WHERE damage_id = ?");
        }
        return updateDamageStatement;
    }

    public static Damage populateDamage(ResultSet rs) throws SQLException {
        return new Damage(rs.getInt("damage_id"), JDBCCarRideDAO.populateCarRide(rs), rs.getInt("damage_filegroup_id"),
                rs.getString("damage_description"), new DateTime(rs.getTimestamp("damage_time")),
                rs.getBoolean("damage_finished"));
    }

    @Override
    public Damage createDamage(CarRide carRide) throws DataAccessException {
        try{
            PreparedStatement ps = getCreateDamageStatement();
            ps.setInt(1, carRide.getReservation().getId());
            ps.setTimestamp(2, new Timestamp(carRide.getReservation().getFrom().getMillis()));
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating damage.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new Damage(keys.getInt(1), carRide);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for damage.", ex);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to create damage", e);
        }
    }

    @Override
    public Damage getDamage(int damageId) throws DataAccessException {
        try {
            PreparedStatement ps = getGetDamageStatement();
            ps.setInt(1, damageId);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return populateDamage(rs);
                else return null;
            }catch (SQLException e){
                throw new DataAccessException("Error reading damage resultset", e);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to get damage", e);
        }
    }

    @Override
    public void updateDamage(Damage damage) throws DataAccessException {
        try {
            PreparedStatement ps = getUpdateDamageStatement();
            ps.setInt(1, damage.getCarRide().getReservation().getId());
            ps.setInt(2, damage.getProofId());
            ps.setString(3, damage.getDescription());
            ps.setBoolean(4, damage.getFinished());
            ps.setTimestamp(5, new Timestamp(damage.getTime().getMillis()));
            ps.setInt(6, damage.getId());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Damage update affected 0 rows.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to update damage", e);
        }
    }

    @Override
    public List<Damage> getUnfinishedDamages() throws DataAccessException {
        try {
            PreparedStatement ps = getGetUnfinishedDamagesStatement();
            return getDamageList(ps);
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the list of unfinished damages.", e);
        }
    }

    @Override
    public List<Damage> getFinishedDamages() throws DataAccessException {
        try {
            PreparedStatement ps = getGetFinishedDamagesStatement();
            return getDamageList(ps);
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the list of finished damages.", e);
        }
    }

    @Override
    public List<Damage> getUserDamages(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getGetUserDamagesStatement();
            ps.setInt(1, userId);
            return getDamageList(ps);
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the list of damages for user.", e);
        }
    }

    @Override
    public void deleteDamage(int damageId) {
        try {
            PreparedStatement ps = getDeleteDamageStatement();
            ps.setInt(1, damageId);
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when deleting damage.");
        } catch (SQLException e){
            throw new DataAccessException("Could not delete damage.", e);
        }
    }

    private List<Damage> getDamageList(PreparedStatement ps) throws DataAccessException {
        List<Damage> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(populateDamage(rs));
            }
            return list;
        }catch (SQLException e){
            throw new DataAccessException("Error while reading damage resultset", e);

        }
    }
}
