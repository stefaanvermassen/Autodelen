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
        DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext();
        user_dao = context.getUserDAO();
        address_dao = context.getAddressDAO();
    }

    /*
     * Tests AddressDAO.createAddress() and UserDAO.createAddress()
     */
    @Test
    public void testUserDAO() throws Exception {
        Scanner sc = new Scanner(new File("test/database/random_users.txt"));
        sc.useDelimiter("\\t|\\n");

        while(sc.hasNext()) {
        	sc.nextLine(); //skip header first time
            String email = sc.next();
            String pass = sc.next();
            String firstName = sc.next();
            String lastName = sc.next();
            String phone = sc.next();

            String street = sc.next();
            String nr = sc.nextInt() + "";
            String zip = sc.next();
            String city = sc.next();
           
            Address address = address_dao.createAddress(zip,city,street,nr,"");
            User user = user_dao.createUser(email,pass,firstName,lastName,phone,address);
            User returnUser = user_dao.getUser(email);
            Address returnAddress = user.getAddress();
            Assert.assertEquals(address.getBus(),returnAddress.getBus());
            Assert.assertEquals(address.getZip(),returnAddress.getZip());
            Assert.assertEquals(address.getStreet(),returnAddress.getStreet());
            Assert.assertEquals(address.getCity(),returnAddress.getCity());
            Assert.assertEquals(returnUser.getEmail(),user.getEmail());
            Assert.assertEquals(returnUser.getPassword(),user.getPassword());
            Assert.assertEquals(returnUser.getFirstName(),user.getFirstName());
            Assert.assertEquals(returnUser.getLastName(),user.getLastName());
            
            user_dao.deleteUser(returnUser);
            address_dao.deleteAddress(returnAddress);
        }
        sc.close();
    }
}
