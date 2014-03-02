package database.jdbc;

import database.AddressDAO;
import database.DataAccessException;
import database.UserDAO;
import models.Address;
import models.User;
import models.UserRole;

import java.sql.*;
import java.util.EnumSet;

/**
 * Created by Cedric on 2/16/14.
 */
public class JDBCUserDAO implements UserDAO {

    private static final String[] AUTO_GENERATED_KEYS = {"user_id"};

    private static final String USER_QUERY = "SELECT user_id, user_password, user_firstname, user_lastname, user_phone, user_email, " +
            "address_id, address_city, address_zipcode, address_street, address_street_number, address_street_bus " +
            "FROM users LEFT JOIN addresses on address_id = user_address_domicile_id";

    private Connection connection;
    private PreparedStatement getUserByEmailStatement;
    private PreparedStatement getUserByIdStatement;
    private PreparedStatement createUserStatement;
    private PreparedStatement updateUserStatement;
    private PreparedStatement deleteUserStatement;

    public JDBCUserDAO(Connection connection) {
        this.connection = connection;
    }
    
    private PreparedStatement getDeleteUserStatement() throws SQLException {
    	if(deleteUserStatement == null){
    		deleteUserStatement = connection.prepareStatement("DELETE FROM Users WHERE user_id = ?");
    	}
    	return deleteUserStatement;
    }
    
    private PreparedStatement getUserByEmailStatement() throws SQLException {
        if (getUserByEmailStatement == null) {
            getUserByEmailStatement = connection.prepareStatement(USER_QUERY + " WHERE user_email = ?");
        }
        return getUserByEmailStatement;
    }

    private PreparedStatement getGetuserByIdStatement() throws SQLException {
        if(getUserByIdStatement == null){
            getUserByIdStatement = connection.prepareStatement(USER_QUERY + " WHERE user_id = ?");
        }
        return getUserByIdStatement;
    }

    private PreparedStatement getCreateUserStatement() throws SQLException {
        if (createUserStatement == null) {
            createUserStatement = connection.prepareStatement("INSERT INTO users(user_email, user_password, user_firstname, user_lastname) VALUES (?,?,?,?)", AUTO_GENERATED_KEYS);
        }
        return createUserStatement;
    }
    
    private PreparedStatement getUpdateUserStatement() throws SQLException {
    	if (updateUserStatement == null){
    		updateUserStatement = connection.prepareStatement("UPDATE Users SET user_email=?, user_password=?, user_firstname=?, user_lastname=? WHERE user_id = ?");
    	}
    	return updateUserStatement;
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
                if(rs.next())
                    return populateUser(rs, true, true);
                else return null;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading user resultset", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch user by email.", ex);
        }

    }
    public User getUser(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getGetuserByIdStatement();
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return populateUser(rs, true, true);
                else return null;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading user resultset", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch user by id.", ex);
        }

    }

    @Override
    public User createUser(String email, String password, String firstName, String lastName) throws DataAccessException {
        try {
            PreparedStatement ps = getCreateUserStatement();
            ps.setString(1, email);
            ps.setString(2, password);
            ps.setString(3, firstName);
            ps.setString(4, lastName);

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                connection.commit();
                return new User(keys.getInt(1), email, firstName, lastName, password, null); //TODO: extra constructor
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new user.", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to commit new user transaction.", ex);
        }
    }

    @Override
    public void updateUser(User user) throws DataAccessException {
    	try {
    		PreparedStatement ps = getUpdateUserStatement(); 
    		ps.setString(1, user.getEmail());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFirstName());
            ps.setString(4, user.getLastName());

            ps.setInt(5, user.getId());

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("User update affected 0 rows.");
        	connection.commit();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update user", ex);
        }
    }

	@Override
	public void deleteUser(User user) throws DataAccessException {
		try {
			PreparedStatement ps = getDeleteUserStatement();
			ps.setInt(1, user.getId());
			ps.executeUpdate();
			connection.commit();
		} catch (SQLException ex){
			throw new DataAccessException("Could not delete user",ex);
		}
		
	}
}
