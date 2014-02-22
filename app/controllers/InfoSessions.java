package controllers;

import database.*;
import models.Address;
import models.InfoSession;
import models.User;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.infosession.*;

import java.util.List;

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

        public DateTime getDateTime() {
            return DATEFORMATTER.parseDateTime(time);
            //return null;
        }

        public String validate() {
            if (DateTime.now().isAfter(getDateTime())) {
                return "Je kan enkel een infosessie plannen na de huidige datum.";
            }
            return null;
        }

    }

    //TODO: allow infosessions to be created by another use than the hoster

    /**
     * Method: GET
     *
     * @return
     */
    //TODO: admin attribute
    @Security.Authenticated(Secured.class)
    public static Result newSession() {
        return ok(newsession.render(Form.form(InfoSessionCreationModel.class)));
    }

    /**
     * Method: POST
     *
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result createNewSession() {
        Form<InfoSessionCreationModel> createForm = Form.form(InfoSessionCreationModel.class).bindFromRequest();
        if (createForm.hasErrors()) {
            return badRequest(newsession.render(createForm));
        } else {
            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                InfoSessionDAO dao = context.getInfoSessionDAO();

                try {
                    User user = DatabaseHelper.getUserProvider().getUser(session("email"));

                    Address address;
                    if ("other".equals(createForm.get().addresstype)) {
                        AddressDAO adao = context.getAddressDAO();
                        address = adao.createAddress(createForm.get().address_zip, createForm.get().address_city, createForm.get().address_street, createForm.get().address_number, createForm.get().address_bus);
                    } else {
                        address = user.getAddress();
                    }

                    InfoSession session = dao.createInfoSession(user, address, createForm.get().getDateTime()); //TODO: allow other hosts
                    context.commit();

                    if (session != null) {
                        return redirect(
                                routes.InfoSessions.showUpcomingSessions() // return to infosession list
                        );
                    } else {
                        createForm.error("Failed to create session in database. Contact administrator.");
                        return badRequest(newsession.render(createForm));
                    }
                } catch(DataAccessException ex){
                    context.rollback();
                    throw ex;
                }
            } catch (DataAccessException ex) {
                throw ex; //TODO: show gracefully
            }
        }
    }

    @Security.Authenticated(Secured.class)
    public static Result showUpcomingSessions() {
        try(DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            InfoSessionDAO dao = context.getInfoSessionDAO();
            List<InfoSession> sessions = dao.getInfoSessionsAfter(DateTime.now());
            return ok(list.render(sessions));
        } catch(DataAccessException ex){
            throw ex;
        }
    }
}
