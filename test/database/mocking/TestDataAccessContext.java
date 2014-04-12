package database.mocking;

import database.*;

/**
 * Created by Cedric on 3/7/14.
 */
public class TestDataAccessContext implements DataAccessContext {

    private UserDAO userDao;
    private InfoSessionDAO infoSessionDAO;
    private UserRoleDAO userRoleDao;
    private ReservationDAO reservationDAO;
    private CarDAO carDAO;
    private TemplateDAO templateDAO;
    private AddressDAO addressDAO;
    private NotificationDAO notificationDAO;
    private MessageDAO messageDAO;
    private CarRideDAO carRidesDAO;

    public TestDataAccessContext(){
        userDao = new TestUserDAO();
        infoSessionDAO = new TestInfoSessionDAO();
        userRoleDao = new TestUserRoleDAO(userDao);
        reservationDAO = new TestReservationDAO();
        carDAO = new TestCarDAO();
        templateDAO = new TestTemplateDAO();
        addressDAO = new TestAddressDAO();
        notificationDAO = new TestNotificationDAO();
        messageDAO = new TestMessageDAO();
        carRidesDAO = new TestCarRidesDAO();
    }

    @Override
    public UserDAO getUserDAO() {
        return userDao;
    }

    @Override
    public InfoSessionDAO getInfoSessionDAO() {
        return infoSessionDAO;
    }

    @Override
    public TemplateDAO getTemplateDAO() {
        return templateDAO;
    }

    @Override
    public AddressDAO getAddressDAO() {
        return addressDAO;
    }

    @Override
    public CarDAO getCarDAO() {
        return carDAO;
    }

    @Override
    public ReservationDAO getReservationDAO() {
        return reservationDAO;
    }

    @Override
    public UserRoleDAO getUserRoleDAO() {
        return userRoleDao;
    }

    @Override
    public CarRideDAO getCarRideDAO() {
        return carRidesDAO;
    }

    @Override
    public ApprovalDAO getApprovalDAO() {
        return null;
    }

    @Override
    public FileDAO getFileDAO() {
        return null;
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

	@Override
	public NotificationDAO getNotificationDAO() {
		return notificationDAO;
	}

	@Override
	public MessageDAO getMessageDAO() {
		return messageDAO;
	}
}
