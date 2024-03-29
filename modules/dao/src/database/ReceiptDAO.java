package database;

import models.*;

import java.math.BigDecimal;
import java.util.List;
import org.joda.time.DateTime;

/**
 * Created by Cedric on 4/11/2014.
 */
public interface ReceiptDAO {
     public Receipt createReceipt(String name, DateTime date, File file, User user, BigDecimal price) throws DataAccessException;
     public List<Receipt> getReceiptsList(FilterField orderBy, boolean asc, int page, int PAGE_SIZE, Filter filter, User user) throws DataAccessException;
     public int getAmountOfReceipts(Filter filter, User user); 
}
