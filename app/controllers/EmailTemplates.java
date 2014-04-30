package controllers;

import controllers.Security.RoleSecured;
import database.*;
import models.EmailTemplate;
import models.UserRole;
import play.mvc.Controller;
import play.mvc.Result;
import providers.DataProvider;
import views.html.emailtemplates.edit;
import views.html.emailtemplates.emailtemplates;
import views.html.emailtemplates.emailtemplatespage;

import java.util.List;
import java.util.Map;


/**
 * Controller responsible for showing and editing message templates
 */
public class EmailTemplates extends Controller {

    /**
     * Method: GET
     *
     * @return all the templates that are available in the system
     */
    @RoleSecured.RoleAuthenticated({UserRole.MAIL_ADMIN})
    public static Result showExistingTemplates() {
       return ok(emailtemplates.render());
    }

    @RoleSecured.RoleAuthenticated({UserRole.MAIL_ADMIN})
    public static Result showExistingTemplatesPage(int page, int ascInt, String orderBy, String searchString) {
        // We don't use the page, ascInt, orderBy, searchString, because there aren't many templates
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            TemplateDAO dao = context.getTemplateDAO();
            List<EmailTemplate> templates = dao.getAllTemplates();
            // Always 1 page
            int pages = 1;
            int results = templates.size();
            return ok(emailtemplatespage.render(templates, results, pages));
        } catch(Exception e) {
            throw e;
        }
    }

    /**
     * Method: GET
     *
     * @param templateId the id of the template of which the details are requested
     * @return the detail page of specific template
     */
    @RoleSecured.RoleAuthenticated({UserRole.MAIL_ADMIN})
    public static Result showTemplate(int templateId) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            TemplateDAO dao = context.getTemplateDAO();
            EmailTemplate template = dao.getTemplate(templateId);
            if (template == null) {
                return badRequest("Template bestaat niet.");
            } else {
                return ok(edit.render(template));
            }
        } catch (DataAccessException ex) {
            throw ex;
        }

    }

    /**
     * Method: POST
     * Called when a template is edited
     *
     * @return templates index page
     */
    @RoleSecured.RoleAuthenticated({UserRole.MAIL_ADMIN})
    public static Result editTemplate() {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            TemplateDAO dao = context.getTemplateDAO();
            final Map<String, String[]> values = request().body().asFormUrlEncoded();
            String templateBody = values.get("template_body")[0];
            String templateSubject = values.get("template_subject")[0];
            int templateId = Integer.parseInt(values.get("template_id")[0]);
            boolean templateSendMail = Boolean.parseBoolean(values.get("template_send_mail")[0]);
            dao.updateTemplate(templateId, templateBody, templateSubject, templateSendMail);
            context.commit();
            return ok(routes.EmailTemplates.showExistingTemplates().toString());
        } catch (DataAccessException ex) {
            throw ex; //TODO: show gracefully
        }
    }
}
