package controllers;

import controllers.Security.RoleSecured;
import database.DataAccessContext;
import database.DataAccessException;
import database.DatabaseHelper;
import database.TemplateDAO;
import models.EmailTemplate;
import models.UserRole;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.emailtemplates.emailtemplates;

import java.util.List;


/**
 * Created by Stefaan Vermassen on 01/03/14.
 */
public class EmailTemplates extends Controller{

    @RoleSecured.RoleAuthenticated(value = {UserRole.ADMIN})
    public static Result showExistingTemplates() {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            TemplateDAO dao = context.getTemplateDao();
            List<EmailTemplate> templates = dao.getAllTemplates();
            return ok(emailtemplates.render(templates));
        }catch (DataAccessException ex) {
            throw ex;
        }

    }
}
