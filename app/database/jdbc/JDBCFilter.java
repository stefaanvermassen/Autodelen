package database.jdbc;

import database.Filter;
import database.FilterField;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by HannesM on 20/03/14.
 */
public class JDBCFilter implements Filter {

    // EnumMap doesn't want F.class as a constructor-argument, so we use HashMap
    private Map<FilterField, String> contains = new HashMap<FilterField, String>();

    private Map<FilterField, String> is = new HashMap<>();

    @Override
    public void fieldIs(FilterField field, String string) {
        is.put(field, string);
    }

    /**
     *
     * @param field The field you want to filter on
     * @param string The string you want the field to contain
     */
    @Override
    public void fieldContains(FilterField field, String string) {
        contains.put(field, string);
    }

    @Override
    public String getFieldIs(FilterField field) {
        String string;
        if(is.containsKey(field))
            string =  is.get(field);
        else
            string =  "";

        return string;
    }

    /**
     *
     * @param field The field you want to get a filter-representation for
     * @param exactValue The exact value
     * @return A JDBC-SQL-representation (or exact representation) of the value you want the field to contain
     */
    @Override
    public String getFieldContains(FilterField field, boolean exactValue) {
        String string;
        if(contains.containsKey(field))
            string =  contains.get(field);
        else
            string =  "";

        if(exactValue)
            return string;
        return "%" + string + "%";
    }
}
