package controllers;

import controllers.Security.RoleSecured;
import controllers.util.ConfigurationHelper;
import controllers.util.FileHelper;
import database.*;
import models.Refuel;
import models.RefuelStatus;
import models.User;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.refuels.editmodal;
import views.html.refuels.refuels;
import views.html.refuels.refuelsOwner;

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
        User user = DatabaseHelper.getUserProvider().getUser();
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            RefuelDAO dao = context.getRefuelDAO();
            List<Refuel> refuelList = dao.getRefuelsForUser(user.getId());
            return ok(refuels.render(refuelList));
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     *
     * @return index page containing all the refuel requests to a specific owner
     */
    @RoleSecured.RoleAuthenticated()
    public static Result showOwnerRefuels() {
        User user = DatabaseHelper.getUserProvider().getUser();
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            RefuelDAO dao = context.getRefuelDAO();
            List<Refuel> refuelList = dao.getRefuelsForOwner(user.getId());
            return ok(refuelsOwner.render(refuelList));
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
            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
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
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            return FileHelper.getFileStreamResult(context.getFileDAO(), proofId);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }
}
