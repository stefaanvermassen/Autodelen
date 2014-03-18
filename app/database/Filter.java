package database;

/**
 * Created by HannesM on 17/03/14.
 */
public interface Filter<F extends Enum<F>> {
    public void fieldContains(F field, String string);
    public void fieldStartsWith(F field, String string);
    public void fieldEndsWith(F field, String string);
}
