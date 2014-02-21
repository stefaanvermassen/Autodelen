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
            getUserByEmailStatement = connection.prepareStatement("SELECT user_id, user_password, user_firstname, user_lastname, user_phone, user_address_domicile_id FROM users WHERE user_email = ?");
        }
        return getUserByEmailStatement;
    }

    private PreparedStatement getCreateUserStatement() throws SQLException {
        if (createUserStatement == null) {
            createUserStatement = connection.prepareStatement("INSERT INTO users(user_email, user_password, user_firstname, user_lastname, user_phone, user_address_domicile_id) VALUES (?,?,?,?,?,?)", AUTO_GENERATED_KEYS);
        }
        return createUserStatement;
    }

    @Override
    public User getUser(String email) {
        try {
            PreparedStatement ps = getUserByEmailStatement();
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();

                User user = new User(email);
                user.setId(rs.getInt("user_id"));
                user.setFirstName(rs.getString("user_firstname"));
                user.setLastName(rs.getString("user_lastname"));
                user.setPassword(rs.getString("user_password"));
                user.setPhone(rs.getString("user_phone"));
                Object address_id = rs.getObject("user_address_domicile_id");

                //TODO: user clean inner join on address instead of using the seperate DAO through helper function!
                Address address = null;
                if (address_id != null) {
                    AddressDAO adao = new JDBCAddressDAO(connection);
                    address = adao.getAddress(((Long) address_id).intValue()); //TODO: fix PK's signed
                }
                user.setAddress(address);

                //TODO: drivers license etc

                return user;
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
}
