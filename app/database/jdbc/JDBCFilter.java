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
    private Map<FilterField, String> content = new HashMap<FilterField, String>();

    /**
     * Associate with a given filterfield a specified value.
     * @param field The filterfield
     * @param string The value
     */
    @Override
    public void putValue(FilterField field, String string) {
        content.put(field, string);
    }

    /**
     * Retrieve the value contained in the specified filterfield.
     * @param field The filterfield for which you want to retrieve the value
     * @return A JDBC-SQL-representation (or exact representation) of the value contained in the specified filterfield
     */
    @Override
    public String getValue(FilterField field) {
        String value = "";
        if(content.containsKey(field))
            value =  content.get(field);
        if(field.useExactValue())
            return value;
        return "%" + value + "%";
    }
}
