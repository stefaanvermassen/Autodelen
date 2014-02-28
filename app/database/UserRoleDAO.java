package database;

import java.util.EnumSet;

import models.UserRole;

public interface UserRoleDAO {
	public EnumSet<UserRole> getUserRoles(int userId) throws DataAccessException;
	public void addUserRole(int userId, UserRole role) throws DataAccessException;
	public void removeUserRole(int userId, UserRole role) throws DataAccessException;
}
