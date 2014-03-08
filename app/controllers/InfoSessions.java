package controllers;

import controllers.Security.RoleSecured;
import database.*;
import models.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.api.templates.Html;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.infosession.*;
import views.html.infosession.newInfosession;

import java.util.List;

/**
 * Created by Cedric on 2/21/14.
 */
public class InfoSessions extends Controller {

    private static final DateTimeFormatter DATEFORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"); //ISO time without miliseconds

    public static class InfoSessionCreationModel {
        public String time; //TODO: use date format here

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
        return ok(newInfosession.render(Form.form(InfoSessionCreationModel.class), 0));
    }

    /**
     * Method: GET
     *
     * @param sessionId
     * @return
     */
    @RoleSecured.RoleAuthenticated(value = {UserRole.ADMIN})
    public static Result editSession(int sessionId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            InfoSessionDAO dao = context.getInfoSessionDAO();
            InfoSession is = dao.getInfoSession(sessionId, false);
            if (is == null) {
                flash("danger", "Infosessie met ID=" + sessionId + " bestaat niet.");
                return badRequest(upcomingSessionsList());
            } else {
                InfoSessionCreationModel model = new InfoSessionCreationModel();
                model.time = is.getTime().toString(DATEFORMATTER);
                model.address_city = is.getAddress().getCity();
                model.address_zip = is.getAddress().getZip();
                model.address_street = is.getAddress().getStreet();
                model.address_number = is.getAddress().getNumber();
                model.address_bus = is.getAddress().getBus();

                Form<InfoSessionCreationModel> editForm = Form.form(InfoSessionCreationModel.class).fill(model);
                return ok(newInfosession.render(editForm, sessionId));
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     * @param sessionId
     * @return
     */
    @RoleSecured.RoleAuthenticated({UserRole.ADMIN})
    public static Result removeSession(int sessionId){
        try(DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            return ok("");
        } catch(DataAccessException ex){
            throw ex;
        }
    }

    /**
     * Method: POST
     *
     * @param sessionId
     * @return
     */
    @RoleSecured.RoleAuthenticated(value = {UserRole.ADMIN})
    public static Result editSessionPost(int sessionId) {
        Form<InfoSessionCreationModel> editForm = Form.form(InfoSessionCreationModel.class).bindFromRequest();
        if (editForm.hasErrors()) {
            return badRequest(newInfosession.render(editForm, sessionId));
        } else {
            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                InfoSessionDAO dao = context.getInfoSessionDAO();
                InfoSession session = dao.getInfoSession(sessionId, false);
                if (session == null) {
                    flash("danger", "Infosessie met ID=" + sessionId + " bestaat niet.");
                    return badRequest(upcomingSessionsList());
                }

                try {
                    Address address = session.getAddress();
                    address.setCity(editForm.get().address_city);
                    address.setBus(editForm.get().address_bus);
                    address.setNumber(editForm.get().address_number);
                    address.setStreet(editForm.get().address_street);
                    address.setZip(editForm.get().address_zip);

                    AddressDAO adao = context.getAddressDAO();
                    adao.updateAddress(address);

                    // Now we update the time
                    DateTime time = editForm.get().getDateTime();
                    if (!session.getTime().equals(time)) {
                        session.setTime(time);
                        dao.updateInfosessionTime(session);
                    }
                    context.commit();
                    flash("success", "Uw wijzigingen werden succesvol toegepast.");
                    return detail(sessionId);
                } catch (DataAccessException ex) {
                    context.rollback();
                    throw ex;
                }
            } catch (DataAccessException ex) {
                throw ex;
            }
        }
    }

    @RoleSecured.RoleAuthenticated()
    public static Result unenrollSession() {
        User user = DatabaseHelper.getUserProvider().getUser(session("email"));
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            InfoSessionDAO dao = context.getInfoSessionDAO();

            InfoSession alreadyAttending = dao.getAttendingInfoSession(user);
            if (alreadyAttending == null) {
                flash("danger", "U bent niet ingeschreven voor een toekomstige infosessie.");
                return badRequest(upcomingSessionsList());
            } else {
                try {
                    dao.unregisterUser(alreadyAttending, user);
                    context.commit();

                    flash("success", "U bent succesvol uitgeschreven uit deze infosessie.");
                    return showUpcomingSessions();
                } catch (DataAccessException ex) {
                    context.rollback();
                    throw ex;
                }
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    @RoleSecured.RoleAuthenticated()
    public static Result detail(int sessionId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            InfoSessionDAO dao = context.getInfoSessionDAO();
            InfoSession session = dao.getInfoSession(sessionId, true);
            if (session == null) {
                flash("danger", "Infosessie met ID=" + sessionId + " bestaat niet.");
                return badRequest(upcomingSessionsList());
            } else {
                return ok(detail.render(session));
            }
        } catch (DataAccessException ex) {
            throw ex;
            //TODO: log
        }
    }

    /**
     * Method: GET
     *
     * @param sessionId Id of the session the user has to be removed from
     * @param userId    Userid of the user to be removed
     * @return Status of the operation page
     */
    @RoleSecured.RoleAuthenticated({UserRole.ADMIN})
    public static Result removeUserFromSession(int sessionId, int userId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            InfoSessionDAO dao = context.getInfoSessionDAO();
            InfoSession is = dao.getInfoSession(sessionId, false);
            if (is == null) {
                flash("danger", "Infosessie met ID " + sessionId + " bestaat niet.");
                return badRequest(upcomingSessionsList());
            }

            UserDAO udao = context.getUserDAO();

            User user = udao.getUser(userId);
            if (user == null) {
                flash("danger", "Gebruiker met ID " + userId + " bestaat niet.");
                return badRequest(upcomingSessionsList());
            }

            dao.unregisterUser(is, user);
            context.commit();

            flash("success", "De gebruiker werd succesvol uitgeschreven uit de infosessie.");
            return detail(sessionId);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    @RoleSecured.RoleAuthenticated({UserRole.ADMIN})
    public static Result setUserSessionStatus(int sessionId, int userId, String status) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {

            InfoSessionDAO dao = context.getInfoSessionDAO();
            InfoSession is = dao.getInfoSession(sessionId, false);
            if (is == null) {
                flash("danger", "Infosessie met ID " + sessionId + " bestaat niet.");
                return badRequest(upcomingSessionsList());
            }

            UserDAO udao = context.getUserDAO();
            User user = udao.getUser(userId);
            if (user == null) {
                flash("danger", "Gebruiker met ID " + userId + " bestaat niet.");
                return badRequest(upcomingSessionsList());
            }

            EnrollementStatus enrollStatus = Enum.valueOf(EnrollementStatus.class, status);

            dao.setUserEnrollmentStatus(is, user, enrollStatus);
            context.commit();
            flash("success", "De gebruikerstatus werd succesvol aangepast.");
            return detail(sessionId);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    @RoleSecured.RoleAuthenticated()
    public static Result enrollSession(int sessionId) {
        User user = DatabaseHelper.getUserProvider().getUser(session("email"));
        if (user.getStatus() == UserStatus.REGISTERED) {
            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                InfoSessionDAO dao = context.getInfoSessionDAO();

                InfoSession alreadyAttending = dao.getAttendingInfoSession(user);
                if (alreadyAttending != null && alreadyAttending.getTime().isAfter(DateTime.now())) {
                    flash("danger", "U bent reeds al ingeschreven voor een infosessie.");
                    return badRequest(upcomingSessionsList());
                } else {
                    InfoSession session = dao.getInfoSession(sessionId, false);
                    if (session == null) {
                        flash("danger", "Sessie met ID = " + sessionId + " bestaat niet.");
                        return badRequest(upcomingSessionsList());
                    } else {
                        try {
                            dao.registerUser(session, user);
                            context.commit();
                            flash("success", "U bent succesvol ingeschreven voor de infosessie op " + session.getTime().toString("dd/MM/yyyy") + ".");
                            return ok(upcomingSessionsList());
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
            flash("danger", "U bent reeds een geverifieerde gebruiker.");
            return badRequest(upcomingSessionsList());
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
            return badRequest(newInfosession.render(createForm, 0));
        } else {
            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                InfoSessionDAO dao = context.getInfoSessionDAO();

                try {
                    User user = DatabaseHelper.getUserProvider().getUser(session("email"));

                    AddressDAO adao = context.getAddressDAO();
                    Address address = adao.createAddress(createForm.get().address_zip, createForm.get().address_city, createForm.get().address_street, createForm.get().address_number, createForm.get().address_bus);

                    InfoSession session = dao.createInfoSession(user, address, createForm.get().getDateTime()); //TODO: allow other hosts
                    context.commit();

                    if (session != null) {
                        return redirect(
                                routes.InfoSessions.showUpcomingSessions() // return to infosession list
                        );
                    } else {
                        createForm.error("Failed to create session in database. Contact administrator.");
                        return badRequest(newInfosession.render(createForm, 0));
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

    private static Html upcomingSessionsList() {
        User user = DatabaseHelper.getUserProvider().getUser(session("email"));
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            InfoSessionDAO dao = context.getInfoSessionDAO();
            InfoSession enrolled = dao.getAttendingInfoSession(user);

            List<InfoSession> sessions = dao.getInfoSessionsAfter(DateTime.now());
            return infosessions.render(sessions, enrolled);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    @RoleSecured.RoleAuthenticated()
    public static Result showUpcomingSessions() {
        return ok(upcomingSessionsList());
    }
}
