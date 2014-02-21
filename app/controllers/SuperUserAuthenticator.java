package controllers;

import database.DatabaseHelper;
import models.User;
import models.UserRole;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

/**
 * Created by Benjamin on 21/02/14.
 */
public class SuperUserAuthenticator extends Security.Authenticator {

    @Override
    public String getUsername(Http.Context ctx) {
        User user = DatabaseHelper.getUserProvider().getUser(ctx.session().get("email"));
        if(user.gotRole(UserRole.SUPER_USER))
            return user.getEmail();
        return null;
    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
        return redirect(routes.Application.index());
    }
}