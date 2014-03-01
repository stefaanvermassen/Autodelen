package database.jdbc;

import database.*;
import models.Address;
import models.Car;
import models.CarFuel;
import models.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

/**
 * Created by HannesM on 26/02/14.
 */
public class JDBCDAOTest {

    private UserDAO userDAO;
    private AddressDAO addressDAO;
    private CarDAO carDAO;
    private ReservationDAO reservationDAO;

    private List<User> users = new ArrayList<>();
    private List<Address> addresses = new ArrayList<>();
    private List<Car> cars = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext();
        userDAO = context.getUserDAO();
        addressDAO = context.getAddressDAO();
        carDAO = context.getCarDAO();
        reservationDAO = context.getReservationDAO();
    }


    @Test
    public void testUserDAO() throws Exception {
        createUsersAndAddresses();
        getUserTest();
        deleteUsersAndAddresses();
    }

    /*
     * Also tests Users
     */
    @Test
    public void testCarDAO() throws Exception {
        createUsersAndAddresses();
        createCars();
        getCarTest();
        deleteCars();
        deleteUsersAndAddresses();
    }

    /*
     * Also tests Users and Cars
     */
    @Test
    public void testReservationDAO() throws Exception {
        createUsersAndAddresses();
        createCars();
        //TODO
        //createReservations();
        //getReservationTest();
        //deleteReservations();
        deleteCars();
        deleteUsersAndAddresses();
    }

    /*
     * Creates 100 random users and addresses in the database and in private Lists users and addresses
     */
    private void createUsersAndAddresses() throws Exception {
        Scanner sc = new Scanner(new File("test/database/random_users.txt"));
        sc.useDelimiter("\\t|\\n");
        sc.nextLine(); //skip header first time
        while(sc.hasNext()) {
            String email = sc.next();
            String pass = sc.next();
            String firstName = sc.next();
            String lastName = sc.next();
            String phone = sc.next();

            String street = sc.next();
            String nr = sc.nextInt() + "";
            String zip = sc.next();
            String city = sc.next();

            Address address = addressDAO.createAddress(zip,city,street,nr,"");
            User user = userDAO.createUser(email,pass,firstName,lastName,phone,address);

            users.add(user);
            addresses.add(address);
        }
        sc.close();
    }

    /*
     * First createUsersAndAddresses() has to be called
     */
    private void getUserTest() {
        for(User user : users) {
            Address address = user.getAddress();

            User returnUser = userDAO.getUser(user.getEmail());
            Address returnAddress = user.getAddress();

            Assert.assertEquals(address.getBus(),returnAddress.getBus());
            Assert.assertEquals(address.getZip(),returnAddress.getZip());
            Assert.assertEquals(address.getStreet(),returnAddress.getStreet());
            Assert.assertEquals(address.getCity(),returnAddress.getCity());
            Assert.assertEquals(returnUser.getEmail(),user.getEmail());
            Assert.assertEquals(returnUser.getPassword(),user.getPassword());
            Assert.assertEquals(returnUser.getFirstName(),user.getFirstName());
            Assert.assertEquals(returnUser.getLastName(),user.getLastName());
        }
    }

    /*
     * First createUsersAndAddresses() has to be called
     */
    private void deleteUsersAndAddresses() {
        Iterator<User> i = users.iterator();
        while(i.hasNext()) {
            User user = i.next();
            Address address = user.getAddress();
            userDAO.deleteUser(user);
            addressDAO.deleteAddress(address);
            i.remove();
            addresses.remove(addresses.indexOf(address));
        }
    }

    /*
     * First createUsersAndAddresses() has to be called
     */
    private void createCars() throws Exception {
        Scanner sc = new Scanner(new File("test/database/random_cars.txt"));
        sc.useDelimiter("\\t|\\n");
        sc.nextLine(); // skip header first time

        while(sc.hasNext()) {
            String brand = sc.next();
            String type = sc.next();
            int seats = sc.nextInt();
            int doors = sc.nextInt();
            int year = sc.nextInt();
            boolean gps = sc.nextBoolean();
            boolean hook = sc.nextBoolean();
            String fuel = sc.next();
            CarFuel carFuel = CarFuel.valueOf(fuel);
            int fuelEconomy = sc.nextInt();
            int estimatedValue = sc.nextInt();
            int ownerAnnualKm = sc.nextInt();
            int owner_id = sc.nextInt();

            // To keep it simple, we take a random user_id, therefore there have to be users in the database/list
            User user = users.get(owner_id);
            // To keep it simple, we give the Address the same id as the user
            Address address = addresses.get(owner_id);
            String comments = sc.next();

            Car car = carDAO.createCar(brand, type, address, seats, doors, year, gps, hook, carFuel, fuelEconomy, estimatedValue, ownerAnnualKm, user, comments);
            cars.add(car);
        }
    }
    /*
    * First createCars() has to be called
    */
    private void getCarTest() {
        for(Car car : cars) {
            Car returnCar = carDAO.getCar(car.getId());
            Assert.assertEquals(car.getBrand(), returnCar.getBrand());
            Assert.assertEquals(car.getComments(), returnCar.getComments());
            Assert.assertEquals(car.getLastEdit(), returnCar.getLastEdit());
            Assert.assertEquals(car.getType(), returnCar.getType());
            Assert.assertEquals(car.getDoors(), returnCar.getDoors());
            Assert.assertEquals(car.getEstimatedValue(), returnCar.getEstimatedValue());
            Assert.assertEquals(car.getFuel(), returnCar.getFuel());
            Assert.assertEquals(car.getFuelEconomy(), returnCar.getFuelEconomy());
            Assert.assertEquals(car.getLocation().getId(), returnCar.getLocation().getId());
            Assert.assertEquals(car.getOwner().getFirstName(), returnCar.getOwner().getFirstName());
            Assert.assertEquals(car.getOwnerAnnualKm(), returnCar.getOwnerAnnualKm());
            Assert.assertEquals(car.getSeats(), returnCar.getSeats());
            Assert.assertEquals(car.getYear(), returnCar.getYear());
        }
    }

    /*
    * First createCars() has to be called
    */
    private void deleteCars() {
        Iterator<Car> i = cars.iterator();
        while(i.hasNext()) {
            Car car = i.next();
            carDAO.deleteCar(car);
            i.remove();
        }
    }

}
