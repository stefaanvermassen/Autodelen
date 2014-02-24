package controllers;

/**
 * Created by Cedric on 2/16/14.
 */
import database.DatabaseHelper;
import models.User;
import models.UserRole;
import play.*;
import play.mvc.*;
import play.mvc.Http.*;

public class Secured extends Security.Authenticator {

    @Override
    public String getUsername(Context ctx) {
        return ctx.session().get("email");
    }

    @Override
    public Result onUnauthorized(Context ctx) {
        return redirect(routes.Login.login());
    }

    // Access rights
    public static boolean isAuthorized(UserRole role) {
        User user = (User) DatabaseHelper.getUserProvider().getUser(Context.current().session().get("email"));
        if(user == null)
            return false;
        return user.gotRole(role);
    }
}
