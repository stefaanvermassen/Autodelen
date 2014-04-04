package controllers;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.POST;
import static play.test.Helpers.callAction;
import static play.test.Helpers.cookie;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.mindrot.jbcrypt.BCrypt;

import database.AddressDAO;
import database.DataAccessContext;
import database.DataAccessProvider;
import database.DatabaseHelper;
import database.InfoSessionDAO;
import database.UserDAO;
import database.UserRoleDAO;
import database.mocking.TestDataAccessProvider;
import models.Address;
import models.InfoSession;
import models.InfoSessionType;
import models.User;
import models.UserRole;
import models.UserStatus;
import play.mvc.Http.Cookie;
import play.mvc.Result;

public class TestHelper {
	
	private UserDAO userDAO;
	private UserRoleDAO userRoleDAO;
	private AddressDAO addressDAO;
	private DataAccessProvider provider;
	
	public TestHelper(){
		provider = new TestDataAccessProvider();
		DatabaseHelper.setDataAccessProvider(provider);
		DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext();
		userDAO = context.getUserDAO();
		userRoleDAO = context.getUserRoleDAO();
		addressDAO = context.getAddressDAO();
	}
	
	public void setTestProvider(){
		if(provider==null){
			provider = new TestDataAccessProvider();
		}
		DatabaseHelper.setDataAccessProvider(provider);
	}
	
	private static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }
	
	public User createRegisteredUser(String email, String password, String firstName, String lastName, UserRole[] roles){
        User user = userDAO.createUser(email, hashPassword(password), firstName, lastName);
        user.setStatus(UserStatus.FULL);
        for(UserRole role : roles){
        	userRoleDAO.addUserRole(user.getId(), role);
        }        
        userDAO.updateUser(user, true);
        return userDAO.getUser(user.getId(), false);
    }
	
	public InfoSession createInfoSession(InfoSessionType type, User host, Address address, DateTime time, int max){
		InfoSessionDAO dao = DatabaseHelper.getDataAccessProvider().getDataAccessContext().getInfoSessionDAO();
		InfoSession session = dao.createInfoSession(InfoSessionType.NORMAL, host, address, new DateTime(),100);
		return session;
	}
	
	public Address createAddress(String country, String zip, String city, String street, String number, String bus){
		return addressDAO.createAddress(country, zip, city, street, number, bus);
	}
	
	public Cookie login(User user, String password){
		Map<String,String> data = new HashMap<>();
        data.put("email", user.getEmail());
        data.put("password", password);

        // inloggen
        Result result = callAction(
                controllers.routes.ref.Login.authenticate("/"),
                fakeRequest(POST, "/login").withFormUrlEncodedBody(data)
        );
        assertEquals("Valid login", 303, status(result));
        
        return cookie("PLAY_SESSION", result);
	}
	
	public void logout(){
		// uitloggen
		Result result3 = callAction(
                controllers.routes.ref.Login.logout(),
                fakeRequest()
        );
        assertEquals("Valid logout", 303, status(result3));
	}

}
