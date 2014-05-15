package controllers;

import controllers.util.Addresses;
import controllers.util.ConfigurationHelper;
import controllers.util.FileHelper;
import controllers.util.Pagination;
import database.*;
import database.FilterField;
import models.*;
import play.libs.F;
import providers.DataProvider;
import providers.UserRoleProvider;
import controllers.Security.RoleSecured;

import notifiers.Notifier;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import play.api.templates.Html;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.cars.*;
import views.html.cars.edit;
import views.html.cars.addcarcostmodal;
import views.html.cars.carCostsAdmin;
import views.html.cars.carCostspage;
import views.html.cars.cars;
import views.html.cars.detail;
import views.html.cars.carsAdmin;
import views.html.cars.carspage;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static controllers.util.Addresses.getCountryList;
import static controllers.util.Addresses.modifyAddress;


/**
 * Controller responsible for creating, updating and showing of cars
 */
public class Cars extends Controller {

    private static final int PAGE_SIZE = 10;
    private static final int PAGE_SIZE_CAR_COSTS = 10;

    private static List<String> fuelList;

    public static List<String> getFuelList() {
        if(fuelList == null) {
            fuelList = new ArrayList<>();
            CarFuel[] types = CarFuel.values();
            for(CarFuel f : types) {
                fuelList.add(f.getDescription());
            }
        }
        return fuelList;
    }


    public static class CarModel {

        public Integer userId;

        public String name;
        public String brand;
        public String type;
        public Integer seats;
        public Integer doors;
        public boolean manual;
        public boolean gps;
        public boolean hook;
        public Integer year;
        public String fuel;
        public Integer fuelEconomy;
        public Integer estimatedValue;
        public Integer ownerAnnualKm;
        public String comments;
        public boolean active;

        // TechnicalCarDetails
        public String licensePlate;
        // public String registration; // TODO: file inschrijvingsbewijs
        public Integer chassisNumber;

        // Insurance
        public String insuranceName;
        public Date expiration;
        public Integer bonusMalus;
        public Integer polisNr;

        public Addresses.EditAddressModel address = new Addresses.EditAddressModel();

        public void populate(Car car) {
            if(car == null) return;

            userId = car.getOwner().getId();

            name = car.getName();
            brand = car.getBrand();
            type = car.getType();
            seats = car.getSeats();
            doors = car.getDoors();
            year = car.getYear();
            manual = car.isManual();
            gps = car.isGps();
            hook = car.isHook();
            fuel = car.getFuel().getDescription();
            fuelEconomy = car.getFuelEconomy();
            estimatedValue = car.getEstimatedValue();
            ownerAnnualKm = car.getOwnerAnnualKm();
            comments = car.getComments();
            active = car.isActive();

            if(car.getTechnicalCarDetails() != null) {
                licensePlate = car.getTechnicalCarDetails().getLicensePlate();
                chassisNumber = car.getTechnicalCarDetails().getChassisNumber();
            }

            if(car.getInsurance() != null) {
                insuranceName = car.getInsurance().getName();
                if(car.getInsurance().getExpiration() != null)
                    expiration = car.getInsurance().getExpiration();
                bonusMalus = car.getInsurance().getBonusMalus();
                polisNr = car.getInsurance().getPolisNr();
            }

            address.populate(car.getLocation());
        }

        /**
         * Validates the form:
         *      - Address zip and city cannot be empty
         *      - Car name and brand cannot be empty
         *      - There have to be at least 2 doors and seats
         * @return An error string or null
         */
        public String validate() {
            String error = "";
            if(userId == null || userId == 0)
                error += "Geef een eigenaar op. ";
            if(!address.enoughFilled())
                error += "Geef het adres op.";
            if(name.length() <= 0)
                error +=  "Geef de autonaam op. ";
            if(brand.length() <= 0)
                error +=  "Geef het automerk op. ";
            if(seats == null || seats < 2)
                error +=  "Een auto heeft minstens 2 zitplaatsen. ";
            if(doors == null || doors < 2)
                error +=  "Een auto heeft minstens 2 deuren. ";

            if("".equals(error)) return null;
            else return error;
        }
    }

    /**
     * @return The cars index-page with all cars (only available to car_user+)
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_ADMIN})
    public static Result showCars() {
        return ok(carsAdmin.render());
    }

    /**
     * @return The cars index-page with user cars  (only available to car_owners)
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER})
    public static Result showUserCars() {
        User user = DataProvider.getUserProvider().getUser();
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            CarDAO dao = context.getCarDAO();
            // Doesn't need to be paginated, because a single user will never have a lot of cars
            List<Car> listOfCars = dao.getCarsOfUser(user.getId());
            return ok(cars.render(listOfCars));
        } catch (DataAccessException ex) {
            throw ex;
        }

    }

    /**
     *
     * @param page The page in the carlists
     * @param ascInt An integer representing ascending (1) or descending (0)
     * @param orderBy A field representing the field to order on
     * @param searchString A string witth form field1:value1,field2:value2 representing the fields to filter on
     * @return A partial page with a table of cars of the corresponding page (only available to car_user+)
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_ADMIN})
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

        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
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
     * Gets the picture for given car Id, or default one if missing
     *
     * @param carId The car for which the image is requested
     * @return The image with correct content type
     */
    @RoleSecured.RoleAuthenticated()
    public static Result getPicture(int carId) {
        //TODO: checks on whether other person can see this
        // TODO: actual car picture
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            CarDAO carDao = context.getCarDAO();
            Car car = carDao.getCar(carId);
            if (car != null && car.getPhoto() != null && car.getPhoto().getId() > 0) {
                return FileHelper.getFileStreamResult(context.getFileDAO(), car.getPhoto().getId());
            } else {
                return FileHelper.getPublicFile(Paths.get("images", "no-photo-car.jpg").toString(), "image/jpeg");
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * @return A form to create a new car (only available to car_owner+)
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    public static Result newCar() {
        return ok(edit.render(Form.form(CarModel.class), null, getCountryList(), getFuelList()));
    }

    /**
     * Method: POST
     * @return redirect to the CarForm you just filled in or to the cars-index page (only available to car_owner+)
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    public static Result addNewCar() {
        Form<CarModel> carForm = Form.form(CarModel.class).bindFromRequest();
        if (carForm.hasErrors()) {
            return badRequest(edit.render(carForm, null, getCountryList(),getFuelList()));
        } else {
            try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
                CarDAO dao = context.getCarDAO();
                try {
                    User user = DataProvider.getUserProvider().getUser();
                    CarModel model = carForm.get();
                    AddressDAO adao = context.getAddressDAO();
                    Address address = modifyAddress(model.address, null, adao);

                    User owner = user;
                    if(DataProvider.getUserRoleProvider().hasRole(user, UserRole.SUPER_USER)
                            || DataProvider.getUserRoleProvider().hasRole(user, UserRole.CAR_ADMIN)) {
                        // User is permitted to add cars for other users
                        owner = context.getUserDAO().getUser(model.userId, false);
                    }
                    TechnicalCarDetails technicalCarDetails = null;
                    Http.MultipartFormData body = request().body().asMultipartFormData();
                    Http.MultipartFormData.FilePart registrationFile = body.getFile("file");
                    Http.MultipartFormData.FilePart photoFilePart = body.getFile("picture");
                    models.File file = null;
                    models.File picture = null;
                    if (registrationFile != null) {
                        String contentType = registrationFile.getContentType();
                        if (!FileHelper.isDocumentContentType(contentType)) {
                            flash("danger", "Verkeerd bestandstype opgegeven. Enkel documenten zijn toegelaten. (ontvangen MIME-type: " + contentType + ")");
                            return badRequest(edit.render(carForm, null, getCountryList(),getFuelList()));
                        } else {
                            try {
                                Path relativePath = FileHelper.saveFile(registrationFile, ConfigurationHelper.getConfigurationString("uploads.carregistrations"));
                                FileDAO fdao = context.getFileDAO();
                                file = fdao.createFile(relativePath.toString(), registrationFile.getFilename(), registrationFile.getContentType());
                            } catch (IOException ex) {
                                throw new RuntimeException(ex); //no more checked catch -> error page!
                            }
                        }
                    }
                    if (photoFilePart != null) {
                        String contentType = photoFilePart.getContentType();
                        if (!FileHelper.isImageContentType(contentType)) {
                            flash("danger", "Verkeerd bestandstype opgegeven. Enkel documenten zijn toegelaten. (ontvangen MIME-type: " + contentType + ")");
                            return badRequest(edit.render(carForm, null, getCountryList(),getFuelList()));
                        } else {
                            try {
                                Path relativePath = FileHelper.saveFile(photoFilePart, ConfigurationHelper.getConfigurationString("uploads.carphotos"));
                                FileDAO fdao = context.getFileDAO();
                                picture = fdao.createFile(relativePath.toString(), photoFilePart.getFilename(), photoFilePart.getContentType());
                            } catch (IOException ex) {
                                throw new RuntimeException(ex); //no more checked catch -> error page!
                            }
                        }
                    }
                    if((model.licensePlate != null && !model.licensePlate.equals(""))
                            || (model.chassisNumber != null && model.chassisNumber != 0) || file != null) {
                        technicalCarDetails = new TechnicalCarDetails(model.licensePlate, file, model.chassisNumber);
                    }
                    CarInsurance insurance = null;
                    if((model.insuranceName != null && !model.insuranceName.equals("")) || (model.expiration != null || (model.polisNr != null && model.polisNr != 0))
                            || (model.bonusMalus != null && model.bonusMalus != 0)) {
                        insurance = new CarInsurance(model.insuranceName, model.expiration, model.bonusMalus, model.polisNr);
                    }
                    Car car = dao.createCar(model.name, model.brand, model.type, address, model.seats, model.doors,
                            model.year, model.manual, model.gps, model.hook, CarFuel.getFuelFromString(model.fuel), model.fuelEconomy, model.estimatedValue,
                            model.ownerAnnualKm, technicalCarDetails, insurance, owner, model.comments, model.active, picture);

                    context.commit();

                    if (car != null) {
                        return redirect(
                                routes.Cars.showCars()
                        );
                    } else {
                        carForm.error("Failed to add the car to the database. Contact administrator.");
                        return badRequest(edit.render(carForm, null, getCountryList(),getFuelList()));
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
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    public static Result editCar(int carId) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            CarDAO dao = context.getCarDAO();
            Car car = dao.getCar(carId);

            if (car == null) {
                flash("danger", "Auto met ID=" + carId + " bestaat niet.");
                return badRequest(carList());
            } else {
                User currentUser = DataProvider.getUserProvider().getUser();
                if(!(car.getOwner().getId() == currentUser.getId() || DataProvider.getUserRoleProvider().hasRole(currentUser.getId(), UserRole.RESERVATION_ADMIN))){
                    flash("danger", "Je hebt geen rechten tot het bewerken van deze wagen.");
                    return badRequest(carList());
                }

                CarModel model = new CarModel();
                model.populate(car);

                Form<CarModel> editForm = Form.form(CarModel.class).fill(model);
                return ok(edit.render(editForm, car, getCountryList(),getFuelList()));
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

    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    public static Result editCarPost(int carId) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            CarDAO dao = context.getCarDAO();
            Car car = dao.getCar(carId);

            Form<CarModel> editForm = Form.form(CarModel.class).bindFromRequest();
            if (editForm.hasErrors())
                return badRequest(edit.render(editForm, car, getCountryList(),getFuelList()));

            if (car == null) {
                flash("danger", "Car met ID=" + carId + " bestaat niet.");
                return badRequest(carList());
            }

            User currentUser = DataProvider.getUserProvider().getUser();
            if(!(car.getOwner().getId() == currentUser.getId() || DataProvider.getUserRoleProvider().hasRole(currentUser.getId(), UserRole.RESERVATION_ADMIN))){
                flash("danger", "Je hebt geen rechten tot het bewerken van deze wagen.");
                return badRequest(carList());
            }

            try {
                CarModel model = editForm.get();
                car.setName(model.name);
                car.setBrand(model.brand);
                car.setType(model.type);
                if(model.doors != null)
                    car.setDoors(model.doors);
                else
                    car.setDoors(null);
                if(model.seats != null)
                    car.setSeats(model.seats);
                else
                    car.setSeats((null));
                car.setManual(model.manual);
                car.setGps(model.gps);
                car.setHook(model.hook);
                car.setFuel(CarFuel.getFuelFromString(model.fuel));
                if(model.year != null)
                    car.setYear(model.year);
                else
                    car.setYear(null);
                if(model.fuelEconomy != null)
                    car.setFuelEconomy(model.fuelEconomy);
                else
                    car.setFuelEconomy(null);
                if(model.estimatedValue != null)
                    car.setEstimatedValue(model.estimatedValue);
                else
                    car.setEstimatedValue(null);
                if(model.ownerAnnualKm != null)
                    car.setOwnerAnnualKm(model.ownerAnnualKm);
                else
                    car.setOwnerAnnualKm(null);
                Http.MultipartFormData body = request().body().asMultipartFormData();
                Http.MultipartFormData.FilePart registrationFile = body.getFile("file");
                Http.MultipartFormData.FilePart photoFilePart = body.getFile("picture");
                models.File file = null;
                models.File picture = null;
                if (registrationFile != null) {
                    String contentType = registrationFile.getContentType();
                    if (!FileHelper.isDocumentContentType(contentType)) {
                        flash("danger", "Verkeerd bestandstype opgegeven. Enkel documenten zijn toegelaten. (ontvangen MIME-type: " + contentType + ")");
                        return redirect(routes.Cars.detail(car.getId()));
                    } else {
                        try {
                            Path relativePath = FileHelper.saveFile(registrationFile, ConfigurationHelper.getConfigurationString("uploads.carregistrations"));
                            FileDAO fdao = context.getFileDAO();
                            file = fdao.createFile(relativePath.toString(), registrationFile.getFilename(), registrationFile.getContentType());
                        } catch (IOException ex) {
                            throw new RuntimeException(ex); //no more checked catch -> error page!
                        }
                    }
                }

                if (photoFilePart != null) {
                    String contentType = photoFilePart.getContentType();
                    if (!FileHelper.isImageContentType(contentType)) {
                        flash("danger", "Verkeerd bestandstype opgegeven. Enkel afbeeldingen zijn toegelaten als foto. (ontvangen MIME-type: " + contentType + ")");
                        return redirect(routes.Cars.detail(car.getId()));
                    } else {
                        try {
                            Path relativePath = FileHelper.saveFile(photoFilePart, ConfigurationHelper.getConfigurationString("uploads.carphotos"));
                            FileDAO fdao = context.getFileDAO();
                            picture = fdao.createFile(relativePath.toString(), photoFilePart.getFilename(), photoFilePart.getContentType());
                        } catch (IOException ex) {
                            throw new RuntimeException(ex); //no more checked catch -> error page!
                        }
                    }
                }
                if(car.getTechnicalCarDetails() == null) {
                    if((model.licensePlate != null && !model.licensePlate.equals(""))
                            || (model.chassisNumber != null && model.chassisNumber != 0) || file != null)
                        car.setTechnicalCarDetails(new TechnicalCarDetails(model.licensePlate, file, model.chassisNumber));
                }
                else {
                    if(model.licensePlate != null && !model.licensePlate.equals(""))
                        car.getTechnicalCarDetails().setLicensePlate(model.licensePlate);
                    else
                        car.getTechnicalCarDetails().setLicensePlate(null);

                        car.getTechnicalCarDetails().setRegistration(null);
                    if(model.chassisNumber != null && model.chassisNumber != 0)
                        car.getTechnicalCarDetails().setChassisNumber(model.chassisNumber);
                    else
                        car.getTechnicalCarDetails().setChassisNumber(null);
                    if(file != null)
                        car.getTechnicalCarDetails().setRegistration(file);
                }
                if(car.getInsurance() == null) {
                    if(model.insuranceName != null && !model.insuranceName.equals("") || (model.expiration != null) || (model.bonusMalus != null && model.bonusMalus != 0)
                            || (model.polisNr != null && model.polisNr != 0))
                        car.setInsurance(new CarInsurance(model.insuranceName, model.expiration, model.bonusMalus, model.polisNr));
                }
                else {
                    if(model.insuranceName != null && !model.insuranceName.equals(""))
                        car.getInsurance().setName(model.insuranceName);
                    else
                        car.getInsurance().setName(null);
                    if(model.expiration != null)
                        car.getInsurance().setExpiration(model.expiration);
                    else
                        car.getInsurance().setExpiration(null);
                    if(model.bonusMalus != null && model.bonusMalus != 0)
                        car.getInsurance().setBonusMalus(model.bonusMalus);
                    else
                        car.getInsurance().setBonusMalus(null);
                    if(model.polisNr != null && model.polisNr != 0)
                        car.getInsurance().setPolisNr(model.polisNr);
                    else
                        car.getInsurance().setPolisNr(null);
                }
                AddressDAO adao = context.getAddressDAO();
                Address address = car.getLocation();
                car.setLocation(modifyAddress(model.address, address, adao));

                car.setComments(model.comments);

                car.setActive(model.active);
                if(picture != null){
                    car.setPhoto(picture);
                }

                User user = DataProvider.getUserProvider().getUser();
                if(DataProvider.getUserRoleProvider().hasRole(user, UserRole.SUPER_USER)
                        || DataProvider.getUserRoleProvider().hasRole(user, UserRole.CAR_ADMIN)) {
                    // User is permitted to add cars for other users
                    car.setOwner(context.getUserDAO().getUser(model.userId, false));
                }

                dao.updateCar(car);

                context.commit();
                flash("success", "Jouw wijzigingen werden succesvol toegepast.");
                return redirect(routes.Cars.detail(car.getId()));
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
     * @return redirect to the car detailPage
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    public static Result updateAvailabilities(int carId, String valuesString) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            CarDAO dao = context.getCarDAO();
            Car car = dao.getCar(carId);

            if (car == null) {
                flash("danger", "Car met ID=" + carId + " bestaat niet.");
                return badRequest(carList());
            }

            User currentUser = DataProvider.getUserProvider().getUser();
            if(!(car.getOwner().getId() == currentUser.getId() || DataProvider.getUserRoleProvider().hasRole(currentUser.getId(), UserRole.CAR_ADMIN))){
                flash("danger", "Je hebt geen rechten tot het bewerken van deze wagen.");
                return badRequest(carList());
            }

            try {
                String[] values = valuesString.split(";");

                List<CarAvailabilityInterval> availabilitiesToAddOrUpdate = new ArrayList<>();
                List<CarAvailabilityInterval> availabilitiesToDelete = new ArrayList<>();

                for(String value : values) {
                    String[] vs = value.split(",");
                    if(vs.length != 5) {
                        flash("error", "Er is een fout gebeurd bij het doorgeven van de beschikbaarheidswaarden.");
                        return redirect(routes.Cars.detail(carId));
                    }
                    try {
                        int id = Integer.parseInt(vs[0]);
                        DayOfWeek beginDay = DayOfWeek.getDayFromInt(Integer.parseInt(vs[1]));
                        String[] beginHM = vs[2].split(":");
                        LocalTime beginTime = new LocalTime(Integer.parseInt(beginHM[0]), Integer.parseInt(beginHM[1]));
                        DayOfWeek endDay = DayOfWeek.getDayFromInt(Integer.parseInt(vs[3]));
                        String[] endHM = vs[4].split(":");
                        LocalTime endTime = new LocalTime(Integer.parseInt(endHM[0]), Integer.parseInt(endHM[1]));
                        if(id == 0) { // create
                            availabilitiesToAddOrUpdate.add(new CarAvailabilityInterval(beginDay, beginTime, endDay, endTime));
                        } else if(id > 0) { // update
                            availabilitiesToAddOrUpdate.add(new CarAvailabilityInterval(id, beginDay, beginTime, endDay, endTime));
                        } else { // delete
                            availabilitiesToDelete.add(new CarAvailabilityInterval(-id, beginDay, beginTime, endDay, endTime));
                        }
                    } catch(ArrayIndexOutOfBoundsException | NumberFormatException e ) {
                        flash("error", "Er is een fout gebeurd bij het doorgeven van de beschikbaarheidswaarden.");
                        return redirect(routes.Cars.detail(carId));
                    }
                }

                dao.addOrUpdateAvailabilities(car, availabilitiesToAddOrUpdate);
                dao.deleteAvailabilties(availabilitiesToDelete);

                context.commit();
                flash("success", "Je wijzigingen werden succesvol toegepast.");
                return redirect(routes.Cars.detail(car.getId()));
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
     * @return redirect to the car detailPage
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    public static Result updatePriviliged(int carId, String valuesString) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            CarDAO dao = context.getCarDAO();
            Car car = dao.getCar(carId);

            if (car == null) {
                flash("danger", "Car met ID=" + carId + " bestaat niet.");
                return badRequest(carList());
            }

            User currentUser = DataProvider.getUserProvider().getUser();
            if(!(car.getOwner().getId() == currentUser.getId() || DataProvider.getUserRoleProvider().hasRole(currentUser.getId(), UserRole.CAR_ADMIN))){
                flash("danger", "Je heeft geen rechten tot het bewerken van deze wagen.");
                return badRequest(carList());
            }

            try {
                String[] values = valuesString.split(";");

                List<User> priviliged = car.getPriviliged();

                List<User> usersToAdd = new ArrayList<>();
                List<User> usersToDelete = new ArrayList<>();

                for(String value : values) {
                    try {
                        int id = Integer.parseInt(value);
                        User user;
                        if(id > 0) { // create
                            user = context.getUserDAO().getUser(id, false);
                            if(!userInList(id, priviliged))
                                usersToAdd.add(user);
                        } else { // delete
                            user = context.getUserDAO().getUser(-1 * id, false);
                            usersToDelete.add(user);
                        }
                        if(user == null) {
                            flash("error", "De opgegeven gebruiker bestaat niet.");
                            return redirect(routes.Cars.detail(carId));
                        }
                    } catch(NumberFormatException e) {
                        flash("error", "Er is een fout gebeurd bij het doorgeven van de gepriviligieerden.");
                        return redirect(routes.Cars.detail(carId));
                    }
                }

                dao.addPriviliged(car, usersToAdd);
                dao.deletePriviliged(car, usersToDelete);

                context.commit();
                flash("success", "Je wijzigingen werden succesvol toegepast.");
                return redirect(routes.Cars.detail(car.getId()));
            } catch (DataAccessException ex) {
                context.rollback();
                throw ex;
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    private static boolean userInList(int userId, List<User> users) {
        for(User u : users) {
            if(u.getId() == userId) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param carId The car to show details of
     * @return A detail page of the car (only available to car_user+)
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_USER, UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN, UserRole.CAR_ADMIN})
    public static F.Promise<Result> detail(int carId) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            CarDAO dao = context.getCarDAO();
            final Car car = dao.getCar(carId);

            if(car == null) {
                flash("danger", "Auto met ID=" + carId + " bestaat niet.");
                return F.Promise.promise(new F.Function0<Result>() {
                    @Override
                    public Result apply() throws Throwable {
                        return badRequest(carList());
                    }
                });
            } else {
                if (DataProvider.getSettingProvider().getBoolOrDefault("show_maps", true)) {
                    return Maps.getLatLongPromise(car.getLocation().getId()).map(
                            new F.Function<F.Tuple<Double, Double>, Result>() {
                                public Result apply(F.Tuple<Double, Double> coordinates) {
                                    return ok(detail.render(car, coordinates == null ? null : new Maps.MapDetails(coordinates._1, coordinates._2, 14)));
                                }
                            }
                    );
                } else {
                    return F.Promise.promise(new F.Function0<Result>() {
                        @Override
                        public Result apply() throws Throwable {
                            return ok(detail.render(car, null));
                        }
                    });
                }
            }
        } catch (DataAccessException ex) {
            throw ex;
            //TODO: log
        }
    }

    /************************
     *      Car costs       *
     ************************/

    public static class CarCostModel {

        public String description;
        public BigDecimal amount;
        public BigDecimal mileage;
        public DateTime time;


        public String validate() {
            if("".equals(description))
                return "Geef aub een beschrijving op.";
            return null;
        }
    }


    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    public static Result getCarCostModal(int id){
        // TODO: hide from other users (badRequest)

        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()){
            CarDAO dao = context.getCarDAO();
            Car car = dao.getCar(id);
            if(car == null){
                return badRequest("Fail."); //TODO: error in flashes?
            } else {
                return ok(addcarcostmodal.render(Form.form(CarCostModel.class), car));
            }
        } catch(DataAccessException ex){
            throw ex; //log?
        }
    }

    /**
     * Method: POST
     * @return redirect to the CarCostForm you just filled in or to the car-detail page
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER})
    public static Result addNewCarCost(int carId) {
        Form<CarCostModel> carCostForm = Form.form(CarCostModel.class).bindFromRequest();
        if (carCostForm.hasErrors()) {
            try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
                CarDAO dao = context.getCarDAO();
                Car car = dao.getCar(carId);
                flash("danger", "Kost toevoegen mislukt.");
                return redirect(routes.Cars.detail(carId));
            }catch(DataAccessException ex){
                throw ex; //log?
            }

        } else {
            try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
                CarCostDAO dao = context.getCarCostDAO();
                try {
                    CarCostModel model = carCostForm.get();
                    CarDAO cardao = context.getCarDAO();
                    Car car = cardao.getCar(carId);
                    Http.MultipartFormData body = request().body().asMultipartFormData();
                    Http.MultipartFormData.FilePart proof = body.getFile("picture");
                    if (proof != null) {
                        String contentType = proof.getContentType();
                        if (!FileHelper.isDocumentContentType(contentType)) {
                            flash("danger", "Verkeerd bestandstype opgegeven. Enkel documenten zijn toegelaten. (ontvangen MIME-type: " + contentType + ")");
                            return redirect(routes.Cars.detail(carId));
                        } else {
                            try {
                                Path relativePath = FileHelper.saveFile(proof, ConfigurationHelper.getConfigurationString("uploads.carboundproofs"));
                                    FileDAO fdao = context.getFileDAO();
                                    try {
                                        models.File file = fdao.createFile(relativePath.toString(), proof.getFilename(), proof.getContentType());
                                        CarCost carCost = dao.createCarCost(car, model.amount, model.mileage, model.description, model.time, file.getId());
                                        context.commit();
                                        if (carCost == null) {
                                            flash("danger", "Failed to add the carcost to the database. Contact administrator.");
                                            return redirect(routes.Cars.detail(carId));
                                        }
                                        Notifier.sendCarCostRequest(carCost);
                                        flash("success", "Je autokost werd toegevoegd.");
                                        return redirect(routes.Cars.detail(carId));
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
                        return redirect(routes.Application.index());
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
     *
     * @return index page containing all the carcost requests
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_ADMIN})
    public static Result showCarCosts() {
        return ok(carCostsAdmin.render());
    }

    public static Result showCarCostsPage(int page, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField field = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        // Check if admin or car owner
        User user = DataProvider.getUserProvider().getUser();
        UserRoleProvider userRoleProvider = DataProvider.getUserRoleProvider();
        if(!userRoleProvider.hasRole(user, UserRole.CAR_ADMIN) || !userRoleProvider.hasRole(user, UserRole.SUPER_USER)) {
            String carIdString = filter.getValue(FilterField.CAR_ID);
            int carId;
            if(carIdString.equals("")) {
                carId = -1;
            } else {
                carId = Integer.parseInt(carIdString);
            }
            try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
                CarDAO carDAO = context.getCarDAO();
                List<Car> listOfCars = carDAO.getCarsOfUser(user.getId());
                // Check if carId in cars
                boolean isCarOfUser = false;
                for(Car c : listOfCars) {
                    if(c.getId() == carId) {
                        isCarOfUser = true;
                        break;
                    }
                }
                if(!isCarOfUser) {
                    flash("danger", "Je bent niet de eigenaar van deze auto.");
                    return badRequest(cars.render(listOfCars));
                }

            } catch (DataAccessException ex) {
                throw ex;
            }
        }

        return ok(carCostList(page, field, asc, filter));

    }

    private static Html carCostList(int page, FilterField orderBy, boolean asc, Filter filter) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            CarCostDAO dao = context.getCarCostDAO();

            if(orderBy == null) {
                orderBy = FilterField.CAR_COST_DATE;
            }

            List<CarCost> listOfResults = dao.getCarCostList(orderBy, asc, page, PAGE_SIZE_CAR_COSTS, filter);

            int amountOfResults = dao.getAmountOfCarCosts(filter);
            int amountOfPages = (int) Math.ceil( amountOfResults / (double) PAGE_SIZE_CAR_COSTS);

            return carCostspage.render(listOfResults, page, amountOfResults, amountOfPages);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     *
     * Called when a car-bound cost of a car is approved by the car admin.
     *
     * @param carCostId  The carCost being approved
     * @return the carcost index page if returnToDetail is 0, car detail page if 1.
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_ADMIN})
    public static Result approveCarCost(int carCostId, int returnToDetail) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            CarCostDAO dao = context.getCarCostDAO();
            CarCost carCost = dao.getCarCost(carCostId);
            carCost.setStatus(CarCostStatus.ACCEPTED);
            dao.updateCarCost(carCost);
            context.commit();
            Notifier.sendCarCostStatusChanged(carCost.getCar().getOwner(), carCost, true);

            flash("success", "Autokost succesvol geaccepteerd");
            if(returnToDetail==0){
                return redirect(routes.Cars.showCarCosts());
            }else{
                return redirect(routes.Cars.detail(carCost.getCar().getId()));
            }
        }catch(DataAccessException ex) {
            throw ex;
        }


    }

    /**
     * Method: GET
     *
     * Called when a car-bound cost of a car is approved by the car admin.
     *
     * @param carCostId  The carCost being approved
     * @return the carcost index page
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_ADMIN})
    public static Result refuseCarCost(int carCostId, int returnToDetail) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            CarCostDAO dao = context.getCarCostDAO();
            CarCost carCost = dao.getCarCost(carCostId);
            carCost.setStatus(CarCostStatus.REFUSED);
            dao.updateCarCost(carCost);
            context.commit();
            Notifier.sendCarCostStatusChanged(carCost.getCar().getOwner(), carCost, false);
            if(returnToDetail==0){
                flash("success", "Autokost succesvol geweigerd");
                return redirect(routes.Cars.showCarCosts());
            }else{
                flash("success", "Autokost succesvol geweigerd");
                return redirect(routes.Cars.detail(carCost.getCar().getId()));
            }
        }catch(DataAccessException ex) {
            throw ex;
        }
    }

    @RoleSecured.RoleAuthenticated()
    public static Result getProof(int proofId) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            return FileHelper.getFileStreamResult(context.getFileDAO(), proofId);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

}
