package controllers;

import database.*;
import database.jdbc.JDBCDataAccessProvider;
import models.User;
import play.*;
import play.data.*;

import static play.data.Form.*;

import play.mvc.*;
import org.mindrot.jbcrypt.BCrypt;

import views.html.*;


/**
 * Created by Cedric on 2/16/14.
 */
public class Login extends Controller {

    private static boolean checkLoginModel(LoginModel model) {
        User user = DatabaseHelper.getUserProvider().getUser(model.email);
        return user != null && BCrypt.checkpw(model.password, user.getPassword());
    }

    public static class LoginModel {
        public String email;
        public String password;

        public String validate() {
            if (checkLoginModel(this)) {
                return null;
            } else return "Foute gebruikersnaam of wachtwoord.";
        }
    }

    public static class RegisterModel {
        public String email;
        public String password;
        public String firstName;
        public String lastName;

        public String validate() {
            //TODO: check valid email format, valid name etc etc
            if (password.length() < 8)
                return "Wachtwoord moet minstens 8 tekens bevatten.";
            else
                return null;
        }
    }

    /**
     * Method: GET
     * Returns the login form
     *
     * @return The login index page
     */
    public static Result login() {
        if (session("email") == null) {
            return ok(
                    login.render(Form.form(LoginModel.class))
            );
        } else {
            return redirect(
                    routes.Application.index()
            );
        }
    }

    /**
     * Method: POST
     * Processes the form data
     *
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
     *
     * @return Page to register to
     */
    public static Result register() {
        if (session("email") == null) {
            return ok(
                    register.render(Form.form(RegisterModel.class))
            );
        } else {
            return redirect(
                    routes.Application.index()
            );
        }
    }

    private static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    /**
     * Method: POST
     *
     * @return Redirect and logged in session if success
     */
    public static Result register_process() {
        //TODO: captcha

        Form<RegisterModel> registerForm = Form.form(RegisterModel.class).bindFromRequest();
        if (registerForm.hasErrors()) {
            return badRequest(register.render(registerForm));
        } else {
            session().clear();
            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                UserDAO dao = context.getUserDAO();
                User user = dao.createUser(registerForm.get().email, hashPassword(registerForm.get().password), registerForm.get().firstName, registerForm.get().lastName);
                session("email", user.getEmail());
                return redirect(
                        routes.Application.index() // return to index page, registration success
                );
            } catch (DataAccessException ex) {
                //TODO: send fail message
                throw ex;
            }
        }
    }


    /**
     * Method: GET
     * Logs the user out
     *
     * @return Redirect to index page
     */
    @Security.Authenticated(Secured.class)
    public static Result logout() {
        DatabaseHelper.getUserProvider().invalidateUser(session("email"));
        
        session().clear();
        return redirect(
                routes.Application.index()
        );
    }

}

