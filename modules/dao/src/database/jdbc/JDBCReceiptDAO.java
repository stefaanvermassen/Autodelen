package database.jdbc;

import database.*;
import models.Receipt;
import models.User;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JDBCReceiptDAO implements ReceiptDAO {

    private Connection connection;
    public JDBCReceiptDAO(Connection connection) {
        this.connection = connection;
    }

    // TODO: more fields to filter on
    public static final String FILTER_FRAGMENT = " WHERE Users.user_firstname LIKE ? AND Users.user_lastname LIKE ? " +
            "AND (CONCAT_WS(' ', users.user_firstname, users.user_lastname) LIKE ? OR CONCAT_WS(' ', users.user_lastname, users.user_firstname) LIKE ?)";

    private static final String USER_FIELDS = "users.user_id, users.user_password, users.user_firstname, users.user_lastname, users.user_email";

    private static final String USER_QUERY = "SELECT " + USER_FIELDS + " FROM Users " +
            "LEFT JOIN addresses as domicileAddresses on domicileAddresses.address_id = user_address_domicile_id " +
            "LEFT JOIN addresses as residenceAddresses on residenceAddresses.address_id = user_address_residence_id " +
            "LEFT JOIN users as contractManagers on contractManagers.user_id = users.user_contract_manager_id";

    private PreparedStatement getGetAmountOfReceiptsStatement;
    private PreparedStatement getReceiptsListStatement;


    private PreparedStatement getGetAmountOfReceiptsStatement() throws SQLException {
        if(getGetAmountOfReceiptsStatement == null) {
            getGetAmountOfReceiptsStatement = connection.prepareStatement("SELECT COUNT(receipt_id) AS amount_of_receipts FROM Receipts" + FILTER_FRAGMENT);
        }
        return getGetAmountOfReceiptsStatement;
    }

    /**
     * @param filter The filter to apply to
     * @return The amount of filtered cars
     * @throws DataAccessException
     */
    @Override
    public int getAmountOfReceipts(Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = getGetAmountOfReceiptsStatement();
            fillFragment(ps, filter, 1);

            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return rs.getInt("amount_of_receipts");
                else return 0;

            } catch (SQLException ex) {
                throw new DataAccessException("Error reading count of receipts", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of receipts", ex);
        }
    }

    public List<Receipt> getReceiptsList(FilterField orderBy, boolean asc, int page, int PAGE_SIZE, Filter filter, User user){
        return new ArrayList();
    }

    private void fillFragment(PreparedStatement ps, Filter filter, int start) throws SQLException {
        if(filter == null) {
            // getFieldContains on a "empty" filter will return the default string "%%", so this does not filter anything
            filter = new JDBCFilter();
        }

        ps.setString(start, filter.getValue(FilterField.RECEIPT_DATE));
    }
}
