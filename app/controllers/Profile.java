package controllers;

import controllers.Security.RoleSecured;
import controllers.util.ConfigurationHelper;
import controllers.util.FileAction;
import controllers.util.FileHelper;
import database.*;
import models.*;
import play.Logger;
import play.data.Form;
import play.mvc.*;
import views.html.profile.*;

import javax.imageio.IIOException;
import java.io.IOException;
import java.nio.file.Path;
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
     *
     * @param userId The userId for which the picture is uploaded
     * @return The page to upload
     */
    @RoleSecured.RoleAuthenticated()
    public static Result profilePictureUpload(int userId) {
        return ok(uploadPicture.render(userId));
    }

    /**
     * Gets the profile picture for given user Id, or default one if missing
     *
     * @param userId The user for which the image is requested
     * @return The image with correct content type
     */
    @RoleSecured.RoleAuthenticated()
    public static Result getProfilePicture(int userId) {
        //TODO: checks on whether other person can see this
        //TODO: Rely on heavy caching of the image ID or views since each app page includes this
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO udao = context.getUserDAO();
            User user = udao.getUser(userId, true);
            if (user != null && user.getProfilePictureId() >= 0) {
                return FileHelper.getFileStreamResult(context.getFileDAO(), user.getProfilePictureId());
            } else {
                return FileHelper.getPublicFile(Paths.get("images", "no_profile.png").toString(), "image/png");
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Processes a profile picture upload request
     *
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

        // Start saving the actual picture
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart picture = body.getFile("picture");
        if (picture != null) {
            String contentType = picture.getContentType();
            if (!FileHelper.isImageContentType(contentType)) { // Check the content type using MIME
                flash("danger", "Verkeerd bestandstype opgegeven. Enkel afbeeldingen zijn toegelaten. (ontvangen MIME-type: " + contentType + ")");
                return badRequest(uploadPicture.render(userId));
            } else {
                try {
                    // We do not put this inside the try-block because then we leave the connection open through file IO, which blocks it longer than it should.
                    Path relativePath;

                    // Non-resized:
                    //relativePath = FileHelper.saveFile(picture, ConfigurationHelper.getConfigurationString("uploads.profile")); // save file to disk


                    // Resized:
                    try {
                        relativePath = FileHelper.saveResizedImage(picture, ConfigurationHelper.getConfigurationString("uploads.profile"), 800);
                    } catch(IIOException ex){
                        // This means imagereader failed.
                        Logger.error("Failed profile picture resize: " + ex.getMessage());
                        flash("danger", "Uw afbeelding is corrupt / niet ondersteund. Gelieve het opnieuw te proberen of een administrator te contacteren.");
                        return badRequest(uploadPicture.render(userId));
                    }

                    try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {                     // Save the file reference in the database
                        FileDAO dao = context.getFileDAO();
                        UserDAO udao = context.getUserDAO();
                        try {
                            models.File file = dao.createFile(relativePath.toString(), picture.getFilename(), picture.getContentType());
                            int oldPictureId = user.getProfilePictureId();
                            user.setProfilePictureId(file.getId());
                            udao.updateUser(user, true);
                            context.commit();

                            if (oldPictureId != -1) {  // After commit we are sure the old one can be deleted
                                try {
                                    File oldPicture = dao.getFile(oldPictureId);
                                    FileHelper.deleteFile(Paths.get(oldPicture.getPath())); // String -> nio.Path
                                    dao.deleteFile(oldPictureId);

                                    context.commit();
                                } catch (DataAccessException ex) {
                                    // Failed to delete old profile picture, but no rollback needed
                                    throw ex;
                                }
                            }

                            flash("success", "De profielfoto werd succesvol aangepast.");
                            return redirect(routes.Profile.index(userId));
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

            // Only a profile admin or user itself can edit
            if (canEditProfile(user, currentUser)) {
                return ok(index.render(user, getProfileCompleteness(user)));
            } else if (DatabaseHelper.getUserRoleProvider().isFullUser(currentUser)) {
                return ok(profile.render(user));
            } else {
                return badRequest(views.html.unauthorized.render(new UserRole[]{UserRole.PROFILE_ADMIN, UserRole.USER}));
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Returns whether the currentUser can edit the profile of user
     *
     * @param user        The subject
     * @param currentUser The actor
     * @return A boolean when the profile can be edited
     */
    private static boolean canEditProfile(User user, User currentUser) {
        return currentUser.getId() == user.getId() || DatabaseHelper.getUserRoleProvider().hasRole(currentUser, UserRole.PROFILE_ADMIN);
    }

    public static class EditIdentityCardForm {
        public String cardNumber;
        public String nationalNumber;

        public EditIdentityCardForm() {
        }

        public EditIdentityCardForm(String cardNumber, String nationalNumber) {
            this.cardNumber = cardNumber;
            this.nationalNumber = nationalNumber;
        }

        public String validate() {
            return null; //TODO: use validation list
        }
    }

    /**
     * Method: GET
     * Generates the page where the user can upload his/her identity information
     * @param userId The user the requester wants to edit
     * @return A page to edit the identity card information
     */
    @RoleSecured.RoleAuthenticated()
    public static Result editIdentityCard(int userId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();
            User user = dao.getUser(userId, true);

            if (user == null) {
                flash("danger", "GebruikersID " + userId + " bestaat niet.");
                return redirect(routes.Dashboard.index());
            }

            User currentUser = DatabaseHelper.getUserProvider().getUser();

            // Only a profile admin or user itself can edit
            if (canEditProfile(user, currentUser)) {
                Form<EditIdentityCardForm> form = Form.form(EditIdentityCardForm.class);

                if (user.getIdentityCard() != null && user.getIdentityCard().getFileGroup() != null) { // get all uploaded files already
                    FileDAO fdao = context.getFileDAO();
                    user.getIdentityCard().setFileGroup(fdao.getFiles(user.getIdentityCard().getFileGroup().getId()));  // TODO: remove this hack to get actual files
                }

                if(user.getIdentityCard() != null) {
                    form = form.fill(new EditIdentityCardForm(user.getIdentityCard().getId(), user.getIdentityCard().getRegistrationNr()));
                }

                return ok(identitycard.render(user, form));
            } else {
                return badRequest(views.html.unauthorized.render(new UserRole[]{UserRole.PROFILE_ADMIN, UserRole.USER}));
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Returns the file requested after authorization checks
     * @param userId
     * @param fileId
     * @return
     */
    @RoleSecured.RoleAuthenticated()
    public static Result viewIdentityCardFile(int userId, int fileId){
        return identityCardFileAction(userId, fileId, new FileAction() {
            @Override
            public Result process(File file, FileDAO dao, DataAccessContext context) throws IOException, DataAccessException {
                 return FileHelper.getFileStreamResult(dao, file.getId());
            }
        });
    }

    private static Result identityCardFileAction(int userId, int fileId, FileAction action){
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO udao = context.getUserDAO();
            FileDAO fdao = context.getFileDAO();
            User user = udao.getUser(userId, true);
            User currentUser = DatabaseHelper.getUserProvider().getUser();

            if (user == null || !canEditProfile(user, currentUser)) {
                return badRequest(views.html.unauthorized.render(new UserRole[]{UserRole.PROFILE_ADMIN}));
            }

            if(user.getIdentityCard() != null && user.getIdentityCard().getFileGroup() != null){
                FileGroup files = fdao.getFiles(user.getIdentityCard().getFileGroup().getId()); //TODO: remove this hack to get actual files
                models.File file = files.getFileWithId(fileId);
                if(file != null){
                    try {
                        return action.process(file, fdao, context);
                    } catch(IOException | DataAccessException ex){
                        context.rollback();
                        throw new RuntimeException(ex);
                    }
                } else {
                    flash("danger", "Bestand niet gevonden.");
                    return redirect(routes.Profile.editIdentityCard(userId));
                }
            } else {
                flash("danger", "Deze gebruiker heeft geen bestanden voor identiteitskaart.");
                return redirect(routes.Profile.editIdentityCard(userId));
            }
        } catch(DataAccessException ex){
            throw ex;
        }
    }

    /**
     * Method: GET
     * Deletes a file from the identity card filegroup
     * @param userId The user to delete from
     * @param fileId The file to delete
     * @return A redirect to the identity card page overview
     */
    @RoleSecured.RoleAuthenticated()
    public static Result deleteIdentityCardFile(final int userId, int fileId){
        return identityCardFileAction(userId, fileId, new FileAction() {
            @Override
            public Result process(File file, FileDAO dao, DataAccessContext context) throws IOException, DataAccessException {
                dao.deleteFile(file.getId());
                FileHelper.deleteFile(Paths.get(file.getPath()));
                context.commit();

                flash("success", file.getFileName() + " werd met succes verwijderd.");
                return redirect(routes.Profile.editIdentityCard(userId));
            }
        });
    }

    /**
     * Method: POST
     * Processes the request to add files / change identity card information
     * @param userId The user to edit
     * @return The overview page or error page when something went wrong
     */
    @RoleSecured.RoleAuthenticated()
    public static Result editIdentityCardPost(int userId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO udao = context.getUserDAO();
            FileDAO fdao = context.getFileDAO();
            User user = udao.getUser(userId, true);
            User currentUser = DatabaseHelper.getUserProvider().getUser();

            if (user == null || !canEditProfile(user, currentUser)) {
                return badRequest(views.html.unauthorized.render(new UserRole[]{UserRole.PROFILE_ADMIN}));
            }

            if (user.getIdentityCard() != null && user.getIdentityCard().getFileGroup() != null) {
                user.getIdentityCard().setFileGroup(fdao.getFiles(user.getIdentityCard().getFileGroup().getId()));  // TODO: remove this hack to get actual files
            }

            Form<EditIdentityCardForm> form = Form.form(EditIdentityCardForm.class).bindFromRequest();
            if (form.hasErrors()) {
                return badRequest(identitycard.render(user, form));
            } else {
                try {
                    boolean updateUser = false; // Only perform a user update when we changed something (so not when adding a file to existing filegroup)

                    Http.MultipartFormData body = request().body().asMultipartFormData();
                    EditIdentityCardForm model = form.get();

                    IdentityCard card = user.getIdentityCard();
                    if (card == null) {
                        updateUser = true;
                        card = new IdentityCard();
                        user.setIdentityCard(card);
                    }

                    // Now check if we also have to create / add file to the group
                    Http.MultipartFormData.FilePart newFile = body.getFile("file");
                    if (newFile != null) {
                        if(!FileHelper.isDocumentContentType(newFile.getContentType())){
                            flash("danger", "Het documentstype dat u bijgevoegd heeft is niet toegestaan. (" + newFile.getContentType() + ").");
                            return badRequest(identitycard.render(user, form));
                        } else {
                            FileGroup group = card.getFileGroup();
                            if (group == null) {
                                // Create new filegroup
                                group = fdao.createFileGroup();
                                card.setFileGroup(group);
                                updateUser = true;
                            }

                            // Now we add the file to the group
                            Path relativePath = FileHelper.saveFile(newFile, ConfigurationHelper.getConfigurationString("uploads.identitycard"));
                            models.File file = fdao.createFile(relativePath.toString(), newFile.getFilename(), newFile.getContentType(), group.getId());
                            group.addFile(file); //this doesn't change the database, but allows reuse as model for next render
                        }
                    }

                    if((user.getIdentityCard().getRegistrationNr() != null && !user.getIdentityCard().getRegistrationNr().equals(model.nationalNumber)) ||
                            user.getIdentityCard().getId() != null && !user.getIdentityCard().getId().equals(model.cardNumber)) {
                        card.setRegistrationNr(model.nationalNumber);
                        card.setId(model.cardNumber);
                        updateUser = true;
                    }

                    if(updateUser) {
                        udao.updateUser(user, true);
                    }
                    context.commit();

                    flash("success", "Uw identiteitskaart werd succesvol bijgewerkt.");
                    return ok(identitycard.render(user, form));
                } catch(DataAccessException | IOException ex){ //IO or database error causes a rollback
                    context.rollback();
                    throw new RuntimeException(ex); //unchecked
                }
            }

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

            if (!canEditProfile(user, currentUser)) {
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
        if (user.getProfilePictureId() != -1) {
            total++;
        }

        return (int) (((float) total / 10) * 100); //10 records
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
