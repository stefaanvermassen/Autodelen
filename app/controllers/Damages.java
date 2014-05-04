package controllers;

import controllers.Security.RoleSecured;
import database.*;
import models.*;
import org.joda.time.DateTime;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import providers.DataProvider;
import views.html.cars.detail;
import views.html.cars.edit;
import views.html.damages.*;

import java.util.List;

import static controllers.util.Addresses.getCountryList;

/**
 * Created by Stefaan Vermassen on 03/05/14.
 */
public class Damages extends Controller {

    public static class DamageModel {

        public String description;
        public DateTime time;

        public String validate() {
            if("".equals(description))
                return "Geef aub een beschrijving op.";
            return null;
        }

        public void populate(Damage damage) {
            if (damage == null) return;
            description = damage.getDescription();
            time = damage.getTime();
        }
    }

    public static class DamageStatusModel {

        public String status;

        public String validate() {
            if("".equals(status))
                return "Geef aub een status op.";
            return null;
        }
    }

    /**
     * Method: GET
     *
     * @return index page containing all the damages from a specific user
     */
    @RoleSecured.RoleAuthenticated()
    public static Result showDamages() {
        User user = DataProvider.getUserProvider().getUser();
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            DamageDAO dao = context.getDamageDAO();
            List<Damage> damageList = dao.getUserDamages(user.getId());
            return ok(damages.render(damageList));
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     *
     * @return detail page containing all information about a specific damage
     */
    @RoleSecured.RoleAuthenticated()
    public static Result showDamageDetails(int damageId) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            DamageDAO dao = context.getDamageDAO();
            UserDAO userDAO = context.getUserDAO();
            CarDAO carDAO = context.getCarDAO();
            DamageLogDAO damageLogDAO = context.getDamageLogDAO();
            Damage damage = dao.getDamage(damageId);
            Car damagedCar = carDAO.getCar(damage.getCarRide().getReservation().getCar().getId());
            User owner = userDAO.getUser(damagedCar.getOwner().getId(), true);
            List<DamageLog> damageLogList = damageLogDAO.getDamageLogsForDamage(damageId);
            return ok(details.render(damage, owner, damagedCar, damageLogList));
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     * @return modal to edit damage information
     */
    @RoleSecured.RoleAuthenticated()
    public static Result editDamage(int damageId) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            DamageDAO dao = context.getDamageDAO();
            Damage damage = dao.getDamage(damageId);

            if (damage == null) {
                flash("danger", "Schadedossier met ID=" + damageId + " bestaat niet.");
                return badRequest();
            } else {
                User currentUser = DataProvider.getUserProvider().getUser();
                if(!(damage.getCarRide().getReservation().getUser().getId() == currentUser.getId() || DataProvider.getUserRoleProvider().hasRole(currentUser.getId(), UserRole.CAR_ADMIN))){
                    flash("danger", "U heeft geen rechten tot het bewerken van dit schadedossier.");
                    return badRequest();
                }

                DamageModel model = new DamageModel();
                model.populate(damage);

                Form<DamageModel> editForm = Form.form(DamageModel.class).fill(model);
                return ok(editmodal.render(editForm, damageId));
            }
        }catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     * @return modal to provide new damage log status
     */
    @RoleSecured.RoleAuthenticated()
    public static Result addStatus(int damageId) {
        return ok(statusmodal.render(Form.form(DamageStatusModel.class), damageId));
    }

    /**
     * Method: GET
     * @return modal to provide new damage proof
     */
    @RoleSecured.RoleAuthenticated()
    public static Result addProof(int damageId) {
        return ok(proofmodal.render(damageId));
    }

    /**
     * Method: POST
     * @return redirect to the damage detail page
     */
   @RoleSecured.RoleAuthenticated()
    public static Result editDamagePost(int damageId) {
        Form<DamageModel> damageForm = Form.form(DamageModel.class).bindFromRequest();
        if (damageForm.hasErrors()) {
            try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
                DamageDAO dao = context.getDamageDAO();
                UserDAO userDAO = context.getUserDAO();
                CarDAO carDAO = context.getCarDAO();
                DamageLogDAO damageLogDAO = context.getDamageLogDAO();
                Damage damage = dao.getDamage(damageId);
                Car damagedCar = carDAO.getCar(damage.getCarRide().getReservation().getCar().getId());
                User owner = userDAO.getUser(damagedCar.getOwner().getId(), true);
                List<DamageLog> damageLogList = damageLogDAO.getDamageLogsForDamage(damageId);
                flash("danger", "Beschrijving aanpassen mislukt.");
                return badRequest(details.render(damage, owner, damagedCar, damageLogList));
            }catch(DataAccessException ex){
                throw ex; //log?
            }
        } else {

            try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
                DamageDAO damageDAO = context.getDamageDAO();
                Damage damage = damageDAO.getDamage(damageId);
                DamageModel model = damageForm.get();
                damage.setDescription(model.description);
                damage.setTime(model.time);
                damageDAO.updateDamage(damage);
                context.commit();
                flash("success", "De beschrijving werd gewijzigd.");
                return redirect(
                        routes.Damages.showDamageDetails(damageId)
                );
            } catch (DataAccessException ex) {
                throw ex; //TODO: show gracefully
            }
        }
    }


    /**
     * Method: POST
     * @return redirect to the damage detail page
     */
    @RoleSecured.RoleAuthenticated()
    public static Result addStatusPost(int damageId) {
        Form<DamageStatusModel> damageStatusForm = Form.form(DamageStatusModel.class).bindFromRequest();
        if (damageStatusForm.hasErrors()) {
            try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
                DamageDAO dao = context.getDamageDAO();
                UserDAO userDAO = context.getUserDAO();
                CarDAO carDAO = context.getCarDAO();
                DamageLogDAO damageLogDAO = context.getDamageLogDAO();
                Damage damage = dao.getDamage(damageId);
                Car damagedCar = carDAO.getCar(damage.getCarRide().getReservation().getCar().getId());
                User owner = userDAO.getUser(damagedCar.getOwner().getId(), true);
                List<DamageLog> damageLogList = damageLogDAO.getDamageLogsForDamage(damageId);
                flash("danger", "Status toevoegen mislukt.");
                return badRequest(details.render(damage, owner, damagedCar, damageLogList));
            }catch(DataAccessException ex){
                throw ex;
            }
        } else {

            try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
                DamageDAO damageDAO = context.getDamageDAO();
                DamageLogDAO damageLogDAO = context.getDamageLogDAO();
                Damage damage = damageDAO.getDamage(damageId);
                DamageStatusModel model = damageStatusForm.get();
                DamageLog damageLog = damageLogDAO.createDamageLog(damage,model.status);
                context.commit();
                if (damageLog == null) {
                    flash("danger", "Kon de damagelog niet toevoegen aan de database.");
                    return redirect(routes.Damages.showDamageDetails(damageId));
                }
                flash("success", "De status werd toegevoegd.");
                return redirect(
                        routes.Damages.showDamageDetails(damageId)
                );
            } catch (DataAccessException ex) {
                throw ex; //TODO: show gracefully
            }
        }
    }
}
