package controllers;

import controllers.Security.RoleSecured;
import controllers.util.ConfigurationHelper;
import controllers.util.FileHelper;
import controllers.util.Pagination;
import database.*;
import models.*;
import providers.DataProvider;
import providers.UserRoleProvider;
import notifiers.Notifier;
import play.api.templates.Html;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.refuels.editmodal;
import views.html.refuels.refuels;
import views.html.refuels.refuelspage;
import views.html.refuels.refuelsOwner;
import views.html.refuels.refuelsAdmin;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 26/04/14.
 */
public class Refuels extends Controller {

    public static class RefuelModel {

        public BigDecimal amount;

        public String validate() {
            if(amount.compareTo(new BigDecimal(200)) == 1)
                return "Bedrag te hoog";
            return null;
        }
    }


    /**
     * Method: GET
     *
     * @return index page containing all the refuel requests from a specific user
     */
    @RoleSecured.RoleAuthenticated()
    public static Result showRefuels() {
        return ok(refuels.render());
    }

    public static Result showUserRefuelsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField field = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        User user = DataProvider.getUserProvider().getUser();
        filter.putValue(FilterField.REFUEL_USER_ID, user.getId() + "");

        // TODO: Check if admin or car owner/user

        return ok(refuelList(page, pageSize, field, asc, filter));

    }

    /**
     * Method: GET
     *
     * @return index page containing all the refuel requests to a specific owner
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER})
    public static Result showOwnerRefuels() {
        return ok(refuelsOwner.render());
    }

    public static Result showOwnerRefuelsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField field = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        User user = DataProvider.getUserProvider().getUser();
        filter.putValue(FilterField.REFUEL_OWNER_ID, user.getId() + "");
        filter.putValue(FilterField.REFUEL_NOT_STATUS, RefuelStatus.CREATED.toString());

        // TODO: Check if admin or car owner/user

        return ok(refuelList(page, pageSize, field, asc, filter));
    }

    /**
     * Method: GET
     *
     * @return index page containing all the refuel requests to a specific owner
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_ADMIN})
    public static Result showAllRefuels() {
        return ok(refuelsAdmin.render());
    }

    @RoleSecured.RoleAuthenticated({UserRole.CAR_ADMIN})
    public static Result showAllRefuelsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField field = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        return ok(refuelList(page, pageSize, field, asc, filter));
    }

    private static Html refuelList(int page, int pageSize, FilterField orderBy, boolean asc, Filter filter) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            RefuelDAO dao = context.getRefuelDAO();

            if(orderBy == null) {
                orderBy = FilterField.REFUEL_NOT_STATUS; // not neccessary, but orderBy cannot be null
            }
            List<Refuel> listOfResults = dao.getRefuels(orderBy, asc, page, pageSize, filter);

            int amountOfResults = dao.getAmountOfRefuels(filter);
            int amountOfPages = (int) Math.ceil( amountOfResults / (double) pageSize);

            return refuelspage.render(listOfResults, page, amountOfResults, amountOfPages);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     * @return modal to provide refuel information
     */
    @RoleSecured.RoleAuthenticated()
    public static Result provideRefuelInfo(int refuelId) {
        return ok(editmodal.render(Form.form(RefuelModel.class), refuelId));
    }

    /**
     * Method: POST
     * @return redirect to the index page containing all the refuel requests
     */
    @RoleSecured.RoleAuthenticated()
    public static Result provideRefuelInfoPost(int refuelId) {
        Form<RefuelModel> refuelForm = Form.form(RefuelModel.class).bindFromRequest();
        if (refuelForm.hasErrors()) {
            flash("danger", "Info verstrekken mislukt.");
            return redirect(routes.Refuels.showRefuels());

        } else {
            try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
                RefuelDAO dao = context.getRefuelDAO();
                try {
                    RefuelModel model = refuelForm.get();
                    Refuel refuel = dao.getRefuel(refuelId);
                    Http.MultipartFormData body = request().body().asMultipartFormData();
                    Http.MultipartFormData.FilePart proof = body.getFile("picture");
                    if (proof != null) {
                        String contentType = proof.getContentType();
                        if (!FileHelper.isDocumentContentType(contentType)) {
                            flash("danger", "Verkeerd bestandstype opgegeven. Enkel documenten zijn toegelaten. (ontvangen MIME-type: " + contentType + ")");
                            return redirect(routes.Refuels.showRefuels());
                        } else {
                            try {
                                Path relativePath = FileHelper.saveFile(proof, ConfigurationHelper.getConfigurationString("uploads.refuelproofs"));
                                FileDAO fdao = context.getFileDAO();
                                try {
                                    models.File file = fdao.createFile(relativePath.toString(), proof.getFilename(), proof.getContentType());
                                    refuel.setAmount(model.amount);
                                    refuel.setStatus(RefuelStatus.REQUEST);
                                    refuel.setProof(file);
                                    dao.updateRefuel(refuel);
                                    context.commit();
                                    Notifier.sendRefuelRequest(refuel.getCarRide().getReservation().getCar().getOwner(), refuel);
                                    flash("success", "Uw tankbeurt wordt voorgelegd aan de auto-eigenaar.");
                                    return redirect(routes.Refuels.showRefuels());
                                } catch (DataAccessException ex) {
                                    context.rollback();
                                    FileHelper.deleteFile(relativePath);
                                    throw ex;
                                }

                            } catch (IOException ex) {
                                throw new RuntimeException(ex); //no more checked catch -> error page!
                            }
                        }
                    } else {
                        flash("error", "Missing file");
                        return redirect(routes.Refuels.showRefuels());
                    }
                }
                catch(DataAccessException ex){
                    context.rollback();
                    throw ex;
                }
            } catch (DataAccessException ex) {
                //TODO: send fail message
                throw ex;
            }
        }
    }

    /**
     * Method: GET
     * @return proof url
     */
    @RoleSecured.RoleAuthenticated()
    public static Result getProof(int proofId) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            return FileHelper.getFileStreamResult(context.getFileDAO(), proofId);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     *
     * Called when a refuel of a car is refused by the car owner.
     *
     * @param refuelId  The refuel being refused
     * @return the refuel admin page
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER})
    public static Result refuseRefuel(int refuelId) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            RefuelDAO dao = context.getRefuelDAO();
            dao.rejectRefuel(refuelId);
            Refuel refuel = dao.getRefuel(refuelId);
            context.commit();
            Notifier.sendRefuelStatusChanged(refuel.getCarRide().getReservation().getUser(), refuel, false);
            flash("success", "Tankbeurt succesvol geweigerd");
            return redirect(routes.Refuels.showOwnerRefuels());
        }catch(DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     *
     * Called when a refuel of a car is accepted by the car owner.
     *
     * @param refuelId  The refuel being approved
     * @return the refuel admin page
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER})
    public static Result approveRefuel(int refuelId) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            RefuelDAO dao = context.getRefuelDAO();
            dao.acceptRefuel(refuelId);
            Refuel refuel = dao.getRefuel(refuelId);
            context.commit();
            Notifier.sendRefuelStatusChanged(refuel.getCarRide().getReservation().getUser(), refuel, true);
            flash("success", "Tankbeurt succesvol geaccepteerd");
            return redirect(routes.Refuels.showOwnerRefuels());
        }catch(DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Get the number of refuels having the provided status
     * @param status The status
     * @return The number of refuels
     */
    public static int refuelsWithStatus(RefuelStatus status) {
        User user = DataProvider.getUserProvider().getUser();
        try(DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            RefuelDAO dao = context.getRefuelDAO();
            return dao.getAmountOfRefuelsWithStatus(status, user.getId());
        } catch(DataAccessException ex) {
            throw ex;
        }
    }
}
