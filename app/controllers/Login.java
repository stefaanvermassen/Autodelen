package controllers;

import play.*;
import play.data.*;
import static play.data.Form.*;
import play.mvc.*;

import views.html.*;


/**
 * Created by Cedric on 2/16/14.
 */
public class Login extends Controller {

    public static class LoginModel {
        public String email;
        public String password;

        public String validate() {
            if("test".equals(email) && "test".equals(password))
                return null;
            else
                return "Foute gebruikersnaam of wachtwoord!";
        }
    }

    /**
     * Method: GET
     * Returns the login form
     * @return The login index page
     */
    public static Result login() {
        return ok(
                login.render(Form.form(LoginModel.class))
        );
    }

    /**
     * Method: POST
     * Processes the form data
     * @return Redirect to old page or login form
     */
    public static Result authenticate() {
        Form<LoginModel> loginForm = Form.form(LoginModel.class).bindFromRequest();
        if (loginForm.hasErrors()) {
            return badRequest(login.render(loginForm));
        } else {
            session().clear();
            session("email", loginForm.get().email);
            return redirect(
                    routes.Application.index() // return to index page, authentication success
            );
        }
    }

    /**
     * Method: GET
     * Logs the user out
     * @return Redirect to indexpage
     */
    public static Result logout() {

    }

}

