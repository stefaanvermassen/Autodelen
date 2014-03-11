package database.jdbc;

import database.AddressDAO;
import database.DataAccessException;
import database.UserDAO;
import models.*;

import java.sql.*;
import java.util.EnumSet;

/**
 * Created by Cedric on 2/16/14.
 */
public class JDBCUserDAO implements UserDAO {

    private static final String[] AUTO_GENERATED_KEYS = {"user_id"};

    private static final String USER_QUERY = "SELECT user_id, user_password, user_firstname, user_lastname, user_phone, user_email, user_status, " +
            "address_id, address_city, address_zipcode, address_street, address_street_number, address_street_bus " +
            "FROM users LEFT JOIN addresses on address_id = user_address_domicile_id";

    private Connection connection;
    private PreparedStatement getUserByEmailStatement;
    private PreparedStatement getUserByIdStatement;
    private PreparedStatement createUserStatement;
    private PreparedStatement updateUserStatement;
    private PreparedStatement deleteUserStatement;
    private PreparedStatement permanentlyDeleteUserStatement;
    private PreparedStatement createVerificationStatement;
    private PreparedStatement getVerificationStatement;
    private PreparedStatement deleteVerificationStatement;

    public JDBCUserDAO(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getDeleteVerificationStatement() throws SQLException {
        if(deleteVerificationStatement == null){
            deleteVerificationStatement = connection.prepareStatement("DELETE FROM Verifications WHERE verification_user_id = ? AND verification_type = ?");
        }
        return deleteVerificationStatement;
    }

    private PreparedStatement getCreateVerificationStatement() throws SQLException {
        if(createVerificationStatement == null){
            createVerificationStatement = connection.prepareStatement("INSERT INTO Verifications(verification_ident, verification_user_id, verification_type) VALUES(UUID(),?, ?)");
        }
        return createVerificationStatement;
    }

    private PreparedStatement getGetVerificationStatement() throws SQLException {
        if(getVerificationStatement == null){
            getVerificationStatement = connection.prepareStatement("SELECT verification_ident FROM Verifications WHERE verification_user_id = ? AND verification_type = ?");
        }
        return getVerificationStatement;
    }
    
    private PreparedStatement getDeleteUserStatement() throws SQLException {
    	if(deleteUserStatement == null){
    		deleteUserStatement = connection.prepareStatement("UPDATE Users SET user_status = 'DROPPED' WHERE user_id = ?");
    	}
    	return deleteUserStatement;
    }

    private PreparedStatement getPermanentlyDeleteUserStatement() throws SQLException {
        if(permanentlyDeleteUserStatement == null){
            permanentlyDeleteUserStatement = connection.prepareStatement("DELETE FROM Users WHERE user_id = ?");
        }
        return permanentlyDeleteUserStatement;
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
    		updateUserStatement = connection.prepareStatement("UPDATE Users SET user_email=?, user_password=?, user_firstname=?, user_lastname=?, user_status=?, user_phone=?, user_cellphone=? WHERE user_id = ?");
    	}
    	return updateUserStatement;
    }

    public static User populateUser(ResultSet rs, boolean withPassword, boolean withAddress, boolean withStatus) throws SQLException {
        User user = new User(rs.getInt("user_id"), rs.getString("user_email"), rs.getString("user_firstname"), rs.getString("user_lastname"),
                withPassword ? rs.getString("user_password") : null,
                withAddress ? JDBCAddressDAO.populateAddress(rs) : null);

        if(withStatus)
            user.setStatus(Enum.valueOf(UserStatus.class,  rs.getString("user_status")));

        return user;
    }

    @Override
    public User getUser(String email) {
        try {
            PreparedStatement ps = getUserByEmailStatement();
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return populateUser(rs, true, true, true);
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
                    return populateUser(rs, true, true, true);
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

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating user.");
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new User(keys.getInt(1), email, firstName, lastName, password, null); //TODO: extra constructor
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new user.", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to commit new user transaction.", ex);
        }
    }

    @Override
    public String getVerificationString(User user, VerificationType type) throws DataAccessException {
        try {

            PreparedStatement ps = getGetVerificationStatement();
            ps.setInt(1, user.getId());
            ps.setString(2, type.name());
            try(ResultSet rs = ps.executeQuery()){
                if(!rs.next())
                    return null;
                else return rs.getString("verification_ident");
            } catch(SQLException ex){
                throw new DataAccessException("Failed to read verification resultset.", ex);
            }
        } catch(SQLException ex){
            throw new DataAccessException("Failed to get verification string.", ex);
        }
    }

    @Override
    public String createVerificationString(User user, VerificationType type) throws DataAccessException {
        try {
            PreparedStatement ps = getCreateVerificationStatement();
            ps.setInt(1, user.getId());
            ps.setString(2, type.name());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Verification string creation failed. Zero rows affected");

            return getVerificationString(user, type); //TODO: this might throw an exception about 2 open connections?

        } catch(SQLException ex){
            throw new DataAccessException("Failed to create verification string.", ex);
        }
    }

    @Override
    public void deleteVerificationString(User user, VerificationType type) throws DataAccessException {
        try {
            PreparedStatement ps = getDeleteVerificationStatement();
            ps.setInt(1, user.getId());
            ps.setString(2, type.name());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Verification delete operation affected 0 rows.");

        } catch(SQLException ex){
            throw new DataAccessException("Failed to delete verification.", ex);
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
            ps.setString(5, user.getStatus().name());
            if(user.getPhone()==null) ps.setNull(6, Types.VARCHAR);
            else ps.setString(6, user.getPhone());
            if(user.getCellphone()==null) ps.setNull(7, Types.VARCHAR);
            else ps.setString(7, user.getCellphone());
            ps.setInt(8, user.getId());

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("User update affected 0 rows.");

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update user", ex);
        }
    }

	@Override
	public void deleteUser(User user) throws DataAccessException {
		try {
			PreparedStatement ps = getDeleteUserStatement();
			ps.setInt(1, user.getId());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when deleting (=updating to DROPPED) user.");
		} catch (SQLException ex){
			throw new DataAccessException("Could not delete user",ex);
		}
		
	}

    @Override
    public void permanentlyDeleteUser(User user) throws DataAccessException {
        try {
            PreparedStatement ps = getPermanentlyDeleteUserStatement();
            ps.setInt(1, user.getId());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when permanently deleting user.");
        } catch (SQLException ex){
            throw new DataAccessException("Could not permanently delete user",ex);
        }
    }
}
