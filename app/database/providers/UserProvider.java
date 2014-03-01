package database.providers;

import database.DataAccessContext;
import database.DataAccessException;
import database.DataAccessProvider;
import database.UserDAO;
import models.Address;
import play.cache.Cache;
import models.User;

/**
 * Created by Cedric on 2/20/14.
 */
public class UserProvider implements UserDAO {

    private static final String USER_BY_EMAIL = "user:email:%s";

    private DataAccessProvider provider;

    public UserProvider(DataAccessProvider provider) {
        this.provider = provider;
    }

    @Override
    public User getUser(String email) throws DataAccessException {
        return getUser(email, true);
    }

    @Override
    public User getUser(int userId) throws DataAccessException {
        throw new RuntimeException("Users aren't cached by ID (yet?)");
    }

    @Override
    public User createUser(String email, String password, String firstName, String lastName) throws DataAccessException {
        String key = String.format(USER_BY_EMAIL, email);
        try (DataAccessContext context = provider.getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();
            User user = dao.createUser(email, password, firstName, lastName);
            if (user != null) { // cache and return
                Cache.set(key, user);
                return user;
            } else {
                return null;
            }
        } catch (DataAccessException ex) {
            return null; //TODO: log
        }
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

    @Override
    public void updateUser(User user) throws DataAccessException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
