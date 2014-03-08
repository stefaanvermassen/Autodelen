package notifiers;


import controllers.routes;
import database.DataAccessContext;
import database.DataAccessException;
import database.DatabaseHelper;
import database.TemplateDAO;
import models.EmailTemplate;
import models.MailType;
import models.User;
import play.mvc.Http;
import views.html.notifiers.welcome;


/**
 * Created by Stefaan Vermassen on 16/02/14.
 */

public class Mail extends Mailer {

    public static final String NOREPLY = "Zelensis <noreply@zelensis.ugent.be>";

    public static void sendVerificationMail(User user, String verificationUrl) {
        String mail = "";
        setSubject("Welkom %s", user.getFirstName());
        addRecipient(user.getEmail());
        addFrom(NOREPLY);
        // get html and process the args in the view
        try(DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()){
            TemplateDAO dao = context.getTemplateDAO();
            EmailTemplate template = dao.getTemplate(MailType.VERIFICATION);
            mail = replaceUserTags(user, template.getBody());
            String vUrl = "http://" + Http.Context.current().request().host()  + routes.Login.register_verification(user.getId(), verificationUrl).toString();
            mail = mail.replace("%verification_url%", vUrl);
        }catch (DataAccessException ex) {
            mail = welcome.render(user).body();
        }

        if(!play.api.Play.isDev(play.api.Play.current())) {
            send(mail);
        }
    }

    private static String replaceUserTags(User user, String template){
        template = template.replace("%user_firstname%", user.getFirstName());
        template = template.replace("%user_lastname%", user.getLastName());
        //TODO: replace address only when provided
        return template;
    }



}
