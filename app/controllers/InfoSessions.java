package controllers;

import controllers.Security.RoleSecured;
import database.*;
import database.fields.FilterField;
import database.jdbc.JDBCFilter;
import models.*;
import notifiers.Notifier;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.api.templates.Html;
import play.data.Form;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.infosession.*;

import java.util.List;

/**
 * Created by Cedric on 2/21/14.
 */
public class InfoSessions extends Controller {

    private static final int PAGE_SIZE = 10;

    private static final boolean SHOW_MAP = true; //TODO: put in config dashboard later

    private static final DateTimeFormatter DATEFORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"); //ISO time without miliseconds

    public static class InfoSessionCreationModel {
        public String time; //TODO: use date format here
        public int max_enrollees;
        public InfoSessionType type;

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
            if (time == null || time.isEmpty()) {
                return "Gelieve het tijdsveld in te vullen";
            } else if (DateTime.now().isAfter(getDateTime())) {
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
    @RoleSecured.RoleAuthenticated(value = {UserRole.INFOSESSION_ADMIN})
    public static Result newSession() {
        User user = DatabaseHelper.getUserProvider().getUser(session("email"));

        if (user.getAddressDomicile() != null) {
            InfoSessionCreationModel model = new InfoSessionCreationModel();
            model.address_city = user.getAddressDomicile().getCity();
            model.address_zip = user.getAddressDomicile().getZip();
            model.address_street = user.getAddressDomicile().getStreet();
            model.address_number = user.getAddressDomicile().getNumber();
            model.address_bus = user.getAddressDomicile().getBus();

            Form<InfoSessionCreationModel> editForm = Form.form(InfoSessionCreationModel.class).fill(model);
            return ok(addinfosession.render(editForm, 0));
        } else return ok(addinfosession.render(Form.form(InfoSessionCreationModel.class), 0));
    }

    /**
     * Method: GET
     *
     * @param sessionId
     * @return
     */
    @RoleSecured.RoleAuthenticated(value = {UserRole.INFOSESSION_ADMIN})
    public static Result editSession(int sessionId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            InfoSessionDAO dao = context.getInfoSessionDAO();
            InfoSession is = dao.getInfoSession(sessionId, false);
            if (is == null) {
                flash("danger", "Infosessie met ID=" + sessionId + " bestaat niet.");
                return badRequest(upcomingSessionsList());
            } else {
                InfoSessionCreationModel model = new InfoSessionCreationModel();
                model.type = is.getType();
                model.time = is.getTime().toString(DATEFORMATTER);
                model.max_enrollees = is.getMaxEnrollees();
                model.address_city = is.getAddress().getCity();
                model.address_zip = is.getAddress().getZip();
                model.address_street = is.getAddress().getStreet();
                model.address_number = is.getAddress().getNumber();
                model.address_bus = is.getAddress().getBus();

                Form<InfoSessionCreationModel> editForm = Form.form(InfoSessionCreationModel.class).fill(model);
                return ok(addinfosession.render(editForm, sessionId));
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     *
     * @param sessionId
     * @return
     */
    @RoleSecured.RoleAuthenticated(value = {UserRole.INFOSESSION_ADMIN})
    public static Result removeSession(int sessionId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            InfoSessionDAO dao = context.getInfoSessionDAO();
            try {
                if (dao.getInfoSession(sessionId, false) == null) {
                    flash("danger", "Deze infosessie bestaat niet.");
                    return redirect(routes.InfoSessions.showUpcomingSessions());
                } else {
                    dao.deleteInfoSession(sessionId);
                    context.commit();
                    flash("success", "De infosessie werd succesvol verwijderd.");
                    return redirect(routes.InfoSessions.showUpcomingSessions());
                }
            } catch (DataAccessException ex) {
                context.rollback();
                throw ex;
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: POST
     *
     * @param sessionId
     * @return
     */
    @RoleSecured.RoleAuthenticated(value = {UserRole.INFOSESSION_ADMIN})
    public static Result editSessionPost(int sessionId) {
        Form<InfoSessionCreationModel> editForm = Form.form(InfoSessionCreationModel.class).bindFromRequest();
        if (editForm.hasErrors()) {
            return badRequest(addinfosession.render(editForm, sessionId));
        } else {
            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                InfoSessionDAO dao = context.getInfoSessionDAO();
                InfoSession session = dao.getInfoSession(sessionId, false);
                if (session == null) {
                    flash("danger", "Infosessie met ID=" + sessionId + " bestaat niet.");
                    return redirect(routes.InfoSessions.showUpcomingSessions());
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

                    //TODO: update maxEnrollees, type SAVE (CHECK IF #ATTENDEES < NEW MAX!!!)
                    context.commit();
                    flash("success", "Uw wijzigingen werden succesvol toegepast.");
                    return redirect(routes.InfoSessions.detail(sessionId));
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
                return redirect(routes.InfoSessions.showUpcomingSessions());
            } else {
                try {
                    dao.unregisterUser(alreadyAttending, user);
                    context.commit();

                    flash("success", "U bent succesvol uitgeschreven uit deze infosessie.");
                    return redirect(routes.InfoSessions.showUpcomingSessions());
                } catch (DataAccessException ex) {
                    context.rollback();
                    throw ex;
                }
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /*@RoleSecured.RoleAuthenticated()
    public static Result detail(int sessionId) {

        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            InfoSessionDAO dao = context.getInfoSessionDAO();
            InfoSession session = dao.getInfoSession(sessionId, true);
            if (session == null) {
                flash("danger", "Infosessie met ID=" + sessionId + " bestaat niet.");
                return redirect(routes.InfoSessions.showUpcomingSessions());
            } else {
                InfoSession enrolled = dao.getAttendingInfoSession(user);
                return ok(detail.render(session, enrolled));
            }
        } catch (DataAccessException ex) {
            throw ex;
            //TODO: log
        }
    }*/

    @RoleSecured.RoleAuthenticated()
    public static F.Promise<Result> detail(int sessionId){
        final User user = DatabaseHelper.getUserProvider().getUser(session("email"));
        try(DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            final InfoSessionDAO dao = context.getInfoSessionDAO();
            final InfoSession session = dao.getInfoSession(sessionId, true);
            if(session == null){
                return F.Promise.promise(new F.Function0<Result>() {
                    @Override
                    public Result apply() throws Throwable {
                        return badRequest("Sessie id bestaat niet.");
                    }
                });
            } else {
                final InfoSession enrolled = dao.getAttendingInfoSession(user);
                if (SHOW_MAP) {
                    return Maps.getLatLongPromise(session.getAddress().getId()).map(
                            new F.Function<F.Tuple<Double, Double>, Result>() {
                                public Result apply(F.Tuple<Double, Double> coordinates) {
                                    return ok(detail.render(session, enrolled,
                                            coordinates == null ? null : new Maps.MapDetails(coordinates._1, coordinates._2, 14, "Afspraak om " + session.getTime().toLocalDate())));
                                }
                            }
                    );
                } else {
                    return F.Promise.promise(new F.Function0<Result>() {
                        @Override
                        public Result apply() throws Throwable {
                            return ok(detail.render(session, enrolled, null));
                        }
                    });
                }
            }
        } catch(DataAccessException ex){
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
    @RoleSecured.RoleAuthenticated(value = {UserRole.INFOSESSION_ADMIN})
    public static Result removeUserFromSession(int sessionId, int userId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            InfoSessionDAO dao = context.getInfoSessionDAO();
            InfoSession is = dao.getInfoSession(sessionId, false);
            if (is == null) {
                flash("danger", "Infosessie met ID " + sessionId + " bestaat niet.");
                return redirect(routes.InfoSessions.showUpcomingSessions());
            }

            UserDAO udao = context.getUserDAO();

            User user = udao.getUser(userId, false);
            if (user == null) {
                flash("danger", "Gebruiker met ID " + userId + " bestaat niet.");
                return redirect(routes.InfoSessions.showUpcomingSessions());
            }

            dao.unregisterUser(is, user);
            context.commit();

            flash("success", "De gebruiker werd succesvol uitgeschreven uit de infosessie.");
            return redirect(routes.InfoSessions.detail(sessionId));
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     * @param sessionId
     * @param userId
     * @param status
     * @return
     */
    @RoleSecured.RoleAuthenticated(value = {UserRole.INFOSESSION_ADMIN})
    public static Result setUserSessionStatus(int sessionId, int userId, String status) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {

            InfoSessionDAO dao = context.getInfoSessionDAO();
            InfoSession is = dao.getInfoSession(sessionId, false);
            if (is == null) {
                flash("danger", "Infosessie met ID " + sessionId + " bestaat niet.");
                return redirect(routes.InfoSessions.showUpcomingSessions());
            }

            UserDAO udao = context.getUserDAO();
            User user = udao.getUser(userId, false);
            if (user == null) {
                flash("danger", "Gebruiker met ID " + userId + " bestaat niet.");
                return redirect(routes.InfoSessions.showUpcomingSessions());
            }

            EnrollementStatus enrollStatus = Enum.valueOf(EnrollementStatus.class, status);

            dao.setUserEnrollmentStatus(is, user, enrollStatus);
            context.commit();
            flash("success", "De gebruikerstatus werd succesvol aangepast.");
            return redirect(routes.InfoSessions.detail(sessionId));
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     * @param sessionId
     * @return
     */
    @RoleSecured.RoleAuthenticated()
    public static Result enrollSession(int sessionId) {
        User user = DatabaseHelper.getUserProvider().getUser(session("email"));
        if (user.getStatus() == UserStatus.REGISTERED) {
            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                InfoSessionDAO dao = context.getInfoSessionDAO();

                InfoSession alreadyAttending = dao.getAttendingInfoSession(user);
                if (alreadyAttending != null && alreadyAttending.getTime().isAfter(DateTime.now())) {
                    flash("danger", "U bent reeds al ingeschreven voor een infosessie.");
                    return redirect(routes.InfoSessions.showUpcomingSessions());
                } else {
                    InfoSession session = dao.getInfoSession(sessionId, true); //TODO: just add going subclause (like in getAttending requirement)
                    if (session == null) {
                        flash("danger", "Sessie met ID = " + sessionId + " bestaat niet.");
                        return redirect(routes.InfoSessions.showUpcomingSessions());
                    } else if (session.getMaxEnrollees() != 0 && session.getEnrolleeCount() == session.getMaxEnrollees()) {
                        flash("danger", "Deze infosessie zit reeds vol.");
                        return redirect(routes.InfoSessions.showUpcomingSessions());
                    } else {
                        try {
                            dao.registerUser(session, user);
                            context.commit();
                            flash("success", "U bent succesvol ingeschreven voor de infosessie op " + session.getTime().toString("dd/MM/yyyy") + ".");
                            Notifier.sendInfoSessionEnrolledMail(user, session);
                            return redirect(routes.InfoSessions.detail(sessionId));
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
            return redirect(routes.InfoSessions.showUpcomingSessions());
        }
    }


    /**
     * Method: POST
     *
     * @return
     */
    @RoleSecured.RoleAuthenticated(value = {UserRole.INFOSESSION_ADMIN})
    public static Result createNewSession() {
        Form<InfoSessionCreationModel> createForm = Form.form(InfoSessionCreationModel.class).bindFromRequest();
        if (createForm.hasErrors()) {
            return badRequest(addinfosession.render(createForm, 0));
        } else {
            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                InfoSessionDAO dao = context.getInfoSessionDAO();

                try {
                    User user = DatabaseHelper.getUserProvider().getUser(session("email"));

                    AddressDAO adao = context.getAddressDAO();
                    Address address = adao.createAddress("Belgium", createForm.get().address_zip, createForm.get().address_city, createForm.get().address_street, createForm.get().address_number, createForm.get().address_bus);

                    //TODO: read InfoSessionType from form
                    InfoSession session = dao.createInfoSession(InfoSessionType.NORMAL, user, address, createForm.get().getDateTime(), createForm.get().max_enrollees); //TODO: allow other hosts
                    context.commit();

                    if (session != null) {
                        return redirect(
                                routes.InfoSessions.showUpcomingSessions() // return to infosession list
                        );
                    } else {
                        createForm.error("Failed to create session in database. Contact administrator.");
                        return badRequest(addinfosession.render(createForm, 0));
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


    @RoleSecured.RoleAuthenticated()
    public static F.Promise<Result> showUpcomingSessions() {
        User user = DatabaseHelper.getUserProvider().getUser(session("email"));
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            InfoSessionDAO dao = context.getInfoSessionDAO();
            final InfoSession enrolled = dao.getAttendingInfoSession(user);

            if(enrolled == null || !SHOW_MAP){
                return F.Promise.promise(new F.Function0<Result>() {
                    @Override
                    public Result apply() throws Throwable {
                        return ok(infosessions.render(enrolled, null));
                    }
                });
            } else {
                    return Maps.getLatLongPromise(enrolled.getAddress().getId()).map(
                            new F.Function<F.Tuple<Double, Double>, Result>() {
                                public Result apply(F.Tuple<Double, Double> coordinates) {
                                    return ok(infosessions.render(enrolled,
                                            coordinates == null ? null : new Maps.MapDetails(coordinates._1, coordinates._2, 14, "Afspraak om " + enrolled.getTime().toLocalDate())));
                                }
                            }
                    );
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    @RoleSecured.RoleAuthenticated()
    public static Result showUpcomingSessionsPage(int page, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField filterField = FilterField.stringToField(orderBy);

        // TODO: create asc and filter in method
        boolean asc = ascInt == 1;

        Filter filter = new JDBCFilter();
        if(searchString != "") {
            String[] searchStrings = searchString.split(",");
            for(String s : searchStrings) {
                String[] s2 = s.split("=");
                if(s2.length == 2) {
                    String field = s2[0];
                    String value = s2[1];
                    filter.fieldContains(FilterField.stringToField(field), value);
                }
            }
        }
        return ok(upcommingSessionsList(page, filterField, asc, filter));
    }

    private static Html upcomingSessionsList() {
        return upcommingSessionsList(1, FilterField.DATE, true, null);
    }
    private static Html upcommingSessionsList(int page, FilterField orderBy, boolean asc, Filter filter) {

        User user = DatabaseHelper.getUserProvider().getUser(session("email"));
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            InfoSessionDAO dao = context.getInfoSessionDAO();
            InfoSession enrolled = dao.getAttendingInfoSession(user);
            if(orderBy == null) {
                orderBy = FilterField.DATE;
            }
            List<InfoSession> sessions = dao.getInfoSessionsAfter(DateTime.now(), orderBy, asc, page, PAGE_SIZE, filter);
            if (enrolled != null) {
                //TODO: Fix this by also including going count in getAttendingInfoSession (now we fetch it from other list)
                // Hack herpedy derp!!

                for (InfoSession s : sessions) {
                    if (enrolled.getId() == s.getId()) {
                        enrolled = s;
                        break;
                    }
                }
            }

            int amountOfResults = dao.getAmountOfInfoSessions(filter);
            int amountOfPages = (int) Math.ceil( amountOfResults / (double) PAGE_SIZE);

            // TODO amount of results and amount of pages
            return infosessionspage.render(sessions, enrolled, page, amountOfResults, amountOfPages);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }
}
