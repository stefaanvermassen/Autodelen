package controllers;

import controllers.Security.RoleSecured;
import database.*;
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

        public void populate(Address address) {
            if (address == null) {
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

        public boolean isEmpty() {
            return !((bus != null && !bus.isEmpty()) || (zipCode != null && !zipCode.isEmpty()) ||
                    (city != null && !city.isEmpty()) || (street != null && !street.isEmpty()) || (number != null && !number.isEmpty()));
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

        public EditProfileModel() {
            this.domicileAddress = new EditAddressModel();
            this.residenceAddress = new EditAddressModel();
        }

        public void populate(User user) {
            if (user == null)
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
     *
     * @return
     */
    private static List<String> getCountryList() {
        if (COUNTRIES == null) {
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
    public static Result indexWithoutId() {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();
            User user = dao.getUser(session("email")); //user always has to exist (roleauthenticated)
            return ok(index.render(user, getProfileCompleteness(user)));
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    @RoleSecured.RoleAuthenticated()
    public static Result index(int userId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();
            User user = dao.getUser(userId, true);

            if (user == null) {
                flash("danger", "GebruikersID " + userId + " bestaat niet.");
                return redirect(routes.Dashboard.index());
            }

            User currentUser = DatabaseHelper.getUserProvider().getUser(session("email"));

            // Only a profile admin or
            if (currentUser.getId() != user.getId() && !DatabaseHelper.getUserRoleProvider().hasRole(user.getId(), UserRole.PROFILE_ADMIN)) {
                return badRequest(views.html.unauthorized.render(new UserRole[]{UserRole.PROFILE_ADMIN}));
            }

            return ok(index.render(user, getProfileCompleteness(user)));
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    @RoleSecured.RoleAuthenticated()
    public static Result edit(int userId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();
            User user = dao.getUser(userId, true);
            User currentUser = DatabaseHelper.getUserProvider().getUser(session("email"));

            if (currentUser.getId() != user.getId() && !DatabaseHelper.getUserRoleProvider().hasRole(user.getId(), UserRole.PROFILE_ADMIN)) {
                return badRequest(views.html.unauthorized.render(new UserRole[]{UserRole.PROFILE_ADMIN}));
            }

            EditProfileModel model = new EditProfileModel();
            model.populate(user);
            return ok(edit.render(Form.form(EditProfileModel.class).fill(model), user, getCountryList()));
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Returns a quotum on how complete the profile is.
     *
     * @param user
     * @return Completeness in percents
     */
    private static int getProfileCompleteness(User user) {
        int total = 0;

        if (user.getAddressDomicile() != null) {
            total++;
        }
        if (user.getAddressResidence() != null) {
            total++;
        }
        if (user.getCellphone() != null) {
            total++;
        }
        if (user.getFirstName() != null) {
            total++;
        }
        if (user.getLastName() != null) {
            total++;
        }
        if (user.getPhone() != null) {
            total++;
        }
        if (user.getEmail() != null) {
            total++;
        }
        if (user.getIdentityCard() != null) {
            total++;
        }
        if (user.getContractManager() != null) {
            total++;
        }
        //TODO: profile picture

        return (int) (((float) total / 9) * 100);
    }

    private static Address modifyAddress(EditAddressModel model, Address address, AddressDAO dao) {
        if (address == null) {
            // User entered new address in fields
            address = dao.createAddress(model.country, model.zipCode, model.city, model.street, model.number, model.bus);
        } else {
            // User changed existing address

            // Only call the database when there's actually some change
            if ((model.country != null && model.country.equals(address.getCountry())) ||
                    (model.zipCode != null && !model.zipCode.equals(address.getZip())) ||
                    (model.city != null && !model.city.equals(address.getCity())) ||
                    (model.street != null && !model.street.equals(address.getStreet())) ||
                    (model.number != null && !model.number.equals(address.getNumber())) ||
                    (model.bus != null && !model.bus.equals(address.getBus()))) {
                address.setCountry(model.country);
                address.setZip(model.zipCode);
                address.setCity(model.city);
                address.setStreet(model.street);
                address.setNumber(model.number);
                address.setBus(model.bus);
                dao.updateAddress(address);
            }
        }
        return address;
    }

    @RoleSecured.RoleAuthenticated()
    public static Result editPost(int userId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();
            User user = dao.getUser(userId, true);
            User currentUser = DatabaseHelper.getUserProvider().getUser(session("email"));

            if (currentUser.getId() != user.getId() && !DatabaseHelper.getUserRoleProvider().hasRole(user.getId(), UserRole.PROFILE_ADMIN)) {
                return badRequest(views.html.unauthorized.render(new UserRole[]{UserRole.PROFILE_ADMIN}));
            }

            Form<EditProfileModel> profileForm = Form.form(EditProfileModel.class).bindFromRequest();
            if (profileForm.hasErrors()) {
                return badRequest(edit.render(profileForm, user, getCountryList()));
            } else {
                EditProfileModel model = profileForm.get();
                try {
                    AddressDAO adao = context.getAddressDAO();

                    user.setPhone(model.phone);
                    user.setCellphone(model.mobile);
                    user.setFirstName(model.firstName);
                    user.setLastName(model.lastName);

                    // Because of constraints with FK we have to set them to NULL first in user before deleting them

                    // Check domicile address
                    Address domicileAddress =  user.getAddressDomicile();
                    boolean deleteDomicile = model.domicileAddress.isEmpty() && domicileAddress != null;
                    user.setAddressDomicile(deleteDomicile ? null : modifyAddress(model.domicileAddress, domicileAddress, adao));

                    // Residence address
                    Address residenceAddress = user.getAddressResidence();
                    boolean deleteResidence = model.residenceAddress.isEmpty() && residenceAddress != null;
                    user.setAddressResidence(deleteResidence ? null : modifyAddress(model.residenceAddress, residenceAddress, adao));
                    dao.updateUser(user, true); // Full update (includes FKs)

                    // Finally we can delete the addresses since there are no references left (this assumes all other code uses copies of addresses)
                    // TODO: soft-delete addresses and keep references
                    if(deleteDomicile)
                        adao.deleteAddress(domicileAddress);

                    if(deleteResidence)
                        adao.deleteAddress(residenceAddress);

                    //TODO: identity card & numbers, profile picture

                    context.commit();
                    flash("success", "Het profiel werd succesvol aangepast.");
                    return redirect(routes.Profile.index(userId));
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
