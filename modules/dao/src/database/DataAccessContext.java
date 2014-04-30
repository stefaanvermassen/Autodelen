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

    public CarCostDAO getCarCostDAO();

    public RefuelDAO getRefuelDAO();

    public SchedulerDAO getSchedulerDAO();
    
    public ReservationDAO getReservationDAO();
    
    public UserRoleDAO getUserRoleDAO();

    public CarRideDAO getCarRideDAO();

    public ApprovalDAO getApprovalDAO();

    public FileDAO getFileDAO();

    public SettingDAO getSettingDAO();

    public void begin();

    public void commit();

    public void rollback();

    public void close();
}