package controllers;

import controllers.Security.RoleSecured;
import database.*;
import models.User;
import models.VerificationType;
import play.data.*;

import views.html.login.*;

import play.mvc.*;
import org.mindrot.jbcrypt.BCrypt;


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
            if (email == null || email.length() < 5)
                return "Emailadres ontbreekt";
            else if (password == null || password.length() == 0)
                return "Wachtwoord ontbreekt";
            else if (checkLoginModel(this)) {
                return null;
            } else return "Foute gebruikersnaam of wachtwoord.";
        }
    }

    public static class RegisterModel {
        public String email;
        public String password;
        public String password_repeat;
        public String firstName;
        public String lastName;

        public String validate() {
            //TODO: check valid email format, valid name etc etc
            if (password == null || password.length() < 8)
                return "Wachtwoord moet minstens 8 tekens bevatten.";
            else if (!password.equals(password_repeat))
                return "Wachtwoord komt niet overeen.";
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
        // Allow a force login when the user doesn't exist anymore
        String email = session("email");
        if (email != null) {
            if (DatabaseHelper.getUserProvider().getUser(email, false) == null) { // check if user really exists (not from cache)
                session().clear();
                email = null;
            }
        }

        if (email == null) {
            return ok(
                    login.render(Form.form(LoginModel.class))
            );
        } else {
            return redirect(
                    routes.Dashboard.index()
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
                    routes.Dashboard.index() // go to dashboard page, authentication success
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
                    routes.Login.login()
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
        //TODO: email verification

        Form<RegisterModel> registerForm = Form.form(RegisterModel.class).bindFromRequest();
        if (registerForm.hasErrors()) {
            return badRequest(register.render(registerForm));
        } else {
            session().clear();
            User otherUser = DatabaseHelper.getUserProvider().getUser(registerForm.get().email);
            if (otherUser != null) {
                registerForm.error("Er bestaat reeds een gebruiker met dit emailadres.");
                return badRequest(register.render(registerForm));
            } else {
                try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                    UserDAO dao = context.getUserDAO();
                    try {
                        User user = dao.createUser(registerForm.get().email, hashPassword(registerForm.get().password),
                                registerForm.get().firstName, registerForm.get().lastName);


                        // Now we create a registration UUID
                        String verificationIdent = dao.createVerificationString(user, VerificationType.REGISTRATION); //TODO: send this in an email

                        context.commit();

                        session("email", user.getEmail());
                        return redirect(
                                routes.Application.index() // return to index page, registration success
                        );
                    } catch (DataAccessException ex) {
                        context.rollback();
                        throw ex;
                    }
                } catch (DataAccessException ex) {
                    throw ex;
                }
            }
        }
    }


    /**
     * Method: GET
     * Logs the user out
     *
     * @return Redirect to index page
     */
    @RoleSecured.RoleAuthenticated()
    public static Result logout() {
        User user = DatabaseHelper.getUserProvider().getUser(session("email"));
        DatabaseHelper.getUserProvider().invalidateUser(session("email"));
        DatabaseHelper.getUserRoleProvider().invalidateRoles(user.getId());

        session().clear();
        return redirect(
                routes.Application.index()
        );
    }

}

