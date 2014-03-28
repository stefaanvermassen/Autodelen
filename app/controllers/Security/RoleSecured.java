package controllers.Security;

import controllers.routes;
import database.DatabaseHelper;
import database.providers.UserRoleProvider;
import models.User;
import models.UserRole;
import models.UserStatus;
import play.libs.F;
import play.mvc.*;
import play.mvc.Http.*;

import java.lang.annotation.*;
import java.util.EnumSet;
import java.util.Set;

/**
 * Created by Benjamin on 26/02/14.
 */
public class RoleSecured {

    /**
     * Creation of the annotation interface.
     * Use of the RoleAuthorization class for action composition.
     */
    @With(RoleAuthorizationAction.class)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RoleAuthenticated {
        UserRole[] value() default {UserRole.USER};
    }

    /**
     * Action allowing authentication of user by role.
     * The username is retrieved from the session cookie and used to
     * determine the user role.
     */
    public static class RoleAuthorizationAction extends Action<RoleAuthenticated> {

        public F.Promise<SimpleResult> call(Context ctx) {
            try {
                UserRole[] securedRoles = configuration.value();
                User user = DatabaseHelper.getUserProvider().getUser(ctx.session(), true); // get user from session

                // If user is null, redirect to login page
                if(user == null) {
                    return F.Promise.pure(redirect(routes.Login.login(ctx.request().path())));
                } else if((user.getStatus() == UserStatus.BLOCKED || user.getStatus() == UserStatus.DROPPED || user.getStatus() == UserStatus.EMAIL_VALIDATING))
                {
                    ctx.flash().put("danger", "Deze account is not niet geactiveerd of geblokkeerd.");
                    return F.Promise.pure(redirect(routes.Login.login(ctx.request().path())));
                }

                Set<UserRole> roles = DatabaseHelper.getUserRoleProvider().getRoles(user.getId(), true); // cached instance

                // If user has got one of the specified roles, delegate to the requested page
                if(securedRoles.length == 0)
                    return delegate.call(ctx);
                else {
                    for(UserRole securedRole : securedRoles) {
                        if(UserRoleProvider.hasRole(roles, securedRole)){ // This also takes care of SU = has all roles logic
                            return delegate.call(ctx);
                        }
                    }
                }
                // User is not authorized
                return F.Promise.pure((SimpleResult) unauthorized(views.html.unauthorized.render(securedRoles)));
            }
            catch(Throwable t) {
               throw new RuntimeException(t);
            }
        }

    }
}
