package controllers;

import controllers.Security.RoleSecured;
import database.*;
import models.Car;
import models.Damage;
import models.User;
import org.joda.time.DateTime;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import providers.DataProvider;
import views.html.damages.damages;
import views.html.damages.details;
import views.html.damages.editmodal;
import views.html.damages.statusmodal;
import views.html.damages.proofmodal;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 03/05/14.
 */
public class Damages extends Controller {

    public static class DamageModel {

        public String description;
        public DateTime time;

        public String validate() {
            return null;
        }
    }

    public static class DamageStatusModel {

        public String status;

        public String validate() {
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
            Damage damage = dao.getDamage(damageId);
            Car damagedCar = carDAO.getCar(damage.getCarRide().getReservation().getCar().getId());
            User owner = userDAO.getUser(damagedCar.getOwner().getId(), true);
            return ok(details.render(damage, owner, damagedCar));
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
        return ok(editmodal.render(Form.form(DamageModel.class), damageId));
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
}
