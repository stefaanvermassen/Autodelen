package notifiers;


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

    public static final String noReply = "Zelensis <noreply@zelensis.ugent.be>";

    public static void welcome(User user) {
        String mail = "";
        setSubject("Welcome %s", user.getFirstName());
        addRecipient(user.getEmail());
        addFrom(noReply);
        // get html and process the args in the view
        try(DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()){
            TemplateDAO dao = context.getTemplateDAO();
            EmailTemplate template = dao.getTemplate(MailType.REGISTRATION);
            //TODO: replace tags!
            mail = template.getBody();
            send(template.getBody());
        }catch (DataAccessException ex) {
            mail = welcome.render(user).body();
        }
        send(mail);
    }

}
