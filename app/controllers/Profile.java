package controllers;

import controllers.Security.RoleSecured;
import database.DataAccessContext;
import database.DataAccessException;
import database.DatabaseHelper;
import database.UserDAO;
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

    @RoleSecured.RoleAuthenticated()
    public static Result indexWithoutId(){
        return redirect(routes.Profile.index(DatabaseHelper.getUserProvider().getUser(session("email")).getId()));
    }

    @RoleSecured.RoleAuthenticated()
    public static Result index(int userId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();
            User user = dao.getUser(userId, true);
            User currentUser = DatabaseHelper.getUserProvider().getUser(session("email"));

            // Only a profile admin or
            if (currentUser.getId() != user.getId() && !DatabaseHelper.getUserRoleProvider().hasRole(user.getId(), UserRole.PROFILE_ADMIN)) {
                flash("danger", "U heeft geen rechten tot deze profielpagina.");
                return redirect(routes.Application.index());
            }

            // Prepare small form on the left
            EditSmallProfileModel smallModel = new EditSmallProfileModel();
            smallModel.phone = user.getPhone();
            Form<EditSmallProfileModel> smallForm = Form.form(EditSmallProfileModel.class).fill(smallModel);

            return ok(index.render(user, smallForm));
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: POST
     *
     * @param userId
     * @return
     */
    @RoleSecured.RoleAuthenticated()
    public static Result editSmallProfilePost(int userId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserDAO dao = context.getUserDAO();
            User user = dao.getUser(userId, true);
            User currentUser = DatabaseHelper.getUserProvider().getUser(session("email"));

            if (currentUser.getId() != user.getId() && !DatabaseHelper.getUserRoleProvider().hasRole(user.getId(), UserRole.PROFILE_ADMIN)) {
                flash("danger", "U heeft geen rechten tot deze profielpagina.");
                return redirect(routes.Application.index());
            }

            Form<EditSmallProfileModel> smallForm = Form.form(EditSmallProfileModel.class).bindFromRequest();
            if (smallForm.hasErrors()) {
                return badRequest(index.render(user, smallForm));
            } else {
                try {
                    user.setPhone(smallForm.get().phone);
                    dao.updateUser(user, true); //TODO: this shouldn't be a 'withrest'!!
                    context.commit();
                    flash("success", "Het profiel werd succesvol aangepast.");
                    return redirect(routes.Profile.index(user.getId()));
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
