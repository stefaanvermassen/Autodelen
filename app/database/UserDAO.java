package database;

import models.Address;
import models.User;

/**
 * Created by Cedric on 2/16/14.
 */
public interface UserDAO {
    public User getUser(String email) throws DataAccessException;
    public void updateUser(User user) throws DataAccessException;
    public User createUser(String email, String password, String firstName, String lastName, String phone, Address address) throws DataAccessException;
}
