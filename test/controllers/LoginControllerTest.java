package controllers;

import database.DatabaseHelper;
import database.UserDAO;
import database.mocking.TestDataAccessProvider;
import models.User;
import models.UserStatus;
import org.junit.*;
import org.mindrot.jbcrypt.BCrypt;
import play.mvc.Result;
import play.mvc.Http.Cookie;

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
                        controllers.routes.ref.Login.authenticate("/"),
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
        dao.updateUser(user, true);
        return dao.getUser(user.getId(), false);
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
                        controllers.routes.ref.Login.authenticate("/"),
                        fakeRequest(POST, "/login").withFormUrlEncodedBody(data)
                );

                assertEquals("Valid login", 303, status(result));

                Cookie loginCookie = cookie("PLAY_SESSION", result); //TODO: fix, this is the most ugly hack I've ever seen x)

                // Test if we can access dashboard now
                Result result2 = callAction(
                        controllers.routes.ref.Dashboard.index(),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Requesting dashboard when logged in", OK, status(result2));

                // Test if we can logout
                Result result3 = callAction(
                        controllers.routes.ref.Login.logout(),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Requesting logout", 303, status(result3));

                // Test if we cannot access dashboard now
                Result result4 = callAction(
                        controllers.routes.ref.Dashboard.index(),
                        fakeRequest().withCookies()
                );
                assertNotEquals("Requesting dashboard when nog loggedin", OK, status(result4));
            }
        });
    }
}
