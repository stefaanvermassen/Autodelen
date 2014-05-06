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
import providers.DataProvider;
import views.html.settings.*;

import java.util.List;
import java.util.Set;

public class Settings extends Controller {


    public static class EditSettingModel {
        public String value;
        public String name;
        public DateTime after;


        public EditSettingModel() {
        }

        public EditSettingModel(String value, String name, DateTime after) {
            this.value = value;
            this.name = name;
            this.after = after;
        }

        public String validate() {
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
        User user = DataProvider.getUserProvider().getUser();
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            UserRoleDAO dao = context.getUserRoleDAO();
            Set<UserRole> roles = dao.getUserRoles(user.getId());
            if (roles.contains(UserRole.SUPER_USER)) {
                flash("warning", "U heeft reeds superuser rechten.");
                return badRequest(views.html.dashboard.render(user, Profile.getProfileCompleteness(user)));
            } else {
                try {
                    dao.addUserRole(user.getId(), UserRole.SUPER_USER);
                    context.commit();
                    DataProvider.getUserRoleProvider().invalidateRoles(user);
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
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            SettingDAO dao = context.getSettingDAO();
            List<Setting> settings = dao.getSettings();
            return ok(sysvars.render(settings));
        }
    }

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result editSysvar(int id) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            SettingDAO dao = context.getSettingDAO();
            Setting setting = dao.getSetting(id);
            if (setting == null) {
                flash("danger", "Deze setting ID bestaat niet.");
                return redirect(routes.Settings.sysvarsOverview());
            } else {
                EditSettingModel model = new EditSettingModel(setting.getValue(), setting.getName(), setting.getAfterDate());
                return ok(editsysvar.render(Form.form(EditSettingModel.class).fill(model), setting));
            }
        }
    }

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result editSysvarPost(int id) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            SettingDAO dao = context.getSettingDAO();

            Form<EditSettingModel> form = Form.form(EditSettingModel.class).bindFromRequest();
            if (form.hasErrors()) {
                return badRequest(editsysvar.render(form, dao.getSetting(id)));
            } else {
                EditSettingModel model = form.get();
                dao.updateSetting(id, model.name, model.value, model.after);
                context.commit();
                flash("success", "De systeemvariabele werd succesvol aangepast.");
                return redirect(routes.Settings.sysvarsOverview());
            }
        }
    }

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result createSysvar() {
        return ok(createsysvar.render(Form.form(EditSettingModel.class).fill(new EditSettingModel(null, null, DateTime.now()))));
    }

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result createSysvarPost() {
        Form<EditSettingModel> form = Form.form(EditSettingModel.class).bindFromRequest();
        if (form.hasErrors() || form.get().name == null) {
            return badRequest(createsysvar.render(form));
        } else {
            try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
                SettingDAO dao = context.getSettingDAO();
                EditSettingModel model = form.get();
                dao.createSettingAfterDate(model.name, model.value, model.after);
                context.commit();
                flash("success", "De systeemvariabele werd succesvol aangemaakt.");
                return redirect(routes.Settings.sysvarsOverview());
            }
        }
    }


}
