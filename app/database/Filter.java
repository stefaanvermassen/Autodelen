package database;

import database.fields.FilterField;

/**
 * Created by HannesM on 17/03/14.
 */
public interface Filter {
    public void fieldContains(FilterField field, String string);
    public String getFieldContains(FilterField field, boolean exactValue);
}
