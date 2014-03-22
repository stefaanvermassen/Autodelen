package controllers;

import controllers.Security.RoleSecured;
import database.DataAccessContext;
import database.DataAccessException;
import database.DatabaseHelper;
import database.UserDAO;
import models.Address;
import models.User;
import models.UserRole;
import play.data.Form;
import play.mvc.*;
import views.html.profile.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Profile extends Controller {

    private static List<String> COUNTRIES;
    private static final Locale COUNTRY_LANGUAGE = new Locale("nl", "BE");

    public static class EditAddressModel { //TODO: unify with other models in controllers

        public String city;
        public String number;
        public String street;
        public String bus;
        public String zipCode;
        public String country;

        public void populate(Address address){
            if(address == null) {
                country = COUNTRY_LANGUAGE.getDisplayCountry(COUNTRY_LANGUAGE);
                return;
            }

            city = address.getCity();
            number = address.getCity();
            street = address.getStreet();
            bus = address.getBus();
            zipCode = address.getZip();
            country = address.getCountry();
        }
    }

    public static class EditProfileModel {
        public String phone;
        public String mobile;
        public String firstName;
        public String lastName;
        public String email; // TODO: verification

        public String identityCardNumber;
        public String nationalNumber;

        public EditAddressModel domicileAddress;
        public EditAddressModel residenceAddress;

        public EditProfileModel(){
            this.domicileAddress = new EditAddressModel();
            this.residenceAddress = new EditAddressModel();
        }

        public void populate(User user){
            if(user == null)
                return;

            this.email = user.getEmail();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.phone = user.getPhone();
            this.mobile = user.getCellphone();

            this.domicileAddress.populate(user.getAddressDomicile());
            this.residenceAddress.populate(user.getAddressResidence());
        }
    }

    /**
     * Lazy loads a countryname list in Dutch
     * @return
     */
    private static List<String> getCountryList(){
        if(COUNTRIES == null){
            COUNTRIES = new ArrayList<>();
            Locale[] locales = Locale.getAvailableLocales();
            for (Locale obj : locales) {
                if ((obj.getDisplayCountry() != null) && (!"".equals(obj.getDisplayCountry()))) {
                    COUNTRIES.add(obj.getDisplayCountry(COUNTRY_LANGUAGE));
                }
            }
            Collections.sort(COUNTRIES);
        }
        return COUNTRIES;
    }

    @RoleSecured.RoleAuthenticated()
    public static Result indexWithoutId(){
        try(DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();
            User user = dao.getUser(session("email")); //user always has to exist (roleauthenticated)
            return ok(index.render(user));
        } catch(DataAccessException ex){
            throw ex;
        }
    }

    @RoleSecured.RoleAuthenticated()
    public static Result index(int userId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();
            User user = dao.getUser(userId, true);

            if(user == null){
                flash("danger", "GebruikersID " + userId + " bestaat niet.");
                return redirect(routes.Dashboard.index());
            }

            User currentUser = DatabaseHelper.getUserProvider().getUser(session("email"));

            // Only a profile admin or
            if (currentUser.getId() != user.getId() && !DatabaseHelper.getUserRoleProvider().hasRole(user.getId(), UserRole.PROFILE_ADMIN)) {
                return badRequest(views.html.unauthorized.render(new UserRole[] {UserRole.PROFILE_ADMIN }));
            }

            return ok(index.render(user));
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    @RoleSecured.RoleAuthenticated()
    public static Result edit(int userId){
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();
            User user = dao.getUser(userId, true);
            User currentUser = DatabaseHelper.getUserProvider().getUser(session("email"));

            if (currentUser.getId() != user.getId() && !DatabaseHelper.getUserRoleProvider().hasRole(user.getId(), UserRole.PROFILE_ADMIN)) {
                return badRequest(views.html.unauthorized.render(new UserRole[] {UserRole.PROFILE_ADMIN }));
            }

            EditProfileModel model = new EditProfileModel();
            model.populate(user);
            return ok(edit.render(Form.form(EditProfileModel.class).fill(model), user, getCountryList()));
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    @RoleSecured.RoleAuthenticated()
    public static Result editPost(int userId){
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();
            User user = dao.getUser(userId, true);
            User currentUser = DatabaseHelper.getUserProvider().getUser(session("email"));

            if (currentUser.getId() != user.getId() && !DatabaseHelper.getUserRoleProvider().hasRole(user.getId(), UserRole.PROFILE_ADMIN)) {
                return badRequest(views.html.unauthorized.render(new UserRole[] {UserRole.PROFILE_ADMIN }));
            }

            Form<EditProfileModel> profileForm = Form.form(EditProfileModel.class).bindFromRequest();
            if (profileForm.hasErrors()) {
                return badRequest(edit.render(profileForm, user, getCountryList()));
            } else {
                EditProfileModel model = profileForm.get();
                try {


                    context.commit();
                    flash("success", "Het profiel werd succesvol aangepast.");
                    return redirect(routes.Profile.index(userId));
                } catch(DataAccessException ex){
                    context.rollback();
                    throw ex;
                }
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }
}
