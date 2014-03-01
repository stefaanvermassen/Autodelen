package controllers;

import controllers.Security.RoleSecured;
import models.UserRole;
import play.mvc.*;
import views.html.emailtemplates.*;

import static play.mvc.Results.ok;

/**
 * Created by Stefaan Vermassen on 01/03/14.
 */
public class EmailTemplates {

    @RoleSecured.RoleAuthenticated(value = {UserRole.ADMIN})
    public static Result showExistingTemplates() {
        return ok(emailtemplates.render());
    }
}
