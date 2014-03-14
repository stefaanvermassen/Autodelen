package database.providers;

import database.DataAccessContext;
import database.DataAccessException;
import database.DataAccessProvider;
import database.UserDAO;
import models.Address;
import models.UserRole;
import play.cache.Cache;
import models.User;

import java.util.EnumSet;

/**
 * Created by Cedric on 2/20/14.
 */
public class UserProvider {

    private static final String USER_BY_EMAIL = "user:email:%s";
    private static final String USER_BY_ID = "user:id:%d";

    private DataAccessProvider provider;

    public UserProvider(DataAccessProvider provider) {
        this.provider = provider;
    }

    public User getUser(String email) throws DataAccessException {
        return getUser(email, true);
    }

    public User getUser(int userId, boolean cached) {
       /* String key = String.format(USER_BY_ID, userId);

        Object obj = null;
        if (cached) {
            obj = Cache.get(key);
        }

        if (obj == null || !(obj instanceof User)) {
            try (DataAccessContext context = provider.getDataAccessContext()) {
                UserDAO dao = context.getUserDAO();
                User user = dao.getUser(userId);

                if (user != null) { // cache and return
                    Cache.set(key, user);
                    return user;
                } else {
                    return null;
                }
            } catch (DataAccessException ex) {
                throw ex; //TODO: log
            }
        } else {
            return (User) obj;
        }*/
        throw new RuntimeException("Not implemented yet. Unify with email (use same objects)");
    }

    public User getUser(int userId) throws DataAccessException {
        return getUser(userId, true);
    }

    public void invalidateUser(String email) {
        Cache.remove(String.format(USER_BY_EMAIL, email));
    }

    public User getUser(String email, boolean cached) throws DataAccessException {
        if (email == null) {
            return null;
        }

        String key = String.format(USER_BY_EMAIL, email);

        Object obj = null;
        if (cached) {
            obj = Cache.get(key);
        }

        if (obj == null || !(obj instanceof User)) {
            try (DataAccessContext context = provider.getDataAccessContext()) {
                UserDAO dao = context.getUserDAO();
                User user = dao.getUser(email);
                if (user != null) { // cache and return
                    Cache.set(key, user);
                    return user;
                } else {
                    return null;
                }
            } catch (DataAccessException ex) {
                throw ex; //TODO: log
            }
        } else {
            return (User) obj;
        }
    }

    public void updateUser(User user) throws DataAccessException {
        try (DataAccessContext context = provider.getDataAccessContext()) {
            invalidateUser(user.getEmail());
            UserDAO dao = context.getUserDAO();
            dao.updateUser(user);
            context.commit();
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    public void deleteUser(User user) throws DataAccessException {
        try (DataAccessContext context = provider.getDataAccessContext()) {
            invalidateUser(user.getEmail());
            UserDAO dao = context.getUserDAO();
            dao.deleteUser(user);
            context.commit();
        } catch (DataAccessException ex) {
            throw ex;
        }

    }
}
