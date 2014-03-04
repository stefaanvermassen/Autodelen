package database.providers;

import database.*;
import models.User;
import models.UserRole;
import play.cache.Cache;

import java.util.EnumSet;

/**
 * Created by Cedric on 3/4/14.
 */
public class UserRoleProvider {

    private static final String ROLES_BY_ID = "role:id:%d";

    private DataAccessProvider provider;
    private UserProvider userProvider;

    public UserRoleProvider(DataAccessProvider provider, UserProvider userProvider){
        this.provider = provider;
        this.userProvider = userProvider;
    }

    public EnumSet<UserRole> getRoles(int userId){
        return getRoles(userId, true);
    }

    //TODO: leave this helper function here, or move to the main databasehelper? Decoupling...
    public boolean hasRole(String email, UserRole role){
        User user = userProvider.getUser(email);
        if(user == null)
            return false;
        else {
            return getRoles(user.getId()).contains(role);
        }
    }

    public EnumSet<UserRole> getRoles(int userId, boolean cached){

        String key = String.format(ROLES_BY_ID, userId);

        Object obj = null;
        if (cached) {
            obj = Cache.get(key);
        }

        if (obj == null || !(obj instanceof EnumSet)) {
            try (DataAccessContext context = provider.getDataAccessContext()) {
                UserRoleDAO dao = context.getUserRoleDAO();
                EnumSet<UserRole> roles = dao.getUserRoles(userId);
                if (roles != null) { // cache and return
                    Cache.set(key, roles);
                    return roles;
                } else {
                    return null;
                }
            } catch (DataAccessException ex) {
                throw ex;
            }
        } else {
            return (EnumSet<UserRole>)obj;
        }
    }

    public void invalidateRoles(int userId){
        Cache.remove(String.format(ROLES_BY_ID, userId));
    }
}
