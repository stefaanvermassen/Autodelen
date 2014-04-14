package controllers;

import controllers.Security.RoleSecured;
import controllers.util.FormHelper;
import controllers.util.Pagination;
import database.*;
import database.FilterField;
import models.*;
import notifiers.Notifier;
import org.joda.time.DateTime;
import play.api.templates.Html;
import play.data.Form;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.infosession.*;

import java.sql.Timestamp;
import java.util.ArrayList;

import java.util.List;

/**
 * Created by Cedric on 2/21/14.
 */
public class InfoSessions extends Controller {

    private static final int PAGE_SIZE = 10;

    private static final boolean SHOW_MAP = true; //TODO: put in config dashboard later

    public static class InfoSessionCreationModel {
        public DateTime time;
        public Integer max_enrollees;
        public InfoSessionType type;

        // Address fields
        public String address_city;
        public String address_zip;
        public String address_street;
        public String address_number;
        public String address_bus;

        public static int getInt(Integer i) {
            return i == null ? 0 : i;
        }

        public String validate() {
            if (time == null) {
                return "Gelieve het tijdsveld in te vullen";
            } else if (DateTime.now().isAfter(time)) {
                return "Je kan enkel een infosessie plannen na de huidige datum.";
            }
            return null;
        }

    }

    //TODO: allow infosessions to be created by another use than the hoster

    /**
     * Method: GET
     *
     * @return An infosession form
     */
    @RoleSecured.RoleAuthenticated(value = {UserRole.INFOSESSION_ADMIN})
    public static Result newSession() {
        User user = DatabaseHelper.getUserProvider().getUser();

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
     * @param sessionId SessionId to edit
     * @return An infosession form for given id
     */
    @RoleSecured.RoleAuthenticated(value = {UserRole.INFOSESSION_ADMIN})
    public static Result editSession(int sessionId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            InfoSessionDAO dao = context.getInfoSessionDAO();
            InfoSession is = dao.getInfoSession(sessionId, false);
            if (is == null) {
                flash("danger", "Infosessie met ID=" + sessionId + " bestaat niet.");
                return redirect(routes.InfoSessions.showUpcomingSessions());
            } else {
                InfoSessionCreationModel model = new InfoSessionCreationModel();
                model.type = is.getType();
                model.time = is.getTime();
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
     * @param sessionId SessionID to remove
     * @return A result redirect whether delete was successfull or not.
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
     * Edits the session for given ID, based on submitted form data
     *
     * @param sessionId SessionID to edit
     * @return Redirect to edited session, or the form if errors occured
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
                    DateTime time = editForm.get().time.withSecondOfMinute(0);
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

    /**
     * Method: GET
     * Unenrolls the user for his subscribed infosession.
     *
     * @return A redirect to the overview page with message if unenrollment was successfull.
     */
    @RoleSecured.RoleAuthenticated()
    public static Result unenrollSession() {
        User user = DatabaseHelper.getUserProvider().getUser();
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

    /**
     * Method: GET
     * Returns the detail promise of the given sessionId. If enabled, this also fetches map location and enables the map view.
     *
     * @param sessionId The sessionId to which the detail belongs to
     * @return A session detail page promise
     */
    @RoleSecured.RoleAuthenticated()
    public static F.Promise<Result> detail(int sessionId) {
        final User user = DatabaseHelper.getUserProvider().getUser();
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            final InfoSessionDAO dao = context.getInfoSessionDAO();
            final InfoSession session = dao.getInfoSession(sessionId, true);
            if (session == null) {
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
                                            coordinates == null ? null : new Maps.MapDetails(coordinates._1, coordinates._2, 14, "Afspraak om " + session.getTime().toString("yyyy-MM-dd HH:mm:ss"))));
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
     *
     * @param sessionId SessionID to change userstatus on
     * @param userId    UserID of the user to change status of
     * @param status    New status of the user.
     * @return Redirect to the session detail page if successful.
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
     *
     * @param sessionId The sessionId to enroll to
     * @return A redirect to the detail page to which the user has subscribed
     */
    @RoleSecured.RoleAuthenticated()
    public static Result enrollSession(int sessionId) {
        User user = DatabaseHelper.getUserProvider().getUser();
        if (user.getStatus() == UserStatus.REGISTERED) {
            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                InfoSessionDAO dao = context.getInfoSessionDAO();

                InfoSession alreadyAttending = dao.getAttendingInfoSession(user);
                InfoSession session = dao.getInfoSession(sessionId, true); //TODO: just add going subclause (like in getAttending requirement)
                if (session == null) {
                    flash("danger", "Sessie met ID = " + sessionId + " bestaat niet.");
                    return redirect(routes.InfoSessions.showUpcomingSessions());
                } else {
                    if (session.getMaxEnrollees() != 0 && session.getEnrolleeCount() == session.getMaxEnrollees()) {
                        flash("danger", "Deze infosessie zit reeds vol.");
                        return redirect(routes.InfoSessions.showUpcomingSessions());
                    } else {
                        try {
                            if (alreadyAttending != null && alreadyAttending.getTime().isAfter(DateTime.now())) {
                                dao.unregisterUser(alreadyAttending, user);
                            }
                            dao.registerUser(session, user);

                            context.commit();
                            flash("success", alreadyAttending == null ? ("U bent succesvol ingeschreven voor de infosessie op " + session.getTime().toString("dd/MM/yyyy") + ".") :
                                    "U bent van infosessie veranderd naar " + session.getTime().toString("dd/MM/yyyy"));
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
     * Creates a new infosession based on submitted form data
     *
     * @return A redirect to the newly created infosession, or the infosession edit page if the form contains errors.
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
                    User user = DatabaseHelper.getUserProvider().getUser();

                    AddressDAO adao = context.getAddressDAO();
                    Address address = adao.createAddress("Belgium", createForm.get().address_zip, createForm.get().address_city, createForm.get().address_street, createForm.get().address_number, createForm.get().address_bus);

                    //TODO: read InfoSessionType from form
                    InfoSession session = dao.createInfoSession(InfoSessionType.NORMAL, user, address, createForm.get().time.withSecondOfMinute(0), FormHelper.toInt(createForm.get().max_enrollees)); //TODO: allow other hosts
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

    /**
     * Method: GET
     * A page to request full user approval
     * @return The page to request approval
     */
    @RoleSecured.RoleAuthenticated()
    public static Result requestApproval(){
        User user = DatabaseHelper.getUserProvider().getUser();
        List<String> errors = new ArrayList<>();
        if(DatabaseHelper.getUserRoleProvider().hasRole(user, UserRole.CAR_OWNER) && DatabaseHelper.getUserRoleProvider().hasRole(user, UserRole.CAR_USER)) {
            errors.add("U bent reeds een geaccepteerd lid als deler en gebruiker.");
        } else {
            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                UserDAO udao = context.getUserDAO();
                user = udao.getUser(user.getId(), true); // gets the full user instead of small cached one

                ApprovalDAO dao = context.getApprovalDAO();
                List<Approval> approvals = dao.getPendingApprovals(user);//TODO: just request a COUNT instead of fetching the list

                if (!approvals.isEmpty()) {
                    errors.add("Er is reeds een toelatingsprocedure in aanvraag.");
                } else {
                    if(user.getAddressDomicile() == null)
                        errors.add("Domicilieadres ontbreekt.");
                    if(user.getAddressResidence() == null)
                        errors.add("Verblijfsadres ontbreekt.");
                    if(user.getIdentityCard() == null)
                        errors.add("Identiteitskaart ontbreekt.");
                    if(user.getCellphone() == null && user.getPhone() == null)
                        errors.add("Telefoon/GSM ontbreekt.");

                    //TODO: check if attended infosession last month and status was 'Attended'
                }
            } catch (DataAccessException ex) {
                throw ex;
            }
        }
        return errors.isEmpty() ? ok(approvalrequest.render(user, null)) : badRequest(approvalrequest.render(user, errors));
    }

    /**
     * Method: GET
     * Returns the promise of list of the upcoming infosessions. When the user is enrolled already this also includes map data if enabled
     *
     * @return
     */
    @RoleSecured.RoleAuthenticated()
    public static F.Promise<Result> showUpcomingSessions() {
        User user = DatabaseHelper.getUserProvider().getUser();
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            InfoSessionDAO dao = context.getInfoSessionDAO();
            final InfoSession enrolled = dao.getAttendingInfoSession(user);

            if (enrolled == null || !SHOW_MAP) {
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
                                        coordinates == null ? null : new Maps.MapDetails(coordinates._1, coordinates._2, 14, "Afspraak om " + enrolled.getTime().toString("yyyy-MM-dd HH:mm:ss"))));
                            }
                        }
                );
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     * Returns the promise of list of the upcoming infosessions. When the user is enrolled already this also includes map data if enabled
     *
     * @return
     */
    @RoleSecured.RoleAuthenticated({UserRole.INFOSESSION_ADMIN})
    public static Result showSessions() {
        return ok(infosessionsAdmin.render());

    }

    /**
     * Method: GET
     *
     * @param page         The page number to fetch
     * @param ascInt
     * @param orderBy      The orderby type, ASC or DESC
     * @param searchString The string to search for
     * @return A partial view of the table containing the filtered upcomming sessions
     */
    @RoleSecured.RoleAuthenticated()
    public static Result showUpcomingSessionsPage(int page, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField filterField = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);
        filter.putValue(FilterField.FROM, DateTime.now().toString());
        filter.putValue(FilterField.UNTIL, "" + DateTime.now().plusYears(100).toString());

        return ok(sessionsList(page, filterField, asc, filter, false));
    }

    /**
     * Method: GET
     *
     * @param page         The page number to fetch
     * @param ascInt
     * @param orderBy      The orderby type, ASC or DESC
     * @param searchString The string to search for
     * @return A partial view of the table containing the filtered sessions
     */
    @RoleSecured.RoleAuthenticated({UserRole.INFOSESSION_ADMIN})
    public static Result showSessionsPage(int page, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        System.out.println(searchString);
        FilterField filterField = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);
        // If no from and until is specified: show infosessions from now until 100 years from now
        // TODO: find better solution for ugly equals YYYY-MM-DD hh:mm
        if(filter.getValue(FilterField.FROM).equals("") || filter.getValue(FilterField.FROM).equals("YYYY-MM-DD hh:mm")) {
            filter.putValue(FilterField.FROM, DateTime.now().toString());
        }
        if(filter.getValue(FilterField.UNTIL).equals("") || filter.getValue(FilterField.UNTIL).equals("YYYY-MM-DD hh:mm")) {
            filter.putValue(FilterField.UNTIL, "" + DateTime.now().plusYears(100).toString());
        }

        return ok(sessionsList(page, filterField, asc, filter, true));
    }

    /**
     * Gets the infosessions html block filtered
     *
     * @param page
     * @param orderBy Orderby type ASC or DESC
     * @param asc
     * @param filter  The filter to apply to
     * @return The html patial table of upcoming sessions for this filter
     */
    private static Html sessionsList(int page, FilterField orderBy, boolean asc, Filter filter, boolean admin) {
        // TODO: not use boolean admin
        User user = DatabaseHelper.getUserProvider().getUser();
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            InfoSessionDAO dao = context.getInfoSessionDAO();
            InfoSession enrolled = dao.getAttendingInfoSession(user);
            if(orderBy == null) {
                orderBy = FilterField.INFOSESSION_DATE;
            }

            List<InfoSession> sessions = dao.getInfoSessions(orderBy, asc, page, PAGE_SIZE, filter);
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
            int amountOfPages = (int) Math.ceil(amountOfResults / (double) PAGE_SIZE);
            if(admin)
                return infosessionsAdminPage.render(sessions, page, amountOfResults, amountOfPages);
            else
                return infosessionspage.render(sessions, enrolled, page, amountOfResults, amountOfPages);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }
}
