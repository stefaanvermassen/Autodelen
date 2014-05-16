/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import models.Car;
import models.Reservation;
import models.ReservationStatus;
import models.User;
import org.joda.time.DateTime;

import java.util.List;

/**
 *
 * @author Laurent
 */
public interface ReservationDAO {
    public Reservation createReservation(DateTime from, DateTime to, Car car, User user, String message) throws DataAccessException;
    public void updateReservation(Reservation reservation) throws DataAccessException;
    public Reservation getReservation(int id) throws DataAccessException;
    public Reservation getNextReservation(Reservation reservation) throws DataAccessException;
    public Reservation getPreviousReservation(Reservation reservation) throws DataAccessException;
    public void deleteReservation(Reservation reservation) throws DataAccessException;

    public int getAmountOfReservations(Filter filter) throws DataAccessException;
    public List<Reservation> getReservationListForUser(int userID) throws DataAccessException;
    public List<Reservation> getReservationListPage(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;
    public List<Reservation> getReservationListForCar(int carID) throws DataAccessException;
    public int numberOfReservationsWithStatus(ReservationStatus status, int userId, boolean userIsOwner, boolean userIsLoaner);

    public void updateTable();
}
