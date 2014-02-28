package database;

/**
 * Created by Cedric on 2/16/14.
 */
public interface DataAccessContext extends AutoCloseable {
    public UserDAO getUserDAO();

    public InfoSessionDAO getInfoSessionDAO();

    public AddressDAO getAddressDAO();
    
    public CarDAO getCarDAO();
    
    public ReservationDAO getReservationDAO();

    public void begin();

    public void commit();

    public void rollback();

    public void close();
}