package controllers;

import controllers.Security.RoleSecured;
import database.*;
import models.Setting;
import models.User;
import models.UserRole;
import org.joda.time.DateTime;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.settings.editsysvar;
import views.html.settings.overview;
import views.html.settings.sysvars;

import java.util.List;
import java.util.Set;

public class Settings extends Controller {


    public static class EditSettingModel {
        public String value;
        public String name;
        public DateTime after;


        public EditSettingModel(){}
        public EditSettingModel(String value, String name, DateTime after){
            this.value = value;
            this.name = name;
            this.after = after;
        }

        public String validate(){
            return null; //TODO
        }
    }

    public static Result index() {
        return ok(overview.render());
    }


    /**
     * Method: GET
     * Temporary method to create a superuser
     *
     * @return Redirect to the userrole page
     */
    @Deprecated
    @RoleSecured.RoleAuthenticated()
    public static Result instantAdmin() {
        User user = DatabaseHelper.getUserProvider().getUser();
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            UserRoleDAO dao = context.getUserRoleDAO();
            Set<UserRole> roles = dao.getUserRoles(user.getId());
            if (roles.contains(UserRole.SUPER_USER)) {
                flash("warning", "U heeft reeds superuser rechten.");
                return badRequest(views.html.dashboard.render());
            } else {
                try {
                    dao.addUserRole(user.getId(), UserRole.SUPER_USER);
                    context.commit();
                    DatabaseHelper.getUserRoleProvider().invalidateRoles(user);
                    roles.add(UserRole.SUPER_USER);

                    flash("success", "U heeft nu superuserrechten. Gelieve je extra rechten aan te duiden.");
                    return ok(views.html.userroles.editroles.render(UserRoles.getUserRolesStatus(roles), user));
                } catch (DataAccessException ex) {
                    context.rollback();
                    throw ex;
                }
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result sysvarsOverview() {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            SettingDAO dao = context.getSettingDAO();
            List<Setting> settings = dao.getSettings();
            return ok(sysvars.render(settings));
        }
    }

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result editSysvar(int id){
        try(DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            SettingDAO dao = context.getSettingDAO();
            Setting setting = dao.getSetting(id);
            if(setting == null){
                flash("danger", "Deze setting ID bestaat niet.");
                return redirect(routes.Settings.sysvarsOverview());
            } else {
                EditSettingModel model = new EditSettingModel(setting.getName(), setting.getValue(), setting.getAfterDate());
                return ok(editsysvar.render(Form.form(EditSettingModel.class).fill(model), setting));
            }
        }

    }

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result editSysvarPost(int id){
        return ok("edit POST request received.");
    }


}
