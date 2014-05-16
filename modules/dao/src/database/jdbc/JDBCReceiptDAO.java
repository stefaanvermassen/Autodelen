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
    public static final String USER_FRAGMENT = 
	"Receipts.receipt_userID LIKE ?)";

    public static final String DATE_FRAGMENT1 =
        "Receipts.receipt_date BETWEEN ? AND ? ";

    public static final String DATE_FRAGMENT2 =
        "Receipts.receipt_date <= ? ";

    private static final String RECEIPT_FIELDS = "receipt_id, receipt_name, receipt_date, receipt_fileID, receipt_userID";

    private static final String RECEIPT_QUERY = "SELECT " + RECEIPT_FIELDS + " FROM Receipts " +
            "LEFT JOIN files as receipt_file on domicileAddresses.address_id = user_address_domicile_id " +
            "LEFT JOIN users as receipt_user on residenceAddresses.address_id = user_address_residence_id ";

    public static final String FILTER_FRAGMENT =
        "WHERE "+ DATE_FRAGMENT2 +";";

    private PreparedStatement getGetAmountOfReceiptsStatement;
    private PreparedStatement getReceiptsListStatement;


    private PreparedStatement getGetAmountOfReceiptsStatement() throws SQLException {
        if(getGetAmountOfReceiptsStatement == null) {
            getGetAmountOfReceiptsStatement = connection.prepareStatement("SELECT COUNT(receipt_id) AS amount_of_receipts FROM Receipts"
		+ FILTER_FRAGMENT);
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
	String date=filter.getValue(FilterField.RECEIPT_DATE);
	if(date.equals("")){
	    date = "CURRENT_TIMESTAMP";
	    //date=DateTime.now().toString();
	}
        ps.setString(start, date);
    }
}
