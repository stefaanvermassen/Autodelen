package controllers;

import database.DatabaseHelper;
import database.UserDAO;
import database.mocking.TestDataAccessProvider;
import models.User;
import models.UserStatus;
import org.junit.*;
import org.mindrot.jbcrypt.BCrypt;
import play.mvc.Result;

import java.util.HashMap;
import java.util.Map;

import static play.test.Helpers.*;
import static org.junit.Assert.*;

/**
 * Created by Cedric on 3/7/14.
 */
public class LoginControllerTest {

    @Test
    public void testBadLogin(){
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                DatabaseHelper.setDataAccessProvider(new TestDataAccessProvider()); // Required!!

                Map<String,String> data = new HashMap<>();
                data.put("email", "nonexistent@something.com");
                data.put("password", "thiswillnotwork");
                Result result = callAction(
                        controllers.routes.ref.Login.authenticate(),
                        fakeRequest().withFormUrlEncodedBody(data)
                );
                assertEquals(BAD_REQUEST, status(result));
            }
        });
    }

    private static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    private User createRegisteredUser(String email, String password, String firstName, String lastName){
        UserDAO dao = DatabaseHelper.getDataAccessProvider().getDataAccessContext().getUserDAO();
        User user = dao.createUser(email, hashPassword(password), firstName, lastName);
        user.setStatus(UserStatus.REGISTERED);
        dao.updateUser(user);
        return dao.getUser(user.getId());
    }

    // TODO: Can we really have a dependency on our models in controller testing??
    @Test
    public void testGoodLogin(){
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                DatabaseHelper.setDataAccessProvider(new TestDataAccessProvider()); // Required!!

                User user = createRegisteredUser("test@testing.com", "1234piano", "Joske", "Vermeulen");

                Map<String,String> data = new HashMap<>();
                data.put("email", user.getEmail());
                data.put("password", "1234piano");

                //TODO: this post always fails!??
                Result result = callAction(
                        controllers.routes.ref.Login.authenticate(),
                        fakeRequest(POST, "/login").withFormUrlEncodedBody(data)
                );

                assertEquals("Valid login", 303, status(result));

                // Test if we can access dashboard now
                Result result2 = callAction(
                        controllers.routes.ref.Dashboard.index(),
                        fakeRequest()
                );
                assertEquals("Requesting dashboard when logged in", OK, status(result2));

                // Test if we can logout
                Result result3 = callAction(
                        controllers.routes.ref.Login.logout(),
                        fakeRequest()
                );
                assertEquals("Requesting logout", 303, status(result3));

                // Test if we cannot access dashboard now
                Result result4 = callAction(
                        controllers.routes.ref.Dashboard.index(),
                        fakeRequest()
                );
                assertNotEquals("Requesting dashboard when nog loggedin", OK, status(result4));
            }
        });
    }
}
