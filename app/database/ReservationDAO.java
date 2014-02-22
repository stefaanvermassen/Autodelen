/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import models.Car;
import models.Reservation;
import models.User;

/**
 *
 * @author Laurent
 */
public interface ReservationDAO {
    public Reservation createReservation(String from, String to, Car car, User user) throws DataAccessException;
    public void updateReservation(Reservation reservation) throws DataAccessException;
    public Reservation getReservation(int id) throws DataAccessException;
}
