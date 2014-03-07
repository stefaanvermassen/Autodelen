package controllers;

import database.DatabaseHelper;
import database.mocking.TestDataAccessProvider;
import org.junit.*;
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
}
