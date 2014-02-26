package controllers;

import database.DatabaseHelper;
import models.User;
import models.UserRole;
import play.libs.F;
import play.mvc.*;
import play.mvc.Http.*;

import java.lang.annotation.*;

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
                UserRole[] SecuredRoles = configuration.value();
                User user = DatabaseHelper.getUserProvider().getUser(ctx.session().get("email"));
                if(user == null)
                    return F.Promise.pure((SimpleResult) unauthorized(views.html.defaultpages.unauthorized.render()));
                for(UserRole securedRole : SecuredRoles) {
                    if(user.gotRole(securedRole))
                        return delegate.call(ctx);
                }
                return F.Promise.pure((SimpleResult) unauthorized(views.html.defaultpages.unauthorized.render()));
            }
            catch(Throwable t) {
                throw new RuntimeException(t);
            }

        }

    }
}
