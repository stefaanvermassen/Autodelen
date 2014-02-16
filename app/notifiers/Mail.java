package notifiers;


import models.User;
import views.html.notifiers.welcome;


/**
 * Created by Stefaan Vermassen on 16/02/14.
 */

public class Mail extends Mailer {

    public static final String noReply = "Zelensis <noreply@zelensis.ugent.be>";

    public static void welcome(User user) {
        setSubject("Welcome %s", user.getName());
        addRecipient(user.getEmail());
        addFrom(noReply);
        // get html and process the args in the view
        String mail = welcome.render(user).body();

        send(mail);
    }

}
