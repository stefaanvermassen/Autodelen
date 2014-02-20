package database.cache.EHCache;

import database.DataAccessException;
import database.UserDAO;
import database.cache.CacheWrapper;
import database.cache.EHCache.EHCacheWrapper;
import models.User;
import net.sf.ehcache.CacheManager;

import java.util.Random;

/**
 * Created by Benjamin on 19/02/14.
 */
public class UserCachedProxy implements UserDAO {

    private UserDAO target;
    private static final CacheWrapper<String, User> cache = new EHCacheWrapper<>("EHCache", CacheManager.create());

    public UserCachedProxy(UserDAO userdao) {
        target = userdao;
    }

    public User getUser(String email) throws DataAccessException {
        User u = cache.get(email);
        if(u != null) {
            return u;
        }
        return target.getUser(email);
    }

    public User createUser(String email, String password, String firstName, String lastName) throws DataAccessException {
        User u = target.createUser(email, password, firstName, lastName);
        cache.put(email, u);
        return u;
    }


}
