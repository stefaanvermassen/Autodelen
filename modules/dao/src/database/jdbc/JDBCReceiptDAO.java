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
	" Receipts.receipt_userID LIKE ? ";

    public static final String DATE_FRAGMENT1 =
        " Receipts.receipt_date BETWEEN ? AND ? ";

    public static final String DATE_FRAGMENT2 =
        " Receipts.receipt_date=? ";

    private static final String RECEIPT_FIELDS = " receipt_id, receipt_name, receipt_date, receipt_fileID, receipt_userID ";

    private static final String RECEIPT_QUERY = "SELECT " + RECEIPT_FIELDS + " FROM Receipts " +
            " LEFT JOIN files as receipt_file on files.file_id = receipt_file " +
            " LEFT JOIN users as receipt_user on users.user_id = receipt_user ";

    public static final String FILTER_FRAGMENT =
        " WHERE "+ USER_FRAGMENT + DATE_FRAGMENT2 +";";

    private PreparedStatement getGetAmountOfReceiptsStatement;
    private PreparedStatement getReceiptsListStatement;


    private PreparedStatement getGetAmountOfReceiptsStatement() throws SQLException {
        if(getGetAmountOfReceiptsStatement == null) {
            getGetAmountOfReceiptsStatement = connection.prepareStatement("SELECT COUNT(receipt_id) AS amount_of_receipts FROM Receipts " + FILTER_FRAGMENT);
	    //getGetAmountOfReceiptsStatement = connection.prepareStatement("SELECT COUNT(receipt_id) AS amount_of_receipts FROM Receipts WHERE Receipts.receipt_date<=?;");
        }
        return getGetAmountOfReceiptsStatement;
    }
    
    private PreparedStatement getReceiptsListStatement() throws SQLException {
        if(getReceiptsListStatement == null) {
            getReceiptsListStatement = connection.prepareStatement(RECEIPT_QUERY + FILTER_FRAGMENT + "ORDER BY Receipts.receipt_date asc LIMIT ?, ?");
        }
        return getReceiptsListStatement;
    }

    /**
     * @param filter The filter to apply to
     * @return The amount of filtered cars
     * @throws DataAccessException
     */
    @Override
    public int getAmountOfReceipts(Filter filter, User user) throws DataAccessException {
        try {
            PreparedStatement ps = getGetAmountOfReceiptsStatement();
            fillFragment(ps, filter, 1, user);

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

    public List<Receipt> getReceiptsList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter, User user){
        try {
            PreparedStatement ps = getReceiptsListStatement();
            fillFragment(ps, filter, 2, user);
            int first = (page-1)*pageSize;
            ps.setInt(5, first);
            ps.setInt(6, pageSize);
            return getReceipts(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of reciepts", ex);
        }
    }

    private List<User> getReceipts(PreparedStatement ps) {
        List<Receipts> receipts = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                receipts.add(populateReceipt(rs, false, true));
            }
            return users;
        } catch (SQLException ex) {
            throw new DataAccessException("Error reading receipts resultset", ex);
        }
    }

    private void fillFragment(PreparedStatement ps, Filter filter, int start, User user) throws SQLException {
	
	ps.setInt(start, user.getId());        

	if(filter == null) {
            // getFieldContains on a "empty" filter will return the default string "%%", so this does not filter anything
            filter = new JDBCFilter();
        }
	String string_date=filter.getValue(FilterField.RECEIPT_DATE);
	//string_date="2036-10-29 11:42:32";
        Timestamp date;
	if(string_date.equals("")){
	    java.util.Date now= new java.util.Date();
	    date = new Timestamp(now.getTime());
	    //date=DateTime.now().toString();
	} else{
            date= Timestamp.valueOf(string_date);
        }
        ps.setTimestamp(start+1, date);
    }

    public static Receipt populateReceipt(ResultSet rs, boolean withDate, boolean withFiles) throws SQLException {
        return populateReceipt(rs, withPassword, withRest, "Receipts");
    }

    public static Receipt populateReceipt(ResultSet rs, boolean withDate, boolean withFiles, String tableName) throws SQLException {
        Receipt receipt = new Receipt(
		rs.getInt(tableName + ".receipt_id"), 
		rs.getString(tableName + ".receipt_name"));

	receipt.setUser(JDBCUserDAO.populateUser(rs, "receipt_user"))
        if(withFiles) {
	    receipt.setFiles(JDBCFilesDAO.populateFiles(rs, "receipt_file"));
	}
        if(withDate) {
	    rs.getObject("setting_after") == null ? null : receipt.setDate(new DateTime(rs.getDate("setting_after")))
        }

        return user;
    }
}
