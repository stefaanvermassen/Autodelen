package database;

/**
 * Created by HannesM on 17/03/14.
 */
public interface Filter<F extends Enum<F>> {
    public void fieldContains(F field, String string);
    public String getFieldContains(F field);
}
