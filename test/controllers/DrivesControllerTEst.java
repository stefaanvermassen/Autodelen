package controllers;

import controllers.util.TestHelper;
import models.*;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Result;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;
import static play.test.Helpers.status;

/**
 * Created by HannesM on 5/04/14.
 */
public class DrivesControllerTest {
    private User user;
    private TestHelper helper;
    private Http.Cookie loginCookie;

    @Before
    public void setUp(){
        helper = new TestHelper();
        helper.setTestProvider();
        user = helper.createRegisteredUser("test@test.com", "1234piano", "Pol", "Thijs",new UserRole[]{UserRole.CAR_USER});
    }

    /**
     * Tests Drives.index()
     */
    @Test
    public void testIndex() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                loginCookie = helper.login(user,"1234piano");

                // Reservation of own car
                // First create a reservation to see
                Car car = helper.createCar("MijnenAuto", "Opel", "Corsa", null, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user, "");
                DateTime from = new DateTime(2015, 1, 1, 0, 0);
                DateTime to = new DateTime(2015, 1, 2, 0, 0);
                Reservation reservation = helper.createReservation(from, to, car, user);

                Result result2 = callAction(
                        controllers.routes.ref.Drives.index(),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Index of drives", OK, status(result2));

                helper.logout();
            }
        });
    }

    @Test
    public void testShowDrivesPage() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                loginCookie = helper.login(user,"1234piano");

                // Reservation of own car
                // First create a reservation, so there's something to show
                Car car = helper.createCar("MijnenAuto", "Opel", "Corsa", null, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user, "");
                DateTime from = new DateTime(2015, 1, 1, 0, 0);
                DateTime to = new DateTime(2015, 1, 2, 0, 0);
                Reservation reservation = helper.createReservation(from, to, car, user);

                Result result2 = callAction(
                        controllers.routes.ref.Drives.showDrivesPage(1,1,"",""),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("ShowDrivesPage first page", OK, status(result2));

                helper.logout();
            }
        });
    }
    /**
     * Tests method Drives.details()
     * Once with an existing reservation and once with a non-existing reservation
     */
    @Test
    public void testDetails(){
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                loginCookie = helper.login(user,"1234piano");

                // Reservation of own car
                // First create a reservation
                Car car = helper.createCar("MijnenAuto", "Opel", "Corsa", null, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user, "");
                DateTime from = new DateTime(2015, 1, 1, 0, 0);
                DateTime to = new DateTime(2015, 1, 2, 0, 0);
                Reservation reservation = helper.createReservation(from, to, car, user);

                Result result2 = callAction(
                        controllers.routes.ref.Drives.details(reservation.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Details of reservation of own car", OK, status(result2));

                // Reservation of other car that I reserved
                User user2 = helper.createRegisteredUser("test2@test.com", "1234piano", "Niet", "Ik");
                Car car2 = helper.createCar("NietMijnenAuto", "Opel", "Corsa", null, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user2, "");
                Reservation reservation2 = helper.createReservation(from, to, car2, user);

                Result result4 = callAction(
                        controllers.routes.ref.Drives.details(reservation2.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Details of reservation of other car", OK, status(result4));

                // Reservation of other car that I didn't reserve
                Reservation reservation3 = helper.createReservation(from, to, car2, user2);

                Result result5 = callAction(
                        controllers.routes.ref.Drives.details(reservation3.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Details of reservation of other car that I didn't reserve", BAD_REQUEST, status(result5));

                // Reservation of my car that other user reserved
                Reservation reservation4 = helper.createReservation(from, to, car, user2);

                Result result6 = callAction(
                        controllers.routes.ref.Drives.details(reservation4.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Details of reservation of my car, reserved by other user", OK, status(result6));

                // Reservation that doesn't exist
                Result result3 = callAction(
                        controllers.routes.ref.Drives.details(reservation.getId() - 1),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Details of unexisting reservation", BAD_REQUEST, status(result3));

                helper.logout();
            }
        });
    }

    /**
     * Tests method Drives.approveReservation()
     * Once with an existing reservation of own car, of other car, of other car by other user, already cancelled, accepted, refused, re-requested
     * Once with a non-existing reservation
     */
    @Test
    public void testApproveReservation(){
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                loginCookie = helper.login(user,"1234piano");

                // Reservation of own car
                // First create a reservation
                Car car = helper.createCar("MijnenAuto", "Opel", "Corsa", null, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user, "");
                DateTime from = new DateTime(2015, 1, 1, 0, 0);
                DateTime to = new DateTime(2015, 1, 2, 0, 0);
                Reservation reservation = helper.createReservation(from, to, car, user);

                Result result2 = callAction(
                        controllers.routes.ref.Drives.approveReservation(reservation.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Approving reservation of own car", OK, status(result2));

                // Reservation of other car that I reserved
                User user2 = helper.createRegisteredUser("test2@test.com", "1234piano", "Niet", "Ik");
                Car car2 = helper.createCar("NietMijnenAuto", "Opel", "Corsa", null, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user2, "");
                Reservation reservation2 = helper.createReservation(from, to, car2, user);

                Result result4 = callAction(
                        controllers.routes.ref.Drives.approveReservation(reservation2.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Approving reservation of other car", BAD_REQUEST, status(result4));

                // Reservation of other car that I didn't reserve
                Reservation reservation3 = helper.createReservation(from, to, car2, user2);

                Result result5 = callAction(
                        controllers.routes.ref.Drives.approveReservation(reservation3.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Approving reservation of other car that I didn't reserve", BAD_REQUEST, status(result5));

                // Reservation of my car that other user reserved
                Reservation reservation4 = helper.createReservation(from, to, car, user2);

                Result result6 = callAction(
                        controllers.routes.ref.Drives.approveReservation(reservation4.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Approving reservation of my car, reserved by other user", OK, status(result6));

                // Reservation of my car that other user reserved AND CANCELLED
                Reservation reservation5 = helper.createReservation(from, to, car, user2);
                reservation5.setStatus(ReservationStatus.CANCELLED);
                helper.updateReservation(reservation5);

                Result result7 = callAction(
                        controllers.routes.ref.Drives.approveReservation(reservation5.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Approving reservation of my car, reserved by other user but already cancelled", BAD_REQUEST, status(result7));

                // Reservation of my car that other user reserved AND ALREADY REFUSED
                Reservation reservation6 = helper.createReservation(from, to, car, user2);
                reservation6.setStatus(ReservationStatus.REFUSED);
                helper.updateReservation(reservation6);

                Result result8 = callAction(
                        controllers.routes.ref.Drives.approveReservation(reservation6.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Approving reservation of my car, reserved by other user but already refused", BAD_REQUEST, status(result8));

                // Reservation of my car that other user reserved AND ALREADY ACCEPTED
                Reservation reservation7 = helper.createReservation(from, to, car, user2);
                reservation7.setStatus(ReservationStatus.ACCEPTED);
                helper.updateReservation(reservation7);

                Result result9 = callAction(
                        controllers.routes.ref.Drives.approveReservation(reservation7.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Approving reservation of my car, reserved by other user but already accepted", BAD_REQUEST, status(result9));

                // Reservation of my car that other user requested AGAIN AFTER PREVIOUS ACCEPTANCE
                Reservation reservation8 = helper.createReservation(from, to, car, user2);
                reservation8.setStatus(ReservationStatus.REQUEST_NEW);
                helper.updateReservation(reservation8);

                Result result10 = callAction(
                        controllers.routes.ref.Drives.approveReservation(reservation8.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Approving reservation of my car, reserved by other user but already accepted and afterwards requested again", OK, status(result10));

                // Reservation that doesn't exist
                Result result3 = callAction(
                        controllers.routes.ref.Drives.approveReservation(reservation.getId()-1),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Approving unexisting reservation", BAD_REQUEST, status(result3));

                helper.logout();
            }
        });
    }

    /**
     * Tests method Drives.refuseReservation()
     * Once with an existing reservation of own car, of other car, of other car by other user, once with empty reason, already cancelled, accepted, refused, re-requested
     * Once with a non-existing reservation
     */
    @Test
    public void testRefuseReservation(){
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                loginCookie = helper.login(user,"1234piano");

                // First create a reservation
                Car car = helper.createCar("MijnenAuto", "Opel", "Corsa", null, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user, "");
                DateTime from = new DateTime(2015, 1, 1, 0, 0);
                DateTime to = new DateTime(2015, 1, 2, 0, 0);
                Reservation reservation = helper.createReservation(from, to, car, user);

                Map<String,String> data = new HashMap<>();
                data.put("reason", "Test reden");
                Result result1 = callAction(
                        controllers.routes.ref.Drives.refuseReservation(reservation.getId(), 1, 1, 1, "", ""),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Refusing reservation of own car", OK, status(result1));

                // With empty reason
                data.put("reason", "");
                Result result2 = callAction(
                        controllers.routes.ref.Drives.refuseReservation(reservation.getId(), 1, 1, 1, "", ""),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Refusing reservation without reason", BAD_REQUEST, status(result2));

                data.put("reason", "Test reden");
                // Reservation of other car that I reserved
                User user2 = helper.createRegisteredUser("test2@test.com", "1234piano", "Niet", "Ik");
                Car car2 = helper.createCar("NietMijnenAuto", "Opel", "Corsa", null, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user2, "");
                Reservation reservation2 = helper.createReservation(from, to, car2, user);

                Result result4 = callAction(
                        controllers.routes.ref.Drives.refuseReservation(reservation2.getId(), 1, 1, 1, "", ""),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Refusing reservation of other car", BAD_REQUEST, status(result4));

                // Reservation of other car that I didn't reserve
                Reservation reservation3 = helper.createReservation(from, to, car2, user2);

                Result result5 = callAction(
                        controllers.routes.ref.Drives.refuseReservation(reservation3.getId(), 1, 1, 1, "", ""),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Refusing reservation of other car that I didn't reserve", BAD_REQUEST, status(result5));

                // Reservation of my car that other user reserved
                Reservation reservation4 = helper.createReservation(from, to, car, user2);

                Result result6 = callAction(
                        controllers.routes.ref.Drives.refuseReservation(reservation4.getId(), 1, 1, 1, "", ""),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Refusing reservation of my car, reserved by other user", OK, status(result6));

                // Reservation of my car that other user reserved AND CANCELLED
                Reservation reservation5 = helper.createReservation(from, to, car, user2);
                reservation5.setStatus(ReservationStatus.CANCELLED);
                helper.updateReservation(reservation5);

                Result result7 = callAction(
                        controllers.routes.ref.Drives.refuseReservation(reservation5.getId(), 1, 1, 1, "", ""),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Approving reservation of my car, reserved by other user but already cancelled", BAD_REQUEST, status(result7));

                // Reservation of my car that other user reserved AND ALREADY REFUSED
                Reservation reservation6 = helper.createReservation(from, to, car, user2);
                reservation6.setStatus(ReservationStatus.REFUSED);
                helper.updateReservation(reservation6);

                Result result8 = callAction(
                        controllers.routes.ref.Drives.refuseReservation(reservation6.getId(), 1, 1, 1, "", ""),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Approving reservation of my car, reserved by other user but already refused", BAD_REQUEST, status(result8));

                // Reservation of my car that other user reserved AND ALREADY ACCEPTED
                Reservation reservation7 = helper.createReservation(from, to, car, user2);
                reservation7.setStatus(ReservationStatus.ACCEPTED);
                helper.updateReservation(reservation7);

                Result result9 = callAction(
                        controllers.routes.ref.Drives.refuseReservation(reservation7.getId(), 1, 1, 1, "", ""),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Approving reservation of my car, reserved by other user but already accepted", BAD_REQUEST, status(result9));

                // Reservation of my car that other user requested AGAIN AFTER PREVIOUS ACCEPTANCE
                Reservation reservation8 = helper.createReservation(from, to, car, user2);
                reservation8.setStatus(ReservationStatus.REQUEST_NEW);
                helper.updateReservation(reservation8);

                Result result10 = callAction(
                        controllers.routes.ref.Drives.refuseReservation(reservation8.getId(), 1, 1, 1, "", ""),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Approving reservation of my car, reserved by other user but already accepted and afterwards requested again", OK, status(result10));


                // Reservation that doesn't exist
                Result result3 = callAction(
                        controllers.routes.ref.Drives.refuseReservation(reservation.getId() - 1, 1, 1, 1, "", ""),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Refusing unexisting reservation", BAD_REQUEST, status(result3));

                helper.logout();
            }
        });
    }

    /**
     * Tests method Drives.refuseReservation()
     * Once with an existing reservation of own car, of other car, of other car by other user,, already cancelled, accepted, refused, re-requested
     * Once with a non-existing reservation
     */
    @Test
    public void testCancelReservation(){
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                loginCookie = helper.login(user,"1234piano");

                // Reservation of own car
                // First create a reservation
                Car car = helper.createCar("MijnenAuto", "Opel", "Corsa", null, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user, "");
                DateTime from = new DateTime(2015, 1, 1, 0, 0);
                DateTime to = new DateTime(2015, 1, 2, 0, 0);
                Reservation reservation = helper.createReservation(from, to, car, user);

                Result result2 = callAction(
                        controllers.routes.ref.Drives.cancelReservation(reservation.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Cancelling reservation of own car", OK, status(result2));

                // Reservation of other car that I reserved
                User user2 = helper.createRegisteredUser("test2@test.com", "1234piano", "Niet", "Ik");
                Car car2 = helper.createCar("NietMijnenAuto", "Opel", "Corsa", null, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user2, "");
                Reservation reservation2 = helper.createReservation(from, to, car2, user);

                Result result4 = callAction(
                        controllers.routes.ref.Drives.cancelReservation(reservation2.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Cancelling reservation of other car", OK, status(result4));

                // Reservation of other car that I didn't reserve
                Reservation reservation3 = helper.createReservation(from, to, car2, user2);

                Result result5 = callAction(
                        controllers.routes.ref.Drives.cancelReservation(reservation3.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Cancelling reservation of other car that I didn't reserve", BAD_REQUEST, status(result5));

                // Reservation of my car that other user reserved
                Reservation reservation4 = helper.createReservation(from, to, car, user2);

                Result result6 = callAction(
                        controllers.routes.ref.Drives.cancelReservation(reservation4.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Cancelling reservation of my car, reserved by other user", BAD_REQUEST, status(result6));

                // Reservation of other car that I reserved AND CANCELLED
                Reservation reservation5 = helper.createReservation(from, to, car2, user);
                reservation5.setStatus(ReservationStatus.CANCELLED);
                helper.updateReservation(reservation5);

                Result result7 = callAction(
                        controllers.routes.ref.Drives.cancelReservation(reservation5.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Cancelling reservation of my car, reserved by other user but already cancelled", BAD_REQUEST, status(result7));

                // Reservation of other car that I reserved AND ALREADY REFUSED
                Reservation reservation6 = helper.createReservation(from, to, car2, user);
                reservation6.setStatus(ReservationStatus.REFUSED);
                helper.updateReservation(reservation6);

                Result result8 = callAction(
                        controllers.routes.ref.Drives.cancelReservation(reservation6.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Cancelling reservation of my car, reserved by other user but already refused", BAD_REQUEST, status(result8));

                // Reservation of other car that I reserved AND ALREADY ACCEPTED
                Reservation reservation7 = helper.createReservation(from, to, car2, user);
                reservation7.setStatus(ReservationStatus.ACCEPTED);
                helper.updateReservation(reservation7);

                Result result9 = callAction(
                        controllers.routes.ref.Drives.cancelReservation(reservation7.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Cancelling reservation of my car, reserved by other user but already accepted", BAD_REQUEST, status(result9));

                // Reservation of other car that I requested AGAIN AFTER PREVIOUS ACCEPTANCE
                Reservation reservation8 = helper.createReservation(from, to, car2, user);
                reservation8.setStatus(ReservationStatus.REQUEST_NEW);
                helper.updateReservation(reservation8);

                Result result10 = callAction(
                        controllers.routes.ref.Drives.cancelReservation(reservation8.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Cancelling reservation of my car, reserved by other user but already accepted and afterwards requested again", OK, status(result10));

                // Reservation that doesn't exist
                Result result3 = callAction(
                        controllers.routes.ref.Drives.cancelReservation(reservation.getId() - 1),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Cancelling unexisting reservation", BAD_REQUEST, status(result3));

                helper.logout();
            }
        });
    }
}
