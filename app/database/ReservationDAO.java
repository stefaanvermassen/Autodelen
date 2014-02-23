/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import models.Car;
import models.Reservation;
import models.User;
import org.joda.time.DateTime;

/**
 *
 * @author Laurent
 */
public interface ReservationDAO {
    public Reservation createReservation(DateTime from, DateTime to, Car car, User user) throws DataAccessException;
    public void updateReservation(Reservation reservation) throws DataAccessException;
    public Reservation getReservation(int id) throws DataAccessException;
}
