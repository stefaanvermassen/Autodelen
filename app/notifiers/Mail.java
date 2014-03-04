package notifiers;


import controllers.routes;
import database.DataAccessContext;
import database.DataAccessException;
import database.DatabaseHelper;
import database.TemplateDAO;
import models.EmailTemplate;
import models.MailType;
import models.User;
import views.html.notifiers.welcome;


/**
 * Created by Stefaan Vermassen on 16/02/14.
 */

public class Mail extends Mailer {

    public static final String NOREPLY = "Zelensis <noreply@zelensis.ugent.be>";

    public static void sendVerificationMail(User user, String verificationUrl) {
        String mail = "";
        setSubject("Welcome %s", user.getFirstName());
        addRecipient(user.getEmail());
        addFrom(NOREPLY);
        // get html and process the args in the view
        try(DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()){
            TemplateDAO dao = context.getTemplateDAO();
            EmailTemplate template = dao.getTemplate(MailType.VERIFICATION);
            mail = replaceUserTags(user, template.getBody());
            mail = mail.replace("%verification_url%", routes.Login.register_verification(user.getId(), verificationUrl).toString());
        }catch (DataAccessException ex) {
            mail = welcome.render(user).body();
        }
        send(mail);
    }

    private static String replaceUserTags(User user, String template){
        template = template.replace("%user_firstname%", user.getFirstName());
        template = template.replace("%user_lastname%", user.getLastName());
        //TODO: replace address only when provided
        return template;
    }



}
