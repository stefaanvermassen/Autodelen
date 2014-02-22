package database.jdbc;

import database.AddressDAO;
import database.DataAccessException;
import database.UserDAO;
import models.Address;
import models.User;
import models.UserRole;

import java.sql.*;

/**
 * Created by Cedric on 2/16/14.
 */
public class JDBCUserDAO implements UserDAO {

    private static final String[] AUTO_GENERATED_KEYS = {"user_id"};

    private Connection connection;
    private PreparedStatement getUserByEmailStatement;
    private PreparedStatement createUserStatement;

    public JDBCUserDAO(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getUserByEmailStatement() throws SQLException {
        if (getUserByEmailStatement == null) {
            getUserByEmailStatement = connection.prepareStatement("SELECT user_id, user_password, user_firstname, user_lastname, user_phone, user_email "+
                    "address_id, address_city, address_zipcode, address_street, address_street_number, address_street_bus " +
                    "FROM users INNER JOIN addresses on address_id = user_address_domicile_id WHERE user_email = ?;");
        }
        return getUserByEmailStatement;
    }

    private PreparedStatement getCreateUserStatement() throws SQLException {
        if (createUserStatement == null) {
            createUserStatement = connection.prepareStatement("INSERT INTO users(user_email, user_password, user_firstname, user_lastname, user_phone, user_address_domicile_id) VALUES (?,?,?,?,?,?)", AUTO_GENERATED_KEYS);
        }
        return createUserStatement;
    }

    public static User populateUser(ResultSet rs, boolean withPassword, boolean withAddress) throws SQLException {
        return new User(rs.getInt("user_id"), rs.getString("user_email"), rs.getString("user_firstname"), rs.getString("user_lastname"),
                withPassword ? rs.getString("user_password") : null,
                withAddress ? JDBCAddressDAO.populateAddress(rs) : null); //TODO: handle null address
    }

    @Override
    public User getUser(String email) {
        try {
            PreparedStatement ps = getUserByEmailStatement();
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return populateUser(rs, true, true);
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading user resultset", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch user by email.", ex);
        }

    }

    @Override
    public User createUser(String email, String password, String firstName, String lastName, String phone, Address address) throws DataAccessException {
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = getCreateUserStatement()) {
                ps.setString(1, email);
                ps.setString(2, password);
                ps.setString(3, firstName);
                ps.setString(4, lastName);
                ps.setString(5, phone);

                //TODO: move this logic to controller (which will be responsible for rollback) and pass Address object to createUser
                if (address != null) { // Create a new address object
                    AddressDAO adao = new JDBCAddressDAO(connection);
                    address = adao.createAddress(address.getZip(), address.getCity(), address.getStreet(), address.getNumber(), address.getBus()); //TODO: pass fields, or address object to this function?
                    ps.setInt(6, address.getId());
                } else ps.setNull(6, Types.INTEGER);

                ps.executeUpdate();
                connection.commit();
                connection.setAutoCommit(true);

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next(); //if this fails we want an exception anyway

                    return new User(keys.getInt(1), email, firstName, lastName, password, address);
                } catch (SQLException ex) {
                    throw new DataAccessException("Failed to get primary key for new user.", ex);
                }
            } catch (SQLException ex) {
                connection.rollback();
                connection.setAutoCommit(true);
                throw new DataAccessException("Failed to commit new user transaction.", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create user.", ex);
        }
    }

    @Override
    public void updateUser(User user) throws DataAccessException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
