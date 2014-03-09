package controllers;

import database.*;
import models.Address;
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

        // TODO: remove when user can't add cars unless his address is specified
        public String address_zip;
        public String address_city;
        public String address_street;
        public String address_number;
        public String address_bus;

        //TODO: check input (year,...)
        public String validate() {
            // TODO: temporary only check if city is not null
            if("".equals(address_zip) || "".equals(address_city))
                return "Geef aub het adres op.";
            else if(brand.length() <= 0)
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
                    AddressDAO adao = context.getAddressDAO();
                    Address address = adao.createAddress(model.address_zip, model.address_city, model.address_street,
                            model.address_number, model.address_bus);

                    // TODO: get boolean out (hook and gps) of form, enum fuel
                    Car car = dao.createCar(model.brand, model.type, address, model.seats, model.doors,
                            model.year, false, false, CarFuel.DIESEL, model.fuelEconomy, model.estimatedValue,
                            model.ownerAnnualKm, user, "");
                    context.commit();
                    if (car != null) {
                        // TODO: redirect to list of cars
                        return redirect(
                                routes.Dashboard.index()
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
