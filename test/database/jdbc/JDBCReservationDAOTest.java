package database.jdbc;

import database.ReservationDAO;
import models.Car;
import models.Reservation;
import models.User;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by HannesM on 28/02/14.
 */
public class JDBCReservationDAOTest {

    ReservationDAO reservationDAO;
    @Before
    public void setUp() throws Exception {
        // Gives Error on DB.getConnection();
        //DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext();
        //reservationDao = context.getReservationDAO();
    }

    /*
     * Tests AddressDAO.createAddress() and UserDAO.createAddress()
     */
    @Test
    public void testUserDAO() throws Exception {
        Scanner sc = new Scanner(new File("test/database/random_reservations.txt"));
        sc.useDelimiter("\t|\r\n");
        // Skip header
        sc.nextLine();

        int i = 0;
        while(sc.hasNext()) {
            String fromString = sc.next();
            Date fromDate = new SimpleDateFormat("M/d/y H:m").parse(fromString);
            DateTime from = new DateTime(fromDate);

            String toString = sc.next();
            Date toDate = new SimpleDateFormat("M/d/y H:m").parse(toString);
            DateTime to = new DateTime(toDate);

            // Idea: To keep it simple, we might compare the dates and make from the earliest date

            int car_id = sc.nextInt();
            Car car = new Car();
            car.setId(car_id);

            int user_id = sc.nextInt();
            User user = new User();
            user.setId(user_id);

            Reservation reservation = reservationDAO.createReservation(from, to, car, user);

            Assert.assertEquals(from, reservation.getFrom());
            //...
            i++;

        }
        Assert.assertEquals(i, 100);
    }
}
