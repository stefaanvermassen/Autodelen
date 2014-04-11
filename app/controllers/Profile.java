package controllers;

import controllers.Security.RoleSecured;
import controllers.util.ConfigurationHelper;
import controllers.util.FileHelper;
import database.*;
import models.Address;
import models.User;
import models.UserRole;
import play.data.Form;
import play.mvc.*;
import views.html.profile.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Profile extends Controller {

    private static List<String> COUNTRIES;
    private static final Locale COUNTRY_LANGUAGE = new Locale("nl", "BE");

    private static boolean nullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

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
            return nullOrEmpty(bus) && nullOrEmpty(zipCode) && nullOrEmpty(city) && nullOrEmpty(street) && nullOrEmpty(number);
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

        public String validate() {
            if (nullOrEmpty(firstName) || nullOrEmpty(lastName)) {
                return "Voor- en achternaam mogen niet leeg zijn.";
            } else return null;
        }
    }

    /**
     * Lazy loads a country list in current configured locale
     *
     * @return A list of all countries enabled in the Java locale
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

    /**
     * The page to upload a new profile picture
     * @param userId The userId for which the picture is uploaded
     * @return The page to upload
     */
    @RoleSecured.RoleAuthenticated()
    public static Result profilePictureUpload(int userId){
        return ok(uploadPicture.render(userId));
    }

    @RoleSecured.RoleAuthenticated()
    public static Result getProfilePicture(int userId){
        //TODO: checks on whether other person can see this
        try(DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO udao = context.getUserDAO();
            User user = udao.getUser(userId, true);
            if(user != null && user.getProfilePictureId() >= 0){
                return FileHelper.getFileStreamResult(context.getFileDAO(), user.getProfilePictureId());
            } else {
                return FileHelper.getPublicFile(Paths.get("images", "no_profile.png").toString(), "image/png");
            }
        } catch(DataAccessException ex){
            throw ex;
        }
    }

    /**
     * Processes a profile picture upload request
     * @param userId
     * @return
     */
    @RoleSecured.RoleAuthenticated()
    public static Result profilePictureUploadPost(int userId) {
        // First we check if the user is allowed to upload to this userId
        User currentUser = DatabaseHelper.getUserProvider().getUser();
        User user;

        // We load the other user(by id)
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();
            user = dao.getUser(userId, true);

            // Check if the userId exists
            if (user == null || currentUser.getId() != user.getId() && !DatabaseHelper.getUserRoleProvider().hasRole(currentUser, UserRole.PROFILE_ADMIN)) {
                return badRequest(views.html.unauthorized.render(new UserRole[]{UserRole.PROFILE_ADMIN}));
            }
        } catch (DataAccessException ex) {
            throw ex;
        }

        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart picture = body.getFile("picture"); //TODO: define that this is always in "file" somewhere, generalize
        if (picture != null) {

            // Check the content type
            String contentType = picture.getContentType();
            if (!FileHelper.IMAGE_CONTENT_TYPES.contains(contentType)) {
                flash("danger", "Verkeerde bestandstype opgegeven. Enkel afbeeldingen zijn toegelaten. (ontvangen MIME-type: " + contentType+ ")");
                return badRequest(uploadPicture.render(userId));
            } else {
                try {
                    // We do not put this inside the try-block because then we leave the connection open through file IO, which blocks it longer than it should.
                    String relativePath = FileHelper.saveFile(picture, ConfigurationHelper.getConfigurationString("uploads.profile"));

                    // Save the file reference in the database
                    try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                        FileDAO dao = context.getFileDAO();
                        try {
                            models.File file = dao.createFile(relativePath, picture.getFilename(), picture.getContentType());
                            //TODO now set user image ID (and delete the old one?)

                            context.commit();

                            return ok("File upload successfully to " + relativePath);
                        } catch (DataAccessException ex) {
                            context.rollback();
                            FileHelper.deleteFile(relativePath);
                            throw ex;
                        }
                    } catch (DataAccessException ex) {
                        FileHelper.deleteFile(relativePath);
                        throw ex;
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex); //no more checked catch -> error page!
                }
            }
        } else {
            flash("error", "Missing file");
            return redirect(routes.Application.index());
        }
    }

    /**
     * Method: GET
     *
     * @return A profile page for the currently requesting user
     */
    @RoleSecured.RoleAuthenticated()
    public static Result indexWithoutId() {
        User user = DatabaseHelper.getUserProvider().getUser(false);  //user always has to exist (roleauthenticated)
        return ok(index.render(user, getProfileCompleteness(user)));
    }

    /**
     * Method: GET
     *
     * @param userId The userId of the user (only available to administrator or user itself)
     * @return The profilepage of the user
     */
    @RoleSecured.RoleAuthenticated()
    public static Result index(int userId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();
            User user = dao.getUser(userId, true);

            if (user == null) {
                flash("danger", "GebruikersID " + userId + " bestaat niet.");
                return redirect(routes.Dashboard.index());
            }

            User currentUser = DatabaseHelper.getUserProvider().getUser();

            // Only a profile admin or
            if (currentUser.getId() != user.getId() && !DatabaseHelper.getUserRoleProvider().hasRole(currentUser.getId(), UserRole.PROFILE_ADMIN)) {
                if (!DatabaseHelper.getUserRoleProvider().hasRole(currentUser.getId(), UserRole.CAR_USER)) {
                    return badRequest(views.html.unauthorized.render(new UserRole[]{UserRole.PROFILE_ADMIN, UserRole.CAR_USER}));
                }

                /** TODO: Verander naar FULL user i.p.v. userRole CAR_USER
                 * if (currentUser.getStatus() != UserStatus.FULL) {
                 *     return badRequest(views.html.unauthorized.render(new UserRole[]{}));
                 * }
                 */

                return ok(profile.render(user));
            }

            return ok(index.render(user, getProfileCompleteness(user)));
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     * Creates a prefilled form to edit the profile
     *
     * @param userId The user to edit
     * @return A user edit page
     */
    @RoleSecured.RoleAuthenticated()
    public static Result edit(int userId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();
            User user = dao.getUser(userId, true);
            User currentUser = DatabaseHelper.getUserProvider().getUser();

            if (currentUser.getId() != user.getId() && !DatabaseHelper.getUserRoleProvider().hasRole(currentUser.getId(), UserRole.PROFILE_ADMIN)) {
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
     * @param user The user to quote
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
        if (!nullOrEmpty(user.getCellphone())) {
            total++;
        }
        if (!nullOrEmpty(user.getFirstName())) {
            total++;
        }
        if (!nullOrEmpty(user.getLastName())) {
            total++;
        }
        if (!nullOrEmpty(user.getPhone())) {
            total++;
        }
        if (!nullOrEmpty(user.getEmail())) {
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

    /**
     * Modifies, creates or deletes an address in the database based on the provided form data and current address
     *
     * @param model   The submitted form data
     * @param address The already-set address for the user
     * @param dao     The DAO to edit addresses
     * @return The changed or null if deleted
     */
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

    /**
     * Method: POST
     * Changes the users profile based on submitted form data
     *
     * @param userId The user id to change
     * @return The new profile page, or the edit form when errors occured
     */
    @RoleSecured.RoleAuthenticated()
    public static Result editPost(int userId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();
            User user = dao.getUser(userId, true);
            User currentUser = DatabaseHelper.getUserProvider().getUser();

            if (currentUser.getId() != user.getId() && !DatabaseHelper.getUserRoleProvider().hasRole(currentUser.getId(), UserRole.PROFILE_ADMIN)) {
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
                    Address domicileAddress = user.getAddressDomicile();
                    boolean deleteDomicile = model.domicileAddress.isEmpty() && domicileAddress != null;
                    user.setAddressDomicile(deleteDomicile || model.domicileAddress.isEmpty() ? null : modifyAddress(model.domicileAddress, domicileAddress, adao));

                    // Residence address
                    Address residenceAddress = user.getAddressResidence();
                    boolean deleteResidence = model.residenceAddress.isEmpty() && residenceAddress != null;
                    user.setAddressResidence(deleteResidence || model.residenceAddress.isEmpty() ? null : modifyAddress(model.residenceAddress, residenceAddress, adao));

                    dao.updateUser(user, true); // Full update (includes FKs)

                    // Finally we can delete the addresses since there are no references left (this assumes all other code uses copies of addresses)
                    // TODO: soft-delete addresses and keep references
                    if (deleteDomicile)
                        adao.deleteAddress(domicileAddress);

                    if (deleteResidence)
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
