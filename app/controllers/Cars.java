package controllers;

import database.*;
import models.Car;
import models.CarFuel;
import models.User;
import controllers.Security.RoleSecured;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.cars.*;

/**
 * Created by Benjamin on 27/02/14.
 */
public class Cars extends Controller {

    public static class CarModel {
        public String brand;
        public String type;
        public int seats;
        public int doors;
        public boolean gps;
        public boolean hook;
        public int year;
        public CarFuel fuel;
        public int fuelEconomy;
        public int estimatedValue;
        public int ownerAnnualKm;
        public String comments;

        //TODO: check input (year,...)
        public String validate() {
            if(brand.length() <= 0)
                return "Geef aub het automerk op.";
            else if(seats < 2)
                return "Een auto heeft minstens 2 zitplaatsen";
            else if(doors < 2)
                return "Een auto heeft minstens 2 deuren";
            return null;
        }
    }

    public static Result showCars() {
        return ok(carlist.render());
    }

    @RoleSecured.RoleAuthenticated()
    public static Result newCar() {
        return ok(addcar.render(Form.form(CarModel.class)));
    }
    
    @RoleSecured.RoleAuthenticated()
    public static Result addNewCar() {
        Form<CarModel> carForm = Form.form(CarModel.class).bindFromRequest();
        if (carForm.hasErrors()) {
            return badRequest(addcar.render(carForm));
        } else {
            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                CarDAO dao = context.getCarDAO();
                try {
                    User user = DatabaseHelper.getUserProvider().getUser(session("email"));
                    CarModel model = carForm.get();
                    // TODO: get boolean out (hook and gps) of form, enum fuel
                    // TODO: avoid nullpointer when user location not yet in database
                    Car car = dao.createCar(model.brand, model.type, user.getAddress(), model.seats, model.doors,
                            model.year, false, false, CarFuel.DIESEL, model.fuelEconomy, model.estimatedValue,
                            model.ownerAnnualKm, user, "");
                    context.commit();
                    if (car != null) {
                        return redirect(
                                routes.Cars.showCars()
                        );
                    } else {
                        carForm.error("Failed to add the car to the database. Contact administrator.");
                        return badRequest(addcar.render(carForm));
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
}
