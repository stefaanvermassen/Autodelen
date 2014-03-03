package database.jdbc;

import database.*;
import models.Address;
import models.Car;
import models.CarFuel;
import models.Reservation;
import models.User;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by HannesM on 26/02/14.
 */
public class JDBCDAOTest {

    private AddressDAO addressDAO;
    private UserDAO userDAO;
    private CarDAO carDAO;
    private ReservationDAO reservationDAO;

    private List<Address> addresses = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private List<Car> cars = new ArrayList<>();
    private List<Reservation> reservations = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext();

        addressDAO = context.getAddressDAO();
        userDAO = context.getUserDAO();
        carDAO = context.getCarDAO();
        reservationDAO = context.getReservationDAO();
    }

    @Test
    public void testAddressDAO() throws Exception {
        try {
            createAddresses();
            getAddressTest();
            updateAddressTest();
        } catch(Exception e) {
            deleteAddresses();
            throw e;
        }
        deleteAddresses();
    }

    @Test
    public void testUserDAO() throws Exception {
        try {
            createAddresses();
            createUsers();
            getUserTest();
            updateUserTest();
        } catch(Exception e) {
            deleteUsers();
            deleteAddresses();
            throw e;
        }

        deleteUsers();
        deleteAddresses();
    }

    /*
     * Also tests Users
     */
    @Test
    public void testCarDAO() throws Exception {
        try {
            createAddresses();
            createUsers();
            createCars();
            getCarTest();
            updateCarTest();
        } catch(Exception e) {
            deleteCars();
            deleteUsers();
            deleteAddresses();
            throw e;
        }

        deleteCars();
        deleteUsers();
        deleteAddresses();
    }

    /*
     * Also tests Users and Cars
     */
    @Test
    public void testReservationDAO() throws Exception {
        try {
            createAddresses();
            createUsers();
            createCars();
            createReservations();
            getReservationTest();
            updateReservationTest();
        } catch(Exception e) {
            deleteReservations();
            deleteCars();
            deleteUsers();
            deleteAddresses();
            throw e;
        }

        deleteReservations();
        deleteCars();
        deleteUsers();
        deleteAddresses();
    }

    /*
     * Creates 100 random addresses in the database and in private List address
     */
    private void createAddresses() throws Exception {
        Scanner sc = new Scanner(new File("test/database/random_addresses.txt"));
        sc.useDelimiter("\\t|\\r\\n");
        sc.nextLine(); //skip header first time
        while(sc.hasNext()) {
            String street = sc.next();
            String nr = sc.next();
            String zip = sc.next();
            String city = sc.next();

            Address address = addressDAO.createAddress(zip, city, street, nr, "");

            addresses.add(address);
        }
        sc.close();
    }

    /*
    * First createAddresses() has to be called
    */
    private void getAddressTest() throws Exception {
        for(Address address : addresses) {
            Address returnAddress = addressDAO.getAddress(address.getId());

            Assert.assertEquals(address.getBus(),returnAddress.getBus());
            Assert.assertEquals(address.getZip(),returnAddress.getZip());
            Assert.assertEquals(address.getNumber(),returnAddress.getNumber());
            Assert.assertEquals(address.getStreet(),returnAddress.getStreet());
            Assert.assertEquals(address.getCity(),returnAddress.getCity());
        }
    }

    /*
    * First createAddresses() has to be called
    */
    private void updateAddressTest() throws Exception {
        for(Address address : addresses) {
            address.setStreet(address.getStreet() + " (test)");
            address.setZip(address.getZip() + "AB");
            address.setCity(address.getCity() + " AB");
            address.setNumber(address.getNumber() + " AB");
            addressDAO.updateAddress(address);
            Address returnAddress = addressDAO.getAddress(address.getId());

            Assert.assertEquals(address.getBus(),returnAddress.getBus());
            Assert.assertEquals(address.getZip(),returnAddress.getZip());
            Assert.assertEquals(address.getNumber(),returnAddress.getNumber());
            Assert.assertEquals(address.getStreet(),returnAddress.getStreet());
            Assert.assertEquals(address.getCity(),returnAddress.getCity());
        }
    }
    /*
     * First createAddresses() has to be called
     */
    private void deleteAddresses() throws Exception {
        Iterator<Address> iAddresses = addresses.iterator();
        while(iAddresses.hasNext()) {
            Address address = iAddresses.next();
            addressDAO.deleteAddress(address);
            iAddresses.remove();
        }
    }

    /*
     * Creates 100 random users in the database and in private List users
     */
    private void createUsers() throws Exception {
        Scanner sc = new Scanner(new File("test/database/random_users.txt"));
        sc.useDelimiter("\\t|\\r\\n");
        sc.nextLine(); //skip header first time
        while(sc.hasNext()) {
            String email = sc.next();
            String pass = sc.next();
            String firstName = sc.next();
            String lastName = sc.next();

            User user = userDAO.createUser(email,pass,firstName,lastName);

            users.add(user);
        }
        sc.close();
    }

    /*
     * First createUsers() has to be called
     */
    private void getUserTest() {
        for(User user : users) {
            User returnUser = userDAO.getUser(user.getEmail());

            Assert.assertEquals(returnUser.getEmail(),user.getEmail());
            Assert.assertEquals(returnUser.getPassword(),user.getPassword());
            Assert.assertEquals(returnUser.getFirstName(),user.getFirstName());
            Assert.assertEquals(returnUser.getLastName(),user.getLastName());
        }
    }

    /*
     * First createUsers() has to be called
     */
    private void updateUserTest() {
        for(User user : users) {
            user.setEmail(user.getEmail() + ".com");
            user.setFirstName(user.getFirstName() + "Test");
            user.setLastName(user.getLastName() + "Test");
            user.setPassword(user.getPassword() + "Test");

            userDAO.updateUser(user);
            User returnUser = userDAO.getUser(user.getId());

            Assert.assertEquals(returnUser.getEmail(),user.getEmail());
            Assert.assertEquals(returnUser.getPassword(),user.getPassword());
            Assert.assertEquals(returnUser.getFirstName(),user.getFirstName());
            Assert.assertEquals(returnUser.getLastName(),user.getLastName());
        }
    }

    /*
     * First createUsers() has to be called
     */
    private void deleteUsers() {
        Iterator<User> iUsers = users.iterator();
        while(iUsers.hasNext()) {
            User user = iUsers.next();
            userDAO.deleteUser(user);
            iUsers.remove();
        }
    }

    /*
     * First createUsers() and createAddresses() has to be called
     */
    private void createCars() throws Exception {
        Scanner sc = new Scanner(new File("test/database/random_cars.txt"));
        sc.useDelimiter("\\t|\\r\\n");
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
        sc.close();
    }
    /*
    * First createCars() has to be called
    */
    private void getCarTest() {
        for(Car car : cars) {
            Car returnCar = carDAO.getCar(car.getId());
            Assert.assertEquals(car.getBrand(), returnCar.getBrand());
            Assert.assertEquals(car.getComments(), returnCar.getComments());
            // Last Edit is automatically generated in database and a bit later in Java source code
            //Assert.assertEquals(car.getLastEdit(), returnCar.getLastEdit());
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
    private void updateCarTest() {
        for(Car car : cars) {
            // Other fields should be updated too
            car.setBrand(car.getBrand() + "test");
            car.setType(car.getType() + "test");

            carDAO.updateCar(car);
            Car returnCar = carDAO.getCar(car.getId());

            Assert.assertEquals(car.getBrand(), returnCar.getBrand());
            Assert.assertEquals(car.getType(), returnCar.getType());
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

    /*
     * First createCars() and createAddresses() has to be called
     */
    private void createReservations() throws Exception {
    	Scanner sc = new Scanner(new File("test/database/random_reservations.txt"));
        sc.useDelimiter("\\t|\\r\\n");
    	sc.nextLine();

    	while(sc.hasNext()){
            String fromString = sc.next();
            Date fromDate = new SimpleDateFormat("M/d/y H:m").parse(fromString);
            DateTime from = new DateTime(fromDate);

            String toString = sc.next();
            Date toDate = new SimpleDateFormat("M/d/y H:m").parse(toString);
            DateTime to = new DateTime(toDate);

    		int carid = sc.nextInt();
            Car car = cars.get(carid-1);

    		int userid = sc.nextInt();
            User user = users.get(userid-1);

    		Reservation reservation = reservationDAO.createReservation(from, to, car, user);

    		reservations.add(reservation);
    	}
    	sc.close();
    }
    
    private void getReservationTest() {
        for(Reservation reservation : reservations) {
            Reservation returnReservation = reservationDAO.getReservation(reservation.getId());

            Assert.assertEquals(reservation.getCar().getId(),returnReservation.getCar().getId());
            Assert.assertEquals(reservation.getUser().getId(),returnReservation.getUser().getId());
            Assert.assertEquals(reservation.getTo(),returnReservation.getTo());
            Assert.assertEquals(reservation.getFrom(),returnReservation.getFrom());
        }
    }

    private void updateReservationTest() {
        for(Reservation reservation : reservations) {
            reservation.setCar(cars.get((reservation.getCar().getId() + 1) % 100));
            reservation.setUser(users.get((reservation.getUser().getId() + 1) % 100));
            reservation.setFrom(reservation.getFrom().plusHours(1));
            reservation.setTo(reservation.getTo().plusHours(1));

            reservationDAO.updateReservation(reservation);
            Reservation returnReservation = reservationDAO.getReservation(reservation.getId());

            Assert.assertEquals(reservation.getCar().getId(),returnReservation.getCar().getId());
            Assert.assertEquals(reservation.getUser().getId(),returnReservation.getUser().getId());
            Assert.assertEquals(reservation.getTo(),returnReservation.getTo());
            Assert.assertEquals(reservation.getFrom(),returnReservation.getFrom());
        }
    }
    
    private void deleteReservations(){
    	Iterator<Reservation> i = reservations.iterator();
        while(i.hasNext()) {
            Reservation reservation = i.next();
            reservationDAO.deleteReservation(reservation);
            i.remove();
        }
    }

}
