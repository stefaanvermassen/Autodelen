package database.jdbc;

import database.DataAccessException;
import database.UserDAO;
import models.*;

import java.sql.*;

/**
 * Created by Cedric on 2/16/14.
 */
public class JDBCUserDAO implements UserDAO {

    private static final String[] AUTO_GENERATED_KEYS = {"user_id"};

    private static final String SMALL_USER_FIELDS = "users.user_id, users.user_password, users.user_firstname, users.user_lastname, users.user_email";

    private static final String SMALL_USER_QUERY = "SELECT " + SMALL_USER_FIELDS + " FROM Users";

    private static final String USER_FIELDS = SMALL_USER_FIELDS + ", users.user_cellphone, users.user_phone, users.user_status, users.user_gender, " +
            "domicileAddresses.address_id, domicileAddresses.address_country, domicileAddresses.address_city, domicileAddresses.address_zipcode, domicileAddresses.address_street, domicileAddresses.address_street_number, domicileAddresses.address_street_bus, " +
            "residenceAddresses.address_id, residenceAddresses.address_country, residenceAddresses.address_city, residenceAddresses.address_zipcode, residenceAddresses.address_street, residenceAddresses.address_street_number, residenceAddresses.address_street_bus, " +
            "users.user_damage_history, users.user_payed_deposit, users.user_agree_terms, users.user_contract_manager_id, " +
            "contractManagers.user_id, contractManagers.user_password, contractManagers.user_firstname, contractManagers.user_lastname, contractManagers.user_email";

    private static final String USER_QUERY = "SELECT " + USER_FIELDS + " FROM Users " +
            "LEFT JOIN addresses as domicileAddresses on domicileAddresses.address_id = user_address_domicile_id " +
            "LEFT JOIN addresses as residenceAddresses on residenceAddresses.address_id = user_address_residence_id " +
            "LEFT JOIN users as contractManagers on contractManagers.user_id = users.user_contract_manager_id";

    private Connection connection;
    private PreparedStatement getUserByEmailStatement;
    private PreparedStatement smallGetUserByIdStatement;
    private PreparedStatement getUserByIdStatement;
    private PreparedStatement createUserStatement;
    private PreparedStatement smallUpdateUserStatement;
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
            getUserByEmailStatement = connection.prepareStatement(USER_QUERY + " WHERE users.user_email = ?");
        }
        return getUserByEmailStatement;
    }

    private PreparedStatement getSmallGetUserByIdStatement() throws SQLException {
        if(smallGetUserByIdStatement == null){
            smallGetUserByIdStatement = connection.prepareStatement(SMALL_USER_QUERY + " WHERE user_id = ?");
        }
        return smallGetUserByIdStatement;
    }

    private PreparedStatement getGetUserByIdStatement() throws SQLException {
        if(getUserByIdStatement == null){
            getUserByIdStatement = connection.prepareStatement(USER_QUERY + " WHERE users.user_id = ?");
        }
        return getUserByIdStatement;
    }

    private PreparedStatement getCreateUserStatement() throws SQLException {
        if (createUserStatement == null) {
            createUserStatement = connection.prepareStatement("INSERT INTO users(user_email, user_password, user_firstname, user_lastname) VALUES (?,?,?,?)", AUTO_GENERATED_KEYS);
        }
        return createUserStatement;
    }

    private PreparedStatement getSmallUpdateUserStatement() throws SQLException {
        if (smallUpdateUserStatement == null){
            smallUpdateUserStatement = connection.prepareStatement("UPDATE Users SET user_email=?, user_password=?, user_firstname=?, user_lastname=? WHERE user_id = ?");
        }
        return smallUpdateUserStatement;
    }

    private PreparedStatement getUpdateUserStatement() throws SQLException {
    	if (updateUserStatement == null){
    		updateUserStatement = connection.prepareStatement("UPDATE Users SET user_email=?, user_password=?, user_firstname=?, user_lastname=?, user_status=?, user_gender=?, user_phone=?, user_cellphone=?, user_address_domicile_id=?, user_address_residence_id=?, user_damage_history=?, user_payed_deposit=?, user_agree_terms=?, user_contract_manager_id=? WHERE user_id = ?");
    	}
    	return updateUserStatement;
    }

    public static User populateUser(ResultSet rs, boolean withPassword, boolean withRest) throws SQLException {
        return populateUser(rs, withPassword, withRest, "users");
    }

    public static User populateUser(ResultSet rs, boolean withPassword, boolean withRest, String tableName) throws SQLException {
        User user = new User(rs.getInt(tableName + ".user_id"), rs.getString(tableName + ".user_email"), rs.getString(tableName + ".user_firstname"), rs.getString(tableName + ".user_lastname"),
                withPassword ? rs.getString(tableName + ".user_password") : null);

        if(withRest) {
            user.setAddressDomicile(JDBCAddressDAO.populateAddress(rs, "domicileAddresses"));
            user.setAddressResidence(JDBCAddressDAO.populateAddress(rs, "residenceAddresses"));
            user.setCellphone(rs.getString(tableName + ".user_cellphone"));
            user.setPhone(rs.getString(tableName + ".user_phone"));
            user.setGender(UserGender.valueOf(rs.getString(tableName + ".user_gender")));
            user.setDamageHistory(rs.getString(tableName + ".user_damage_history"));
            user.setPayedDeposit(rs.getBoolean(tableName + ".user_payed_deposit"));
            user.setAgreeTerms(rs.getBoolean(tableName + ".user_agree_terms"));
            user.setContractManager(JDBCUserDAO.populateUser(rs, false, false, "contractManagers"));
            user.setStatus(Enum.valueOf(UserStatus.class, rs.getString(tableName + ".user_status")));
            // TODO: driver license, identity card, image
        }

        return user;
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

    @Override
    public User getUser(int userId, boolean withRest) throws DataAccessException {
        try {
            PreparedStatement ps;
            if(withRest) {
                ps = getGetUserByIdStatement();
            } else {
                ps = getSmallGetUserByIdStatement();
            }

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    return populateUser(rs, true, withRest);
                }
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
                return new User(keys.getInt(1), email, firstName, lastName, password); //TODO: extra constructor
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new user.", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to commit new user transaction.", ex);
        }
    }

    @Override
    public void updateUser(User user, boolean withRest) throws DataAccessException {
        try {
            PreparedStatement ps;
            if(withRest) {
                ps = getUpdateUserStatement();
            } else {
                ps = getSmallUpdateUserStatement();
            }
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFirstName());
            ps.setString(4, user.getLastName());

            if(!withRest) {
                ps.setInt(5, user.getId());
            } else {
                ps.setString(5, user.getStatus().name());
                ps.setString(6, user.getGender().name());
                if(user.getPhone()==null) ps.setNull(7, Types.VARCHAR);
                else ps.setString(7, user.getPhone());
                if(user.getCellphone()==null) ps.setNull(8, Types.VARCHAR);
                else ps.setString(8, user.getCellphone());
                if(user.getAddressDomicile() == null) ps.setNull(9, Types.INTEGER);
                else ps.setInt(9, user.getAddressDomicile().getId());
                if(user.getAddressResidence() == null) ps.setNull(10, Types.INTEGER);
                else ps.setInt(10, user.getAddressResidence().getId());
                if(user.getDamageHistory()==null) ps.setNull(11, Types.VARCHAR);
                else ps.setString(11, user.getDamageHistory());
                ps.setBoolean(12, user.isPayedDeposit());
                ps.setBoolean(13, user.isAgreeTerms());
                if(user.getContractManager()==null) ps.setNull(14, Types.INTEGER);
                else ps.setInt(14, user.getContractManager().getId());
                ps.setInt(15, user.getId());

                // TODO: driver license, identity card, image
            }

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
}
