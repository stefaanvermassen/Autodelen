package controllers;

import controllers.Security.RoleSecured;
import controllers.util.Addresses;
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
import views.html.infosession.addinfosession;
import views.html.infosession.*;


import java.sql.Timestamp;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static controllers.util.Addresses.getCountryList;
import static controllers.util.Addresses.modifyAddress;

/**
 * Created by Cedric on 2/21/14.
 */
public class InfoSessions extends Controller {

    private static final int PAGE_SIZE = 10;

    private static final boolean SHOW_MAP = true; //TODO: put in config dashboard later

    private static List<String> typeList = null;

    private static List<String> getTypeList() {
        if(typeList == null) {
            typeList = new ArrayList<>();
            InfoSessionType[] types = InfoSessionType.values();
            for(InfoSessionType t : types) {
                typeList.add(t.getDescription());
            }
        }
        return typeList;
    }

    public static class InfoSessionCreationModel {
        public String userEmail;
        public DateTime time;
        public Integer max_enrollees;
        public String type;
        public String type_alternative;
        public String comments;
        public Addresses.EditAddressModel address = new Addresses.EditAddressModel();

        public static int getInt(Integer i) {
            return i == null ? 0 : i;
        }

        public String validate() {
            String error = "";
            if(userEmail == null || userEmail.equals("")) {
                error += "Gelieve een host te selecteren. ";
            }
            if (time == null) {
                error += "Gelieve het tijdsveld in te vullen. ";
            }
            if(InfoSessionType.getTypeFromString(type) == InfoSessionType.OTHER && (type_alternative == null || type_alternative.equals(""))) {
                error += "Gelieve een alternatief type in te geven of een ander type te selecteren. ";
            }
            if("".equals(error)) return null;
            else return error;
        }

        public void populate(InfoSession i) {
            if(i == null) return;

            userEmail = i.getHost().getEmail();
            time = i.getTime();
            max_enrollees = i.getMaxEnrollees();
            type = i.getType().getDescription();
            type_alternative = i.getTypeAlternative();
            comments = i.getComments();
            address.populate(i.getAddress());
        }

    }

    /**
     * Method: GET
     *
     * @return An infosession form
     */
    @RoleSecured.RoleAuthenticated(value = {UserRole.INFOSESSION_ADMIN})
    public static Result newSession() {
        User user = DatabaseHelper.getUserProvider().getUser();
        Form<InfoSessionCreationModel> editForm = Form.form(InfoSessionCreationModel.class);

        InfoSessionCreationModel model = new InfoSessionCreationModel();
        model.userEmail = user.getEmail();
        model.address.populate(user.getAddressDomicile());
        model.type = InfoSessionType.NORMAL.getDescription();
        editForm = editForm.fill(model);
        return ok(addinfosession.render(editForm, 0, getCountryList(), getTypeList()));
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
                model.populate(is);

                Form<InfoSessionCreationModel> editForm = Form.form(InfoSessionCreationModel.class).fill(model);
                return ok(addinfosession.render(editForm, sessionId, getCountryList(), getTypeList()));
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
            return badRequest(addinfosession.render(editForm, sessionId, getCountryList(), getTypeList()));
        } else {
            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                InfoSessionDAO dao = context.getInfoSessionDAO();
                InfoSession session = dao.getInfoSession(sessionId, false);
                if (session == null) {
                    flash("danger", "Infosessie met ID=" + sessionId + " bestaat niet.");
                    return redirect(routes.InfoSessions.showUpcomingSessions());
                }

                try {
                    // update address
                    AddressDAO adao = context.getAddressDAO();
                    Address newAddress = modifyAddress(editForm.get().address, session.getAddress(), adao);
                    session.setAddress(newAddress);

                    // update time
                    DateTime time = editForm.get().time.withSecondOfMinute(0);
                    session.setTime(time);

                    // check if amountOfAttendees < new max
                    int amountOfAttendees = dao.getAmountOfAttendees(session.getId());
                    if(editForm.get().max_enrollees != 0 && editForm.get().max_enrollees < amountOfAttendees) {
                        flash("danger", "Er zijn al meer inschrijvingen dan het nieuwe toegelaten aantal. Aantal huidige inschrijvingen: " + amountOfAttendees + ".");
                        return badRequest(addinfosession.render(editForm, sessionId, getCountryList(), getTypeList()));
                    } else {
                        session.setMaxEnrollees(editForm.get().max_enrollees);
                    }

                    // type
                    InfoSessionType type = InfoSessionType.getTypeFromString(editForm.get().type);
                    session.setType(type);
                    String typeAlternative = null;
                    if(type.equals(InfoSessionType.OTHER)) {
                        typeAlternative = editForm.get().type_alternative;
                    }
                    session.setTypeAlternative(typeAlternative);


                    // host
                    User host = DatabaseHelper.getUserProvider().getUser(editForm.get().userEmail);
                    session.setHost(host);

                    // comments
                    session.setComments(editForm.get().comments);

                    dao.updateInfoSession(session);
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
                                            coordinates == null ? null : new Maps.MapDetails(coordinates._1, coordinates._2, 14, "Afspraak op " + session.getTime().toString("dd-MM-yyyy") + " om " + session.getTime().toString("HH:mm"))));
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
                            flash("success", alreadyAttending == null ? ("U bent succesvol ingeschreven voor de infosessie op " + session.getTime().toString("dd-MM-yyyy") + ".") :
                                    "U bent van infosessie veranderd naar " + session.getTime().toString("dd-MM-yyyy") + ".");
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
            return badRequest(addinfosession.render(createForm, 0, getCountryList(), getTypeList()));
        } else {
            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                InfoSessionDAO dao = context.getInfoSessionDAO();

                try {
                    AddressDAO adao = context.getAddressDAO();
                    Address address = modifyAddress(createForm.get().address, null, adao);

                    User host = DatabaseHelper.getUserProvider().getUser(createForm.get().userEmail);

                    InfoSessionType type = InfoSessionType.getTypeFromString(createForm.get().type);
                    String typeAlternative = null;
                    if(type.equals(InfoSessionType.OTHER)) {
                        typeAlternative = createForm.get().type_alternative;
                    }

                    InfoSession session = dao.createInfoSession(type, typeAlternative, host, address, createForm.get().time.withSecondOfMinute(0), FormHelper.toInt(createForm.get().max_enrollees), createForm.get().comments); //TODO: allow other hosts
                    context.commit();

                    if (session != null) {
                        return redirect(
                                routes.InfoSessions.showUpcomingSessions() // return to infosession list
                        );
                    } else {
                        createForm.error("Failed to create session in database. Contact administrator.");
                        return badRequest(addinfosession.render(createForm, 0, getCountryList(), getTypeList()));
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

    public static class RequestApprovalModel {
        public String message;
        public boolean acceptsTerms;

        public String validate() {
            if (!acceptsTerms)
                return "Gelieve de algemene voorwaarden te accepteren";
            else
                return null;
        }
    }

    private static List<String> checkApprovalConditions(User user, DataAccessContext context) {

        UserDAO udao = context.getUserDAO();
        FileDAO fdao = context.getFileDAO();
        user = udao.getUser(user.getId(), true); // gets the full user instead of small cached one
        if (user.getIdentityCard() != null && user.getIdentityCard().getFileGroup() != null) {
            // TODO: fix identity card dao so this line is unnecessary
            user.getIdentityCard().setFileGroup(fdao.getFiles(user.getIdentityCard().getFileGroup().getId()));
            user.getDriverLicense().setFileGroup(fdao.getFiles(user.getDriverLicense().getFileGroup().getId()));
        }

        ApprovalDAO dao = context.getApprovalDAO();
        InfoSessionDAO idao = context.getInfoSessionDAO();
        Tuple<InfoSession, EnrollementStatus> lastSession = idao.getLastInfoSession(user);
        List<Approval> approvals = dao.getPendingApprovals(user);//TODO: just request a COUNT instead of fetching the list

        List<String> errors = new ArrayList<>();
        if (!approvals.isEmpty()) {
            errors.add("Er is reeds een toelatingsprocedure in aanvraag.");
        } else {
            if (user.getAddressDomicile() == null)
                errors.add("Domicilieadres ontbreekt.");
            if (user.getAddressResidence() == null)
                errors.add("Verblijfsadres ontbreekt.");
            if (user.getIdentityCard() == null)
                errors.add("Identiteitskaart ontbreekt.");
            if (user.getIdentityCard() != null && (user.getIdentityCard().getFileGroup() == null || user.getIdentityCard().getFileGroup().size() == 0))
                errors.add("Bewijsgegevens identiteitskaart ontbreken");
            if (user.getDriverLicense() == null)
                errors.add("Rijbewijs ontbreekt.");
            if (user.getDriverLicense() != null && (user.getDriverLicense().getFileGroup() == null || user.getDriverLicense().getFileGroup().size() == 0))
                if (user.getCellphone() == null && user.getPhone() == null)
                    errors.add("Telefoon/GSM ontbreekt.");
            if (lastSession == null || lastSession.getSecond() != EnrollementStatus.PRESENT)
                errors.add("U bent nog niet aanwezig geweest op een infosessie.");
        }
        return errors;
    }

    private static String getTermsAndConditions(DataAccessContext context){
        TemplateDAO dao = context.getTemplateDAO();
        EmailTemplate t = dao.getTemplate(MailType.TERMS);
        return t.getBody();
    }

    /**
     * Method: GET
     * A page to request full user approval
     *
     * @return The page to request approval
     */
    @RoleSecured.RoleAuthenticated()
    public static Result requestApproval() {
        User user = DatabaseHelper.getUserProvider().getUser();
        if (DatabaseHelper.getUserRoleProvider().isFullUser(user)) {
            flash("warning", "U bent reeds een volwaardige gebruiker.");
            return redirect(routes.Dashboard.index());
        } else {
            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                List<String> errors = checkApprovalConditions(user, context);
                return badRequest(approvalrequest.render(user, errors.isEmpty() ? null : errors, Form.form(RequestApprovalModel.class), getTermsAndConditions(context)));
            }
        }
    }

    @RoleSecured.RoleAuthenticated()
    public static Result requestApprovalPost() {
        User user = DatabaseHelper.getUserProvider().getUser();
        if (DatabaseHelper.getUserRoleProvider().isFullUser(user)) {
            flash("warning", "U bent reeds een volwaardige gebruiker.");
            return redirect(routes.Dashboard.index());
        } else {
            Form<RequestApprovalModel> form = Form.form(RequestApprovalModel.class).bindFromRequest();
            if (form.hasErrors()) {
                try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                    List<String> errors = checkApprovalConditions(user, context);
                    return badRequest(approvalrequest.render(user, errors.isEmpty() ? null : errors, form, getTermsAndConditions(context)));
                }
            } else {
                return ok("Received post");
            }
        }
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
                                        coordinates == null ? null : new Maps.MapDetails(coordinates._1, coordinates._2, 14, "Afspraak op " + enrolled.getTime().toString("dd-MM-yyyy") + " om " + enrolled.getTime().toString("HH:mm"))));
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
        FilterField filterField = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

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
            if (orderBy == null) {
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
            if (admin)
                return infosessionsAdminPage.render(sessions, page, amountOfResults, amountOfPages);
            else
                return infosessionspage.render(sessions, enrolled, page, amountOfResults, amountOfPages);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }
}
