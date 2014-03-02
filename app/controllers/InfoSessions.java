package controllers;

import controllers.Security.RoleSecured;
import database.*;
import models.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.infosession.detail;
import views.html.infosession.infosessions;
import views.html.infosession.newInfosession;

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
    @RoleSecured.RoleAuthenticated(value = {UserRole.ADMIN})
    public static Result newSession() {
        return ok(newInfosession.render(Form.form(InfoSessionCreationModel.class)));
    }

    @RoleSecured.RoleAuthenticated({})
    public static Result unenrollSession() {
        User user = DatabaseHelper.getUserProvider().getUser(session("email"));
        if (user.getStatus() == UserStatus.REGISTERED) {
            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                InfoSessionDAO dao = context.getInfoSessionDAO();

                InfoSession alreadyAttending = dao.getAttendingInfoSession(user);
                if(alreadyAttending == null){
                    return badRequest("U bent niet ingeschreven in een toekomstige infosessie.");
                } else {
                    try {
                        dao.unregisterUser(alreadyAttending, user);
                        context.commit();
                        return ok("U bent succesvol uitgeschreven uit deze infosessie."); //TODO: flash
                    } catch(DataAccessException ex){
                        context.rollback();
                        throw ex;
                    }
                }
            } catch (DataAccessException ex) {
                throw ex;
            }
        } else {
            return badRequest("U bent al een geverifieerde gebruiker.");     //TODO: flash already normal user
        }
    }

    @RoleSecured.RoleAuthenticated({})
    public static Result detail(int sessionId){
        try(DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            InfoSessionDAO dao = context.getInfoSessionDAO();
            InfoSession session = dao.getInfoSession(sessionId, true);
            if(session == null){
                return badRequest("Sessie bestaat niet."); //TODO: flash
            } else {
                return ok(detail.render(session));
            }
        } catch(DataAccessException ex){
            throw ex;
            //TODO: log
        }
    }

    //TODO: allow UserProvider to get user by ID

    /**
     * Method: GET
     * @param sessionId Id of the session the user has to be removed from
     * @param userEmail Email of the user to be removed
     * @return Status of the operation page
     */
    @RoleSecured.RoleAuthenticated({UserRole.ADMIN})
    public static Result removeUserFromSession(int sessionId, String userEmail){
        try(DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            InfoSessionDAO dao = context.getInfoSessionDAO();
            InfoSession is = dao.getInfoSession(sessionId, false);
            if(is == null){
                return badRequest("Infosessie bestaat niet.");
            }

            User user = DatabaseHelper.getUserProvider().getUser(userEmail);
            if(user == null){
                return badRequest("User bestaat niet met adres=" + userEmail);
            }

            dao.unregisterUser(is, user);
            context.commit();
            return ok("De gebruiker werd succesvol uitgeschreven uit de infosessie.");
        } catch(DataAccessException ex){
            throw ex;
        }
    }

    @RoleSecured.RoleAuthenticated({})
    public static Result enrollSession(int sessionId) {
        User user = DatabaseHelper.getUserProvider().getUser(session("email"));
        if (user.getStatus() == UserStatus.REGISTERED) {
            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                InfoSessionDAO dao = context.getInfoSessionDAO();

                InfoSession alreadyAttending = dao.getAttendingInfoSession(user);
                if (alreadyAttending != null && alreadyAttending.getTime().isAfter(DateTime.now())) {
                    return badRequest("U bent al ingeschreven voor een infosessie op " + alreadyAttending.getTime()); //TODO: show flash message and link to session
                } else {
                    InfoSession session = dao.getInfoSession(sessionId, false);
                    if (session == null) {
                        return badRequest("Deze sessie bestaat niet."); //TODO: flash
                    } else {
                        try {
                            dao.registerUser(session, user);
                            context.commit();
                            return ok("U bent succesvol ingeschreven op de sessie van " + session.getTime());
                        } catch (DataAccessException ex) {
                            context.rollback();
                            throw ex;
                        }
                    }
                }
            } catch (DataAccessException ex) {
                throw ex;
            }
        } else {
            return badRequest("U bent al een geverifieerde gebruiker.");     //TODO: flash already normal user
        }
    }


    /**
     * Method: POST
     *
     * @return
     */
    @RoleSecured.RoleAuthenticated(value = {UserRole.ADMIN})
    public static Result createNewSession() {
        Form<InfoSessionCreationModel> createForm = Form.form(InfoSessionCreationModel.class).bindFromRequest();
        if (createForm.hasErrors()) {
            return badRequest(newInfosession.render(createForm));
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
                        return badRequest(newInfosession.render(createForm));
                    }
                } catch (DataAccessException ex) {
                    context.rollback();
                    throw ex;
                }
            } catch (DataAccessException ex) {
                throw ex; //TODO: show gracefully
            }
        }
    }

    @RoleSecured.RoleAuthenticated(value = {UserRole.ADMIN})
    public static Result showUpcomingSessions() {
        User user = DatabaseHelper.getUserProvider().getUser(session("email"));
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            InfoSessionDAO dao = context.getInfoSessionDAO();
            InfoSession enrolled = dao.getAttendingInfoSession(user);

            List<InfoSession> sessions = dao.getInfoSessionsAfter(DateTime.now());
            return ok(infosessions.render(sessions, enrolled)); //TODO: get enrolled
        } catch (DataAccessException ex) {
            throw ex;
        }
    }
}
