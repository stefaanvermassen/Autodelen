package database.jdbc;

import database.AddressDAO;
import database.DataAccessException;
import models.Address;
import models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Cedric on 2/21/14.
 */
public class JDBCAddressDAO implements AddressDAO {

    private Connection connection;
    private PreparedStatement getAddressStatement;
    private PreparedStatement createAddressStatement;

    private static final String[] AUTO_GENERATED_KEYS = {"address_id"};

    private PreparedStatement getGetAddressStatement() throws SQLException {
        if (getAddressStatement == null) {
            getAddressStatement = connection.prepareStatement("SELECT address_city, address_zipcode, address_street, address_street_number, address_street_bus FROM addresses WHERE address_id = ?");
        }
        return getAddressStatement;
    }

    private PreparedStatement getCreateAddressStatement() throws SQLException {
        if (createAddressStatement == null) {
            createAddressStatement = connection.prepareStatement("INSERT INTO addresses(address_city, address_zipcode, address_street, address_street_number, address_street_bus) VALUES (?,?,?,?,?)", AUTO_GENERATED_KEYS);
        }
        return createAddressStatement;
    }

    public JDBCAddressDAO(Connection connection){
        this.connection = connection;
    }

    @Override
    public Address getAddress(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getGetAddressStatement();
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    return new Address(id, rs.getString("address_zipcode"), rs.getString("address_city"), rs.getString("address_street"), rs.getString("address_street_number"), rs.getString("address_street_bus"));
                } else return null;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading address resultset", ex);
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch address by id.", ex);
        }
    }

    @Override
    public Address createAddress(String zip, String city, String street, String number, String bus) throws DataAccessException {
        try {
            PreparedStatement ps = getCreateAddressStatement();
            ps.setString(1, city);
            ps.setString(2, zip);
            ps.setString(3, street);
            ps.setString(4, number);
            ps.setString(5, bus);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new Address(keys.getInt(1), zip, city, street, number, bus);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new address.", ex);
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create address.", ex);
        }
    }
}
