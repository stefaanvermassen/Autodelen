package controllers;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.infosession.*;

/**
 * Created by Cedric on 2/21/14.
 */
public class InfoSessions extends Controller {

    private static final DateTimeFormatter DATEFORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"); //ISO time without miliseconds

    public static class InfoSessionCreationModel {
        public String time; //TODO: use date format here
        public String addresstype;

        // Address fields
        public String address_city;
        public String address_zip;
        public String address_street;
        public String address_number;
        public String address_bus;

        public DateTime getDateTime(){
            return DATEFORMATTER.parseDateTime(time);
            //return null;
        }

        public String validate() {
            if(DateTime.now().isAfter(getDateTime())) {
                return "Je kan enkel een infosessie plannen na de huidige datum.";
            }
            return null;
        }

    }

    //TODO: allow infosessions to be created by another use than the hoster

    /**
     * Method: GET
     * @return
     */
    //TODO: admin attribute
    @Security.Authenticated(Secured.class)
    public static Result newSession() {
        return ok(newsession.render(Form.form(InfoSessionCreationModel.class)));
    }

    /**
     * Method: POST
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result createNewSession(){
        Form<InfoSessionCreationModel> createForm = Form.form(InfoSessionCreationModel.class).bindFromRequest();
        if (createForm.hasErrors()) {
            return badRequest(newsession.render(createForm));
        } else {


            return ok("Sessie aangemaakt. TODO: toon flash op aanmaakpagina." + createForm.get().getDateTime());
        }
    }
}
