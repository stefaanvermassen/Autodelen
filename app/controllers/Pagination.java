package controllers;

import database.Filter;
import database.fields.FilterField;
import database.jdbc.JDBCFilter;

/**
 * Helper-class with functions that can help you when using pagination.js
 */
public class Pagination {

    public static boolean parseBoolean(int i) {
        return i == 1;
    }

    public static Filter parseFilter(String searchString) {
        Filter filter = new JDBCFilter();
        if(searchString != "") {
            String[] searchStrings = searchString.split(",");
            for(String s : searchStrings) {
                String[] s2 = s.split("=");
                if(s2.length == 2) {
                    String field = s2[0];
                    String value = s2[1];
                    filter.fieldContains(FilterField.stringToField(field), value);
                }
            }
        }
        return filter;
    }
}
