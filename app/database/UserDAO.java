package database;

import java.util.EnumSet;

import models.Address;
import models.User;
import models.UserRole;
import models.VerificationType;

/**
 * Created by Cedric on 2/16/14.
 */
public interface UserDAO {
    public User getUser(String email) throws DataAccessException;
    public User getUser(int userId, boolean withRest) throws DataAccessException;
    public void updateUser(User user, boolean withRest) throws DataAccessException;
	public void deleteUser(User user) throws DataAccessException;
    public User createUser(String email, String password, String firstName, String lastName) throws DataAccessException;
    public String getVerificationString(User user, VerificationType type) throws DataAccessException;
    public String createVerificationString(User user, VerificationType type) throws DataAccessException;
    public void deleteVerificationString(User user, VerificationType type) throws DataAccessException;
}
