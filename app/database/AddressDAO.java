package database;

import models.Address;

/**
 * Created by Cedric on 2/21/14.
 */
public interface AddressDAO {
    public Address getAddress(int id) throws DataAccessException;
    public Address createAddress(String zip, String city, String street, String number, String bus) throws DataAccessException;
}
