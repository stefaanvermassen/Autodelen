package database.jdbc;

import database.Filter;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by HannesM on 20/03/14.
 */
public class JDBCFilter<F extends Enum<F>> implements Filter<F> {

    // EnumMap doesn't want F.class as a constructor-argument, so we use HashMap
    private Map<F, String> contains = new HashMap<F, String>();

    @Override
    public void fieldContains(F field, String string) {
        contains.put(field, string);
    }

    @Override
    public String getFieldContains(F field) {
        String string;
        if(contains.containsKey(field))
            string =  contains.get(field);
        else
            string =  "";

        return "%" + string + "%";
    }
}
