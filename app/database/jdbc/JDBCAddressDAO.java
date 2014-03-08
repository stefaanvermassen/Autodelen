package database.jdbc;

import database.AddressDAO;
import database.DataAccessException;
import models.Address;
import models.User;

import java.sql.*;

/**
 * Created by Cedric on 2/21/14.
 */
public class JDBCAddressDAO implements AddressDAO {

    private Connection connection;
    private PreparedStatement getAddressStatement;
    private PreparedStatement createAddressStatement;
	
    private PreparedStatement existsAddressStatement;
    private PreparedStatement deleteAddressStatement;
    private PreparedStatement updateAddressStatement;

    private static final String[] AUTO_GENERATED_KEYS = {"address_id"};

    private PreparedStatement getGetAddressStatement() throws SQLException {
        if (getAddressStatement == null) {
            getAddressStatement = connection.prepareStatement("SELECT address_id, address_city, address_zipcode, address_street, address_street_number, address_street_bus FROM addresses WHERE address_id = ?");
        }
        return getAddressStatement;
    }

    private PreparedStatement getUpdateAddressStatement() throws SQLException {
        if(updateAddressStatement == null){
            updateAddressStatement = connection.prepareStatement("UPDATE addresses SET address_city = ?, address_zipcode = ?, address_street = ?, address_street_number = ?, address_street_bus = ? WHERE address_id = ?");
        }
        return updateAddressStatement;
    }
    private PreparedStatement getDeleteAddressStatement() throws SQLException {
        if(deleteAddressStatement == null){
            deleteAddressStatement = connection.prepareStatement("DELETE FROM addresses WHERE address_id = ?");
        }
        return deleteAddressStatement;
    }

    private PreparedStatement getCreateAddressStatement() throws SQLException {
        if (createAddressStatement == null) {
            createAddressStatement = connection.prepareStatement("INSERT INTO addresses(address_city, address_zipcode, address_street, address_street_number, address_street_bus) VALUES (?,?,?,?,?)", AUTO_GENERATED_KEYS);
        }
        return createAddressStatement;
    }

    private PreparedStatement getExistsAddressStatement() throws SQLException {
        if (existsAddressStatement == null) {
            existsAddressStatement = connection.prepareStatement("SELECT address_id FROM addresses WHERE address_city=? AND address_zipcode=? AND address_street=? AND address_street_number=? AND address_street_bus=?");
        }
        return existsAddressStatement;
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
                    return populateAddress(rs);
                } else return null;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading address resultset", ex);
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch address by id.", ex);
        }
    }

    public static Address populateAddress(ResultSet rs) throws SQLException {
        if(rs.getObject("address_id") == null)
            return null;
        else
            return new Address(rs.getInt("address_id"), rs.getString("address_zipcode"), rs.getString("address_city"), rs.getString("address_street"), rs.getString("address_street_number"), rs.getString("address_street_bus"));
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
                    connection.commit();
                    return new Address(keys.getInt(1), zip, city, street, number, bus);
                } catch (SQLException ex) {
                    throw new DataAccessException("Failed to get primary key for new address.", ex);
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create address.", ex);
        }
    }


    /*
     * Returns  -1          if Address doesn't exist in database yet.
     *          address_id  if it does.
     */
    @Override
    public int existsAddress(Address address) throws DataAccessException {
        try {
            PreparedStatement ps = getExistsAddressStatement();
			ps.setString(1, address.getCity());
            ps.setString(2, address.getZip());
            ps.setString(3, address.getStreet());
            ps.setString(4, address.getNumber());
            ps.setString(5, address.getBus());
			
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    return rs.getInt("address_id");
                } else {
                    return -1;
                }
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading address resultset", ex);
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch address.", ex);
        }
    }
	
    @Override
    public void deleteAddress(Address address) throws DataAccessException {
        if(address.getId() == 0)
            throw new DataAccessException("Cannot delete address that doesn't have an ID");

        try {
            PreparedStatement ps = getDeleteAddressStatement();
            ps.setInt(1, address.getId());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when deleting address with ID=" + address.getId());
            connection.commit();
        } catch(SQLException ex){
            throw new DataAccessException("Failed to execute address deletion query.", ex);
        }
    }

    @Override
    public void updateAddress(Address address) throws DataAccessException {
        try {
            PreparedStatement ps = getUpdateAddressStatement();
            ps.setString(1, address.getCity());
            ps.setString(2, address.getZip());
            ps.setString(3, address.getStreet());
            ps.setString(4, address.getNumber());
            ps.setString(5, address.getBus());
            ps.setInt(6, address.getId());
			
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Address update affected 0 rows.");
            connection.commit();
        } catch(SQLException ex) {
            throw new DataAccessException("Failed to update address.", ex);
        }

    }
}
