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
import views.html.emailtemplates.edit;
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

    @RoleSecured.RoleAuthenticated(value = {UserRole.ADMIN})
    public static Result edit(int templateId){
        try(DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            TemplateDAO dao = context.getTemplateDao();
            EmailTemplate template = dao.getTemplate(templateId);
            if(template == null){
                return badRequest("Template bestaat niet.");
            } else {
                return ok(edit.render(template));
            }
        } catch(DataAccessException ex){
            throw ex;
        }

    }
}
