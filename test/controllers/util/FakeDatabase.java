package controllers.util;

import database.CarDAO;
import database.DatabaseHelper;
import database.UserDAO;
import database.UserRoleDAO;
import models.*;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Created by HannesM on 4/04/14.
 */
public class FakeDatabase {

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public static User createRegisteredUser(String email, String password, String firstName, String lastName, UserStatus status){
        UserDAO dao = DatabaseHelper.getDataAccessProvider().getDataAccessContext().getUserDAO();
        User user = dao.createUser(email, hashPassword(password), firstName, lastName);
        user.setStatus(status);
        dao.updateUser(user, true);
        return dao.getUser(user.getId(), false);
    }

    public static void addUserRole(User user, UserRole role) {
        UserRoleDAO dao = DatabaseHelper.getDataAccessProvider().getDataAccessContext().getUserRoleDAO();
        dao.addUserRole(user.getId(), role);
    }

    public static void removeUserRole(User user, UserRole role) {
        UserRoleDAO dao = DatabaseHelper.getDataAccessProvider().getDataAccessContext().getUserRoleDAO();
        dao.removeUserRole(user.getId(), role);
    }

    public static Car createCar(String name, String brand, String type,
                                Address location, int seats, int doors, int year, boolean gps,
                                boolean hook, CarFuel fuel, int fuelEconomy, int estimatedValue,
                                int ownerAnnualKm, User owner, String comments) {
        CarDAO dao = DatabaseHelper.getDataAccessProvider().getDataAccessContext().getCarDAO();
        Car car = dao.createCar(name, brand, type, location, seats, doors, year, gps, hook, fuel, fuelEconomy, estimatedValue, ownerAnnualKm, owner, comments);
        return car;
    }
}
