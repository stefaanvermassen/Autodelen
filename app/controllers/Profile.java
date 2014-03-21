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

public class Profile extends Controller {

    public static class EditSmallProfileModel {
        public String phone; // momentarely only allow changing phone in small profile model?

        public String validate() {
            return null; //no validation
        }
    }

    public static class EditAddressModel { //TODO: unify with other models in controllers

        public String city;

        public void populate(Address address){
            if(address == null)
                return;

            city = address.getCity();
        }
    }

    public static class EditProfileModel {
        public String phone;
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

            this.domicileAddress.populate(user.getAddressDomicile());
            this.residenceAddress.populate(user.getAddressResidence());
        }
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
            return ok(edit.render(Form.form(EditProfileModel.class).fill(model), userId));
        } catch (DataAccessException ex) {
            throw ex;
        }
    }
}
