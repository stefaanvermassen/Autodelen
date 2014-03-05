package controllers;

import controllers.Security.RoleSecured;
import database.DataAccessContext;
import database.DataAccessException;
import database.DatabaseHelper;
import database.UserDAO;
import models.User;
import models.UserStatus;
import models.VerificationType;
import notifiers.Mail;
import org.mindrot.jbcrypt.BCrypt;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.login.*;


/**
 * Created by Cedric on 2/16/14.
 */
public class Login extends Controller {

    public static class LoginModel {
        public String email;
        public String password;

        public String validate() {
            if (email == null || email.length() < 5)
                return "Emailadres ontbreekt";
            else if (password == null || password.length() == 0)
                return "Wachtwoord ontbreekt";
            else return null;
        }
    }

    public static class PasswordResetModel {
        public String password;
        public String password_repeat;

        public String validate() {
            if (password == null || !password.equals(password_repeat))
                return "Wachtwoorden komen niet overeen";
            else return null;
        }
    }

    public static class PasswordResetRequestModel {
        public String email;

        public String validate() {
            return null; //TODO regex check?
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
     * Method: GET
     *
     * @return
     */
    public static Result resetPasswordRequest() {
        return ok(pwresetrequest.render(Form.form(PasswordResetRequestModel.class)));
    }

    /**
     * Method: POST
     *
     * @return
     */
    public static Result resetPasswordRequestProcess() {
        Form<PasswordResetRequestModel> resetForm = Form.form(PasswordResetRequestModel.class).bindFromRequest();
        if (resetForm.hasErrors()) {
            return badRequest(pwresetrequest.render(resetForm));
        } else {
            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                UserDAO dao = context.getUserDAO();
                User user = dao.getUser(resetForm.get().email);
                if (user == null) {
                    resetForm.reject("Gebruiker met dit adres bestaat niet.");
                    return badRequest(pwresetrequest.render(resetForm));
                } else {
                    try {
                        //TODO: this check should be implicit?
                        if (dao.getVerificationString(user, VerificationType.PWRESET) != null) {
                            dao.deleteVerificationString(user, VerificationType.PWRESET);
                        }

                        String newUuid = dao.createVerificationString(user, VerificationType.PWRESET);
                        context.commit();
                        //TODO: send this by email
                        return ok(pwresetrequestok.render(user.getId(), newUuid, user.getEmail()));
                    } catch (DataAccessException ex) {
                        context.rollback();
                        throw ex;
                    }
                }
            } catch (DataAccessException ex) {
                throw ex;
            }
        }
    }

    /**
     * Method: GET
     *
     * @param userId
     * @param uuid
     * @return
     */
    public static Result resetPassword(int userId, String uuid) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();
            User user = dao.getUser(userId);
            if (user == null) {
                return badRequest("Deze user bestaat niet."); //TODO: flash
            } else {
                String ident = dao.getVerificationString(user, VerificationType.PWRESET);
                if (ident == null) {
                    return badRequest("There was no password reset requested on this account.");
                } else {
                    // Render the password reset page
                    return ok(pwreset.render(Form.form(PasswordResetModel.class), userId, uuid));
                }
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: POST
     *
     * @param userId
     * @param uuid
     * @return
     */
    public static Result resetPasswordProcess(int userId, String uuid) {
        Form<PasswordResetModel> resetForm = Form.form(PasswordResetModel.class).bindFromRequest();
        if (resetForm.hasErrors()) {
            return badRequest(pwreset.render(resetForm, userId, uuid));
        } else {
            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                UserDAO dao = context.getUserDAO();
                User user = dao.getUser(userId);
                if (user == null) {
                    return badRequest("Deze user bestaat niet."); //TODO: flash
                } else {
                    String ident = dao.getVerificationString(user, VerificationType.PWRESET);
                    if (ident == null) {
                        return badRequest("There was no password reset requested on this account.");
                    } else if (ident.equals(uuid)) {
                        dao.deleteVerificationString(user, VerificationType.PWRESET);
                        user.setPassword(hashPassword(resetForm.get().password));
                        dao.updateUser(user);
                        context.commit();

                        DatabaseHelper.getUserProvider().invalidateUser(user.getEmail());
                        flash("success", "Uw wachtwoord werd succesvol gewijzigd.");
                        LoginModel model = new LoginModel();
                        model.email = user.getEmail();

                        return ok(login.render(Form.form(LoginModel.class).fill(model)));
                    } else {
                        return badRequest("De verificatiecode komt niet overeen met onze gegevens.");
                    }
                }
            } catch (DataAccessException ex) {
                throw ex;
            }
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
            User user = DatabaseHelper.getUserProvider().getUser(loginForm.get().email);
            boolean goodCredentials = user != null && BCrypt.checkpw(loginForm.get().password, user.getPassword());

            if (goodCredentials) {
                if (user.getStatus() == UserStatus.EMAIL_VALIDATING) {
                    loginForm.reject("Deze account is nog niet geactiveerd. Gelieve je inbox te checken.");
                    //TODO: link aanvraag nieuwe bevestigingscode
                    return badRequest(login.render(loginForm));
                } else if (user.getStatus() == UserStatus.BLOCKED || user.getStatus() == UserStatus.DROPPED) {
                    loginForm.reject("Deze account werd verwijderd of geblokkeerd. Gelieve de administrator te contacteren.");
                    return badRequest(login.render(loginForm));
                } else {
                    session().clear();
                    session("email", loginForm.get().email);
                    return redirect(
                            routes.Dashboard.index() // go to dashboard page, authentication success
                    );
                }
            } else {
                loginForm.reject("Foute gebruikersnaam of wachtwoord.");
                return badRequest(login.render(loginForm));
            }
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

    public static Result register_verification(int userId, String uuid) {

        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();
            User user = dao.getUser(userId);
            if (user == null) {
                return badRequest("Deze user bestaat niet."); //TODO: flash
            } else if (user.getStatus() != UserStatus.EMAIL_VALIDATING) {
                flash("warning", "Deze gebruiker is reeds gevalideerd.");
                return badRequest(login.render(Form.form(LoginModel.class))); //We don't include a preset email address here since we could leak ID -> email to public
            } else {
                String ident = dao.getVerificationString(user, VerificationType.REGISTRATION);
                if (ident == null) {
                    return badRequest("Oops something went wrong. Missing identifier in database?!!!!! Anyway, contact an administrator.");
                } else if (ident.equals(uuid)) {
                    dao.deleteVerificationString(user, VerificationType.REGISTRATION);
                    user.setStatus(UserStatus.REGISTERED);

                    dao.updateUser(user);
                    context.commit();
                    DatabaseHelper.getUserProvider().invalidateUser(user.getEmail());

                    flash("success", "Uw email werd succesvol geverifieerd. Gelieve aan te melden.");
                    LoginModel model = new LoginModel();
                    model.email = user.getEmail();
                    return ok(login.render(Form.form(LoginModel.class).fill(model)));
                } else {
                    return badRequest("De verificatiecode komt niet overeen met onze gegevens. TODO: nieuwe string voorstellen.");
                }
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
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
                registerForm.reject("Er bestaat reeds een gebruiker met dit emailadres.");
                return badRequest(register.render(registerForm));
            } else {
                try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                    UserDAO dao = context.getUserDAO();
                    try {
                        User user = dao.createUser(registerForm.get().email, hashPassword(registerForm.get().password),
                                registerForm.get().firstName, registerForm.get().lastName);

                        // Now we create a registration UUID
                        String verificationIdent = dao.createVerificationString(user, VerificationType.REGISTRATION);
                        context.commit();
                        Mail.sendVerificationMail(user, verificationIdent);


                        return ok(registrationok.render(user.getId(), verificationIdent));
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

