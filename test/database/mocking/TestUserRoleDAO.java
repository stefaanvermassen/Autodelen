package database.mocking;

import database.DataAccessException;
import database.UserDAO;
import database.UserRoleDAO;
import models.User;
import models.UserRole;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Cedric on 3/8/14.
 */
public class TestUserRoleDAO implements UserRoleDAO {


    private Map<Integer, EnumSet<UserRole>> roles;
    private UserDAO userDao;

    public TestUserRoleDAO(UserDAO userDao) {
        roles = new HashMap<>();
        this.userDao = userDao;
    }

    @Override
    public EnumSet<UserRole> getUserRoles(int userId) throws DataAccessException {
        EnumSet<UserRole> r = roles.get(userId);
        if (r == null) {
            if (userDao.getUser(userId, false) != null) {
                return EnumSet.of(UserRole.USER);
            } else {
                return EnumSet.noneOf(UserRole.class);
            }
        } else {
            r = EnumSet.copyOf(r);
            r.add(UserRole.USER);
            return r;
        }
    }

    @Override
    public List<User> getUsersByRole(UserRole userRole) throws DataAccessException {
        return null;
    }

    @Override
    public void addUserRole(int userId, UserRole role) throws DataAccessException {
        EnumSet<UserRole> r = roles.get(userId);
        if (r == null) {
            roles.put(userId, EnumSet.of(role));
        } else {
            r.add(role);
        }
    }

    @Override
    public void removeUserRole(int userId, UserRole role) throws DataAccessException {
        EnumSet<UserRole> r = roles.get(userId);
        if (r == null) {
            throw new DataAccessException("User did not have this role");
        } else {
            r.remove(role);
        }
    }
}
