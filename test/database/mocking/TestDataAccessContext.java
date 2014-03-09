package database.mocking;

import database.*;

/**
 * Created by Cedric on 3/7/14.
 */
public class TestDataAccessContext implements DataAccessContext {

    private TestUserDAO userDao;
    private TestUserRoleDAO roleDao;

    public TestDataAccessContext(){
        userDao = new TestUserDAO();
        roleDao = new TestUserRoleDAO(userDao);
    }

    @Override
    public UserDAO getUserDAO() {
        return userDao;
    }

    @Override
    public InfoSessionDAO getInfoSessionDAO() {
        return null;
    }

    @Override
    public TemplateDAO getTemplateDAO() {
        return null;
    }

    @Override
    public AddressDAO getAddressDAO() {
        return null;
    }

    @Override
    public CarDAO getCarDAO() {
        return null;
    }

    @Override
    public ReservationDAO getReservationDAO() {
        return null;
    }

    @Override
    public UserRoleDAO getUserRoleDAO() {
        return roleDao;
    }

    @Override
    public void begin() {

    }

    @Override
    public void commit() {

    }

    @Override
    public void rollback() {
        throw new RuntimeException("Rollback is not supported");
    }

    @Override
    public void close() {

    }
}
