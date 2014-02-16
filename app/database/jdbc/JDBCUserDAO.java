package database.jdbc;

import database.DataAccessException;
import database.UserDAO;
import models.User;
import models.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Cedric on 2/16/14.
 */
public class JDBCUserDAO implements UserDAO {

    private static final String[] AUTO_GENERATED_KEYS = {"ID"};

    private Connection connection;
    private PreparedStatement getUserByEmailStatement;
    private PreparedStatement createUserStatement;

    public JDBCUserDAO(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getUserByEmailStatement() throws SQLException {
        if (getUserByEmailStatement == null) {
            getUserByEmailStatement = connection.prepareStatement("SELECT id, email, password, firstname, lastname, role FROM users WHERE email = ?");
        }
        return getUserByEmailStatement;
    }

    private PreparedStatement getCreateUserStatement() throws SQLException {
        if (createUserStatement == null) {
            createUserStatement = connection.prepareStatement("INSERT INTO personen(email, password, firstname, lastname) VALUES (?,?,?,?)", AUTO_GENERATED_KEYS);
        }
        return createUserStatement;
    }

    @Override
    public User getUser(String email) {
        try {
            PreparedStatement ps = getUserByEmailStatement();
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                User user = new User(email);
                user.setId(rs.getInt("ID"));
                user.setFirstName(rs.getString("firstname"));
                user.setLastName(rs.getString("lastname"));
                user.setPassword(rs.getString("password"));
                user.setRole(Enum.valueOf(UserRole.class, rs.getString("role")));

                return user;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading user resultset", ex);
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch user by email.", ex);
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
                return new User(keys.getInt(1), email, firstName, lastName, password);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new user.", ex);
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create user.", ex);
        }
    }
}
