package controllers;

import controllers.Security.RoleSecured;
import database.*;
import models.Address;
import models.User;
import models.UserRole;
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
            if(email == null || email.length() < 5)
                return "Emailadres ontbreekt";
            else if(password == null || password.length() == 0)
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
        public String phone;

        // Address fields
        public String address_city;
        public String address_zip;
        public String address_street;
        public String address_number;
        public String address_bus;

        public String validate() {
            //TODO: check valid email format, valid name etc etc
            if (password == null || password.length() < 8)
                return "Wachtwoord moet minstens 8 tekens bevatten.";
            else if(!password.equals(password_repeat))
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
    @RoleSecured.RoleAuthenticated(value = {UserRole.ADMIN, UserRole.SUPER_USER})
    public static Result login() {
        // Allow a force login when the user doesn't exist anymore
        String email = session("email");
        if(email != null){
            if(DatabaseHelper.getUserProvider().getUser(email, false) == null) { // check if user really exists (not from cache)
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
                AddressDAO adao = context.getAddressDAO();
                try {
                    Address address = adao.createAddress(registerForm.get().address_zip, registerForm.get().address_city, registerForm.get().address_street, registerForm.get().address_number, registerForm.get().address_bus);
                    User user = dao.createUser(registerForm.get().email, hashPassword(registerForm.get().password), registerForm.get().firstName, registerForm.get().lastName, registerForm.get().phone,address);
                    context.commit();

                    session("email", user.getEmail());
                    return redirect(
                            routes.Application.index() // return to index page, registration success
                    );
                } catch(DataAccessException ex){
                    context.rollback();
                    throw ex;
                }
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

