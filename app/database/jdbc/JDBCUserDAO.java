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

    private Connection connection;
    private PreparedStatement getUserByEmailStatement;
    private PreparedStatement createUserStatement;
    private PreparedStatement getUserRoles;
    private PreparedStatement updateUserStatement;
    private PreparedStatement insertUserRolesStatement;
    private PreparedStatement deleteUserRolesStatement;

    public JDBCUserDAO(Connection connection) {
        this.connection = connection;
    }
    
    private PreparedStatement deleteUserRolesStatement() throws SQLException{
    	if(deleteUserRolesStatement == null){
    		deleteUserRolesStatement = connection.prepareStatement("DELETE FROM UserRoles WHERE userrole_userid=? AND userrole_role=?");    		
    	}
    	return deleteUserRolesStatement;
    }
    
    private PreparedStatement insertUserRolesStatement() throws SQLException{
    	if(insertUserRolesStatement == null){
    		insertUserRolesStatement = connection.prepareStatement("IF NOT EXISTS (SELECT * FROM UserRoles WHERE userrole_userid"
    				+ "=? AND userrole_role=?) INSERT INTO UserRoles(userrole_userid, userrole_role) VALUES (?,?) ");    		
    	}
    	return insertUserRolesStatement;
    }

    private PreparedStatement getUserRoles() throws SQLException {
    	if(getUserRoles == null){
    		getUserRoles = connection.prepareStatement("SELECT userrole_role FROM UserRoles INNER JOIN Users ON "
    				+ "userrole_userid = user_id WHERE user_id = ?");
    	}
    	return getUserRoles;
    }
    
    private PreparedStatement getUserByEmailStatement() throws SQLException {
        if (getUserByEmailStatement == null) {
            getUserByEmailStatement = connection.prepareStatement("SELECT user_id, user_password, user_firstname, user_lastname, user_phone, user_email, " +
                    "address_id, address_city, address_zipcode, address_street, address_street_number, address_street_bus " +
                    "FROM Users INNER JOIN Addresses on address_id = user_address_domicile_id WHERE user_email = ?;");
        }
        return getUserByEmailStatement;
    }

    private PreparedStatement getCreateUserStatement() throws SQLException {
        if (createUserStatement == null) {
            createUserStatement = connection.prepareStatement("INSERT INTO Users(user_email, user_password, user_firstname, user_lastname, user_phone, user_address_domicile_id) VALUES (?,?,?,?,?,?)", AUTO_GENERATED_KEYS);
        }
        return createUserStatement;
    }
    
    private PreparedStatement getUpdateUserStatement() throws SQLException {
    	if (updateUserStatement == null){
    		updateUserStatement = connection.prepareStatement("UPDATE Users SET user_email=?, user_password=?, user_firstname=?, user_lastname=?, user_phone=?, user_address_domicile_id=? ");
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
                rs.next();
                // TODO: if Address is null second argument of populateUser should be false
                User user = populateUser(rs, true, true);
                PreparedStatement rolesStmt = getUserRoles();
                rolesStmt.setInt(1, user.getId());
                try (ResultSet roleSet = rolesStmt.executeQuery()) {
                	while(roleSet.next()){
                		user.addRole(UserRole.valueOf(rs.getString("userrole_role")));
                	}
                return user;	
                } catch (SQLException ex){
                	throw new DataAccessException("Error reading UserRoles", ex);
                }
                
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading user resultset", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch user by email.", ex);
        }

    }

    @Override
    public User createUser(String email, String password, String firstName, String lastName, String phone, Address address) throws DataAccessException {
        try (PreparedStatement ps = getCreateUserStatement()) {
            ps.setString(1, email);
            ps.setString(2, password);
            ps.setString(3, firstName);
            ps.setString(4, lastName);
            ps.setString(5, phone);

            if (address != null) {
                ps.setInt(6, address.getId());
            } else ps.setNull(6, Types.INTEGER);

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new User(keys.getInt(1), email, firstName, lastName, password, address);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new user.", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to commit new user transaction.", ex);
        }
    }
    
    @Override
    public User createUser(String email, String password, String firstName, String lastName, String phone, Address address, EnumSet<UserRole> roles) throws DataAccessException {
        try (PreparedStatement ps = getCreateUserStatement() ; PreparedStatement insert = insertUserRolesStatement()) {
            ps.setString(1, email);
            ps.setString(2, password);
            ps.setString(3, firstName);
            ps.setString(4, lastName);
            ps.setString(5, phone);

            if (address != null) {
                ps.setInt(6, address.getId());
            } else ps.setNull(6, Types.INTEGER);

            ps.executeUpdate();
            User user = new User(0, email, firstName, lastName, password, address);
            for(UserRole role : roles){
    			insert.setString(2, role.toString());
    			insert.executeUpdate();
    			user.addRole(role);
        	}
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                user.setId(keys.getInt(1));
                return user;
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new user.", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to commit new user transaction.", ex);
        }
    }

    @Override
    public void updateUser(User user) throws DataAccessException {
    	try (PreparedStatement ps = getUpdateUserStatement() ; PreparedStatement insert = insertUserRolesStatement() ; PreparedStatement delete = deleteUserRolesStatement()) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFirstName());
            ps.setString(4, user.getLastName());
            ps.setString(5, user.getPhone());

            if (user.getAddress() != null) {
                ps.setInt(6, user.getAddress().getId());
            } else ps.setNull(6, Types.INTEGER);

            ps.executeUpdate();
            
            insert.setInt(1, user.getId());
        	delete.setInt(1, user.getId());
        	for(UserRole role : UserRole.values()){
        		if(user.gotRole(role)){
        			insert.setString(2, role.toString());
        			insert.executeUpdate();
        		} else {
        			delete.setString(2, role.toString());
        		}
        	}
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update user", ex);
        }
    }
}
