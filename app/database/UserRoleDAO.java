package database;

import java.util.EnumSet;
import java.util.List;

import models.User;
import models.UserRole;

public interface UserRoleDAO {
	public EnumSet<UserRole> getUserRoles(int userId) throws DataAccessException;
    public List<User> getUsersByRole(UserRole userRole) throws DataAccessException;
	public void addUserRole(int userId, UserRole role) throws DataAccessException;
	public void removeUserRole(int userId, UserRole role) throws DataAccessException;
}
