package controllers;

import database.*;
import database.FilterField;
import database.jdbc.JDBCFilter;
import models.*;
import controllers.Security.RoleSecured;

import play.api.templates.Html;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.cars.*;

import java.util.List;


/**
 * Controller responsible for creating, updating and showing of cars
 */
public class Cars extends Controller {

    private static final int PAGE_SIZE = 10;

    public static class CarModel {

        public String name;
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

        /**
         * Validates the form:
         *      - Address zip and city cannot be empty
         *      - Car name and brand cannot be empty
         *      - There have to be at least 2 doors and seats
         * @return An error string or null
         */
        public String validate() {
            // TODO: temporary only check if city is not null
            if("".equals(address_zip) || "".equals(address_city))
                return "Geef aub het adres op.";
            else if(name.length() <= 0)
                return "Geef aub de autonaam op.";
            else if(brand.length() <= 0)
                return "Geef aub het automerk op.";
            else if(seats < 2)
                return "Een auto heeft minstens 2 zitplaatsen";
            else if(doors < 2)
                return "Een auto heeft minstens 2 deuren";
            return null;
        }
    }

    /**
     * @return The cars index-page with all cars (only available to car_user+)
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_USER, UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    public static Result showCars() {
        return ok(cars.render());
    }

    /**
     *
     * @param page The page in the carlists
     * @param ascInt An integer representing ascending (1) or descending (0)
     * @param orderBy A field representing the field to order on
     * @param searchString A string witth form field1:value1,field2:value2 representing the fields to filter on
     * @return A partial page with a table of cars of the corresponding page (only available to car_user+)
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_USER, UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    public static Result showCarsPage(int page, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField carField = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);
        return ok(carList(page, carField, asc, filter));
    }

    private static Html carList() {
        return carList(1, FilterField.CAR_NAME, true, null);
    }

    private static Html carList(int page, FilterField orderBy, boolean asc, Filter filter) {

        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            CarDAO dao = context.getCarDAO();

            if(orderBy == null) {
                orderBy = FilterField.CAR_NAME;
            }
            List<Car> listOfCars = dao.getCarList(orderBy, asc, page, PAGE_SIZE, filter);

            int amountOfResults = dao.getAmountOfCars(filter);
            int amountOfPages = (int) Math.ceil( amountOfResults / (double) PAGE_SIZE);

            return carspage.render(listOfCars, page, amountOfResults, amountOfPages);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * @return A form to create a new car (only available to car_owner+)
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    public static Result newCar() {
        return ok(addcar.render(Form.form(CarModel.class), 0));
    }

    /**
     * Method: POST
     * @return redirect to the CarForm you just filled in or to the cars-index page (only available to car_owner+)
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    public static Result addNewCar() {
        Form<CarModel> carForm = Form.form(CarModel.class).bindFromRequest();
        if (carForm.hasErrors()) {
            return badRequest(addcar.render(carForm, 0));
        } else {
            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                CarDAO dao = context.getCarDAO();
                try {
                    User user = DatabaseHelper.getUserProvider().getUser();
                    CarModel model = carForm.get();
                    AddressDAO adao = context.getAddressDAO();
                    // TODO: add country
                    Address address = adao.createAddress("Belgium", model.address_zip, model.address_city, model.address_street,
                            model.address_number, model.address_bus);

                    // TODO: also accept other users (only admin can do this)
                    // TODO: get boolean out (hook and gps) of form, enum fuel, comments
                    Car car = dao.createCar(model.name, model.brand, model.type, address, model.seats, model.doors,
                            model.year, false, false, CarFuel.DIESEL, model.fuelEconomy, model.estimatedValue,
                            model.ownerAnnualKm, user, "");
                    context.commit();

                    if (car != null) {
                        return redirect(
                                routes.Cars.showCars()
                        );
                    } else {
                        carForm.error("Failed to add the car to the database. Contact administrator.");
                        return badRequest(addcar.render(carForm, 0));
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
     * @param carId The car to edit
     * @return A form to edit the car (only available to the corresponding car owner or administrator)
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    public static Result editCar(int carId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            CarDAO dao = context.getCarDAO();
            Car car = dao.getCar(carId);

            if (car == null) {
                flash("danger", "Auto met ID=" + carId + " bestaat niet.");
                return badRequest(carList());
            } else {
                User currentUser = DatabaseHelper.getUserProvider().getUser();
                if(!(car.getOwner().getId() == currentUser.getId() || DatabaseHelper.getUserRoleProvider().hasRole(currentUser.getId(), UserRole.RESERVATION_ADMIN))){

                    flash("danger", "U heeft geen rechten tot het bewerken van deze wagen.");
                    return badRequest(carList());
                }

                CarModel model = new CarModel();
                model.name = car.getName();
                model.brand = car.getBrand();
                model.type = car.getType();
                model.seats = car.getSeats();
                model.doors = car.getDoors();
                model.year = car.getYear();
                model.gps = car.isGps();
                model.hook = car.isHook();
                model.fuel = car.getFuel();
                model.fuelEconomy = car.getFuelEconomy();
                model.estimatedValue = car.getEstimatedValue();
                model.ownerAnnualKm = car.getOwnerAnnualKm();
                model.comments = car.getComments();

                if(car.getLocation() != null) {
                    model.address_street = car.getLocation().getStreet();
                    model.address_number = car.getLocation().getNumber();
                    model.address_bus = car.getLocation().getBus();
                    model.address_zip = car.getLocation().getZip();
                    model.address_city = car.getLocation().getCity();
                }

                Form<CarModel> editForm = Form.form(CarModel.class).fill(model);
                return ok(addcar.render(editForm, carId));
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: POST
     *
     * @param carId The car to edit
     * @return Redirect to the car-index page on error or the car detail-page on succes (only available to the corresponding car owner or administrator)
     */

    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    public static Result editCarPost(int carId) {
        Form<CarModel> editForm = Form.form(CarModel.class).bindFromRequest();
        if (editForm.hasErrors()) {
            return badRequest(addcar.render(editForm, carId));
        } else {
            try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
                CarDAO dao = context.getCarDAO();
                Car car = dao.getCar(carId);

                if (car == null) {
                    flash("danger", "Car met ID=" + carId + " bestaat niet.");
                    return badRequest(carList());
                }

                User currentUser = DatabaseHelper.getUserProvider().getUser();
                if(!(car.getOwner().getId() == currentUser.getId() || DatabaseHelper.getUserRoleProvider().hasRole(currentUser.getId(), UserRole.RESERVATION_ADMIN))){

                    flash("danger", "U heeft geen rechten tot het bewerken van deze wagen.");
                    return badRequest(carList());
                }

                try {
                    CarModel carModel = editForm.get();
                    car.setName(carModel.name);
                    car.setBrand(carModel.brand);
                    car.setType(carModel.type);
                    car.setDoors(carModel.doors);
                    car.setSeats(carModel.seats);
                    car.setYear(carModel.year);
                    car.setFuelEconomy(carModel.fuelEconomy);
                    car.setEstimatedValue(carModel.estimatedValue);
                    car.setOwnerAnnualKm(carModel.ownerAnnualKm);

                    AddressDAO adao = context.getAddressDAO();
                    Address address = car.getLocation();
                    if(address == null) {
                        address = adao.createAddress("Belgium", carModel.address_zip, carModel.address_city, carModel.address_street, carModel.address_number, carModel.address_bus);
                        car.setLocation(address);
                    } else {
                        address.setCity(carModel.address_city);
                        address.setBus(carModel.address_bus);
                        address.setNumber(carModel.address_number);
                        address.setStreet(carModel.address_street);
                        address.setZip(carModel.address_zip);
                        adao.updateAddress(address);
                    }

                    car.setComments(carModel.comments);
                    dao.updateCar(car);

                    context.commit();
                    flash("success", "Uw wijzigingen werden succesvol toegepast.");
                    return detail(carId);
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
     *
     * @param carId The car to show details of
     * @return A detail page of the car (only available to car_user+)
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_USER, UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    public static Result detail(int carId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            CarDAO dao = context.getCarDAO();
            Car car = dao.getCar(carId);

            if(car == null) {
                flash("danger", "Auto met ID=" + carId + " bestaat niet.");
                return badRequest(carList());
            } else {
                return ok(detail.render(car));
            }
        } catch (DataAccessException ex) {
            throw ex;
            //TODO: log
        }
    }

    /**
     * TODO: delete this out of final version
     * @param carId The car to be removed
     * @return redirect to the index carpage, with error-messages if there were any problems
     */
    @RoleSecured.RoleAuthenticated(UserRole.RESERVATION_ADMIN)
    public static Result removeCar(int carId) {
        try (DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext()) {
            CarDAO dao = context.getCarDAO();
            try {
                Car car = dao.getCar(carId);
                if (car == null) {
                    flash("danger", "De auto met ID= " + carId + " bestaat niet.");
                    return badRequest(carList());
                } else {

                    //TODO: this is repeat code, unify with above controllers as extra check
                    User currentUser = DatabaseHelper.getUserProvider().getUser();
                    if(!(car.getOwner().getId() == currentUser.getId() || DatabaseHelper.getUserRoleProvider().hasRole(currentUser.getId(), UserRole.RESERVATION_ADMIN))){

                        flash("danger", "U heeft geen rechten tot het verwijderen van deze wagen.");
                        return badRequest(carList());
                    }

                    dao.deleteCar(car);
                    context.commit();
                    flash("success", "De auto werd succesvol verwijderd.");
                    return ok(carList());
                }
            } catch (DataAccessException ex) {
                context.rollback();
                throw ex;
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }
}
