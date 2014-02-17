package controllers;

/**
 * Created by Cedric on 2/16/14.
 */
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
}
