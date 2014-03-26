package database;

/**
 * Created by Cedric on 2/16/14.
 */
public interface DataAccessContext extends AutoCloseable {
    public UserDAO getUserDAO();

    public InfoSessionDAO getInfoSessionDAO();

    public TemplateDAO getTemplateDAO();

    public NotificationDAO getNotificationDAO();

    public MessageDAO getMessageDAO();

    public AddressDAO getAddressDAO();
    
    public CarDAO getCarDAO();
    
    public ReservationDAO getReservationDAO();
    
    public UserRoleDAO getUserRoleDAO();

    public CarRideDAO getCarRideDAO();

    public void begin();

    public void commit();

    public void rollback();

    public void close();
}