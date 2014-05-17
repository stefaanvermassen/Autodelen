package database;

import models.*;

import java.util.List;

/**
 * Created by Cedric on 4/11/2014.
 */
public interface ReceiptDAO {
     public List<Receipt> getReceiptsList(FilterField orderBy, boolean asc, int page, int PAGE_SIZE, Filter filter, User user) throws DataAccessException;
     public int getAmountOfReceipts(Filter filter, User user); 
}
