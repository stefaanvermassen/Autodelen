/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database.jdbc;

import database.DataAccessException;
import database.Filter;
import database.FilterField;
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

    public static final String RESERVATION_QUERY = "SELECT * FROM CarReservations " +
            "INNER JOIN Cars ON CarReservations.reservation_car_id = Cars.car_id " +
            "INNER JOIN Users ON CarReservations.reservation_user_id = Users.user_id ";

    public static final String FILTER_FRAGMENT = " WHERE (car_owner_user_id=? OR reservation_user_id=? ) ";

    public static final String MATCH_PASSED = " reservation_status != 'ACCEPTED' AND reservation_status != 'REQUEST' ";

    public static final String MATCH_STATUS = " reservation_status = ? ";

    private void fillFragment(PreparedStatement ps, Filter filter, int start, boolean matchStatus) throws SQLException {
        if(filter == null) {
            // getFieldContains on a "empty" filter will return the default string "%%", so this does not filter anything
            filter = new JDBCFilter();
        }
        ps.setString(start, filter.getFieldIs(FilterField.RESERVATION_USER_OR_OWNER_ID));
        ps.setString(start+1, filter.getFieldIs(FilterField.RESERVATION_USER_OR_OWNER_ID));
        if(matchStatus)
            ps.setString(start+2, filter.getFieldIs(FilterField.RESERVATION_STATUS));
    }

    private Connection connection;
    private PreparedStatement createReservationStatement;
    private PreparedStatement updateReservationStatement;
    private PreparedStatement getReservationStatement;
    private PreparedStatement deleteReservationStatement;
    private PreparedStatement getReservationListByCaridStatement;
    private PreparedStatement getGetReservationListPageByFromAscStatement;
    private PreparedStatement getGetReservationListPageByFromDescStatement;
    private PreparedStatement getGetAmountOfReservationsStatement;

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

    private PreparedStatement getGetReservationListPageByFromAscStatement() throws SQLException {
        if(getGetReservationListPageByFromAscStatement == null) {
            getGetReservationListPageByFromAscStatement = connection.prepareStatement(RESERVATION_QUERY + FILTER_FRAGMENT + "ORDER BY reservation_from asc LIMIT ?, ?");
        }
        return getGetReservationListPageByFromAscStatement;
    }
    private PreparedStatement getGetReservationListPageByFromDescStatement() throws SQLException {
        if(getGetReservationListPageByFromDescStatement == null) {
            getGetReservationListPageByFromDescStatement = connection.prepareStatement(RESERVATION_QUERY + FILTER_FRAGMENT + "ORDER BY reservation_from desc LIMIT ?, ?");
        }
        return getGetReservationListPageByFromDescStatement;
    }

    private PreparedStatement getGetReservationListByCaridStatement() throws SQLException {
        if (getReservationListByCaridStatement == null) {
            // Only request the reservations for which the current user is the loaner or the owner
            getReservationListByCaridStatement = connection.prepareStatement("SELECT * FROM CarReservations INNER JOIN Cars ON CarReservations.reservation_car_id = Cars.car_id INNER JOIN Users ON CarReservations.reservation_user_id = Users.user_id " +
                    "WHERE car_id=?");
        }
        return getReservationListByCaridStatement ;
    }

    private PreparedStatement getGetAmountOfReservationsStatement() throws SQLException {
        if(getGetAmountOfReservationsStatement == null) {
            getGetAmountOfReservationsStatement = connection.prepareStatement("SELECT COUNT(reservation_id) AS amount_of_reservations FROM CarReservations " +
                    "INNER JOIN Cars ON CarReservations.reservation_car_id = Cars.car_id " +
                    "INNER JOIN Users ON CarReservations.reservation_user_id = Users.user_id " + FILTER_FRAGMENT);
        }
        return getGetAmountOfReservationsStatement;
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

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating reservation.");
            
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
                if(rs.next())
                    return populateReservation(rs);
                else return null;
            }catch (SQLException e){
                throw new DataAccessException("Error reading reservation resultset", e);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to get reservation", e);
        }
    }
    
    @Override
    public void deleteReservation(Reservation reservation){
    	try {
			PreparedStatement ps = getDeleteReservationStatement();
			ps.setInt(1, reservation.getId());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when deleting reservation.");
		} catch (SQLException ex){
			throw new DataAccessException("Could not delete reservation",ex);
		}
    }

    @Override
    public int getAmountOfReservations(Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = getGetAmountOfReservationsStatement();
            fillFragment(ps, filter, 1, false);

            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return rs.getInt("amount_of_reservations");
                else return 0;

            } catch (SQLException ex) {
                throw new DataAccessException("Error reading count of reservations", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of reservations", ex);
        }
    }

    @Override
    public int numberOfReservationsWithStatus(ReservationStatus status) {
        try {
            String statement = "SELECT COUNT(*) as result FROM CarReservations " +
                    "WHERE CarReservations.reservation_status = '" + status + "'";
            PreparedStatement ps = connection.prepareStatement(statement);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return rs.getInt("result");
                else return 0;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading count of reservations", ex);
            }
        } catch(SQLException ex) {
            throw new DataAccessException("Could not count number of reservations");
        }
    }

    @Override
    public List<Reservation> getReservationListPage(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        try {
            boolean matchStatus = true;
            String statement = RESERVATION_QUERY + FILTER_FRAGMENT + "AND";
            if("PASSED".equals(filter.getFieldIs(FilterField.RESERVATION_STATUS))) {
                statement += MATCH_PASSED;
                matchStatus = false;
            }
            else
                statement += MATCH_STATUS;
            if("".equals(filter.getFieldIs(FilterField.RESERVATION_STATUS)))
                filter.fieldIs(FilterField.RESERVATION_STATUS, ReservationStatus.ACCEPTED.toString());
            switch(orderBy) {
                // TODO: get some other things to sort on
                default:
                    statement += asc ? "ORDER BY reservation_from asc" : "ORDER BY reservation_from desc";
                    break;
            }
            int first = (page-1)*pageSize;
            PreparedStatement ps = connection.prepareStatement(statement + " LIMIT " + first + ", " + pageSize);
            if(ps == null) {
                throw new DataAccessException("Could not create getReservationList statement");
            }

            fillFragment(ps, filter, 1, matchStatus);
            return getReservationList(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of cars", ex);
        }
    }

    @Override
    public List<Reservation> getReservationListForCar(int carId) throws DataAccessException {
        try {
            PreparedStatement ps = getGetReservationListByCaridStatement();
            ps.setInt(1, carId);
            return getReservationList(ps);
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the list of reservations", e);
        }
    }

    private List<Reservation> getReservationList(PreparedStatement ps) throws DataAccessException {
        List<Reservation> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(populateReservation(rs));
            }
            return list;
        }catch (SQLException e){
            throw new DataAccessException("Error while reading reservation resultset", e);
        }
    }

    @Override
    public void updateTable() {
        try {
            String statement = "SELECT reservation_id, reservation_status FROM CarReservations WHERE CarReservations.reservation_to < NOW() " +
                    "AND CarReservations.reservation_status = 'ACCEPTED'";
            PreparedStatement ps = connection.prepareStatement(statement);
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    ReservationStatus status = ReservationStatus.valueOf(rs.getString("reservation_status"));
                    System.out.println(status);
                    PreparedStatement update =
                             connection.prepareStatement("UPDATE CarReservations SET reservation_status = ? WHERE reservation_id = ? ");
                    update.setString(1, ReservationStatus.REQUEST_DETAILS.toString());
                    update.setInt(2, rs.getInt("reservation_id"));
                    if(update.executeUpdate() == 0)
                        throw new DataAccessException("Error while updating the reservations table");
                }
            } catch (SQLException ex) {
                throw new DataAccessException("Error while updating the reservations table", ex);
            }
        } catch(SQLException ex) {
            throw new DataAccessException("Error while updating the reservations table");
        }
    }
}
