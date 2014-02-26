package database.jdbc;

import database.AddressDAO;
import database.DataAccessContext;
import database.DatabaseHelper;
import database.UserDAO;
import models.Address;
import models.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Created by HannesM on 26/02/14.
 */
public class JDBCUserDAOTest {

    private UserDAO user_dao;
    private AddressDAO address_dao;

    @Before
    public void setUp() throws Exception {
        // Gives Error on DB.getConnection();
        //DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext();
        //dao = context.getUserDAO();
    }

    @Test
    public void testPopulateUser() throws Exception {

    }

    @Test
    public void testGetUser() throws Exception {

    }

    @Test
    public void fillDatabase() throws Exception {
        Scanner sc = new Scanner(new File("test/database/random_users.txt"));
        sc.useDelimiter("\t");
        // Skip header
        sc.nextLine();

        int i = 0;
        while(sc.hasNext()) {
            String email = sc.next();
            String pass = sc.next();
            String firstName = sc.next();
            String lastName = sc.next();
            String phone = sc.next();

            String street = sc.next();
            int nr = sc.nextInt();
            String zip = sc.next();
            String city = sc.next();

            //Address address = address_dao.createAddress(...);
            //User user = user_dao.createUser(..);

            //Assert.assertEquals(email, user.email)
            //...
            sc.nextLine();
            i++;

        }
        Assert.assertEquals(i, 100);
        /*User user = dao.createUser(email, pass, firstName, lastName, phone, address);
        Assert.assertEquals(email, user.getEmail());
        Assert.assertEquals(firstName, user.getFirstName());
        Assert.assertEquals(lastName, user.getLastName());
        Assert.assertEquals(phone, user.getPhone());
        Assert.assertEquals(address, user.getAddress());*/
    }


    @Test
    public void testUpdateUser() throws Exception {

    }
}
