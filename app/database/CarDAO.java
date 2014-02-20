/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import models.Car;
import models.User;

/**
 *
 * @author Laurent
 */
public interface CarDAO {
    
    public Car createCar(String type, String brand, User owner, int doors, int year) throws DataAccessException;
}
