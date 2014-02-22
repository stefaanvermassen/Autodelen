/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import models.Location;

/**
 *
 * @author Laurent
 */
public interface LocationDAO {
    public Location createLocation(int zip, String location) throws DataAccessException;
    public void updateLocation(Location location) throws DataAccessException;
    public Location getLocation(int zip) throws DataAccessException;
}
