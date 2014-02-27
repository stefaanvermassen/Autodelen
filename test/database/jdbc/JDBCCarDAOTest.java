package database.jdbc;

import database.CarDAO;
import models.Address;
import models.Car;
import models.CarFuel;
import models.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Scanner;

/**
 * Created by HannesM on 27/02/14.
 */
public class JDBCCarDAOTest {
    private CarDAO carDAO;

    @Before
    public void setUp() throws Exception {
        // Gives Error on DB.getConnection();
        //DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext();
        //dao = context.getCarDAO();
    }

    @Test
    public void testCarDAO() throws Exception {
        Scanner sc = new Scanner(new File("test/database/random_cars.txt"));
        sc.useDelimiter("\t");
        // Skip header
        sc.nextLine();

        int i = 0;
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

            // To keep it simple, we take a random user_id
            User user = new User(owner_id, "", "", "", "", null);
            // To keep it simple, we give the Address the same id as the user
            Address address = new Address(owner_id, "", "", "", "", "");
            String comments = sc.next();

            Car car = carDAO.createCar(brand, type, address, seats, doors, year, gps, hook, carFuel, fuelEconomy, estimatedValue, ownerAnnualKm, user, comments);

            //Assert.assertEquals(brand, car.getBrand())
            //...
            sc.nextLine();
            i++;

        }
        Assert.assertEquals(i, 100);
    }
}
