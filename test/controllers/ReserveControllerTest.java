package controllers;

import controllers.util.TestHelper;
import database.DataAccessContext;
import database.DatabaseHelper;
import models.*;
import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Cookie;
import play.mvc.Result;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static play.test.Helpers.*;
import static play.test.Helpers.status;

/**
 * Created by HannesM on 4/04/14.
 */
public class ReserveControllerTest {

    private User user;
    private TestHelper helper;
    private Cookie loginCookie;

    @Before
    public void setUp(){
        helper = new TestHelper();
        helper.setTestProvider();
        DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext();
        user = helper.createRegisteredUser("test@test.com", "1234piano", "Pol", "Thijs",new UserRole[]{UserRole.CAR_USER});
    }

    /**
     * Tests method Reserve.reserve()
     * Once with an existing car, and once with a non-existing car
     */
    @Test
    public void testReserve(){
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                loginCookie = helper.login(user,"1234piano");

                // Test if we can see a reservation-screen of a car
                // First create a car
                Car car = helper.createCar("MijnenAuto", "Opel", "Corsa", null, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user, "");
                Result result2 = callAction(
                        controllers.routes.ref.Reserve.reserve(car.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Requesting reserve", OK, status(result2));

                // Test if we can see a reservation-screen of a car that doesn't exist
                Result result3 = callAction(
                        controllers.routes.ref.Reserve.reserve(car.getId()-1),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Requesting reserve for unexisting car", BAD_REQUEST, status(result3));
                helper.logout();
            }
        });
    }

    @Test
    public void testConfirmReservation() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                loginCookie = helper.login(user,"1234piano");

                // Test if we can make a reservation of a car
                // First create a car
                Car car = helper.createCar("MijnenAuto", "Opel", "Corsa", null, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user, "");
                Map<String,String> reserveData = new HashMap<>();
                reserveData.put("from", "2015-01-01 00:00");
                reserveData.put("until", "2015-01-02 00:00");
                Result result2 = callAction(
                        controllers.routes.ref.Reserve.confirmReservation(car.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Creating reservation", 303, status(result2));

                // Try to create a reservation that overlaps with the reservation just created
                reserveData.put("from", "2015-01-01 00:05");
                reserveData.put("until", "2015-01-02 00:05");
                Result result3 = callAction(
                        controllers.routes.ref.Reserve.confirmReservation(car.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Creating reservation with overlapping dates from other reservation", BAD_REQUEST, status(result3));

                // Test if we can make a reservation where from > until
                reserveData.put("from", "2016-01-02 00:05");
                reserveData.put("until", "2016-01-01 00:05");
                Result result4 = callAction(
                        controllers.routes.ref.Reserve.confirmReservation(car.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Creating reservation where from > until", BAD_REQUEST, status(result4));

                // Test if we can make a reservation on a date that doesn't exist (feb 30th)
                reserveData.put("from", "2014-02-30 00:00");
                reserveData.put("until", "2014-03-30 00:00");
                Result result5 = callAction(
                        controllers.routes.ref.Reserve.confirmReservation(car.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Creating reservation on nonexisting date", BAD_REQUEST, status(result5));

                // Test if we can make a reservation on a date that is in the past
                reserveData.put("from", "1999-02-01 00:00");
                reserveData.put("until", "1999-02-02 00:00");
                Result result6 = callAction(
                        controllers.routes.ref.Reserve.confirmReservation(car.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Creating reservation on nonexisting date", BAD_REQUEST, status(result6));

                // Test if we can make a reservation of a car that doesn't exist
                reserveData.put("from", "2015-01-01 00:00");
                reserveData.put("until", "2015-01-02 00:00");
                Result result7 = callAction(
                        controllers.routes.ref.Reserve.confirmReservation(car.getId() - 1),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Creating reservation for nonexisting car", BAD_REQUEST, status(result7));
            }
        });
    }


    @Test
    public void authorizeTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                loginCookie = helper.login(user,"1234piano");

                UserRole[] userRoles = {UserRole.INFOSESSION_ADMIN, UserRole.MAIL_ADMIN, UserRole.PROFILE_ADMIN, UserRole.RESERVATION_ADMIN, UserRole.SUPER_USER, UserRole.CAR_OWNER};

                for(int i = 0; i < userRoles.length; i++) {
                    if(i != 0) {
                        helper.removeUserRole(user, userRoles[i - 1]);
                    }
                    helper.addUserRole(user, userRoles[i]);

                    // Now let's try to see the pages

                    // Test if we can see a reservation-screen of a car
                    // First create a car
                    Car car = helper.createCar("MijnenAuto" + i, "Opel", "Corsa", null, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user, "");
                    Result result2 = callAction(
                            controllers.routes.ref.Reserve.reserve(car.getId()),
                            fakeRequest().withCookies(loginCookie)
                    );
                    if(userRoles[i] == UserRole.CAR_USER)
                        assertEquals("Requesting reserve as " + userRoles[i].toString(), OK, status(result2));
                    else
                        assertEquals("Requesting reserve as " + userRoles[i].toString(), UNAUTHORIZED, status(result2));
                }
            }
        });
    }
}
