package database;

/**
 * Created by HannesM on 17/03/14.
 */
public interface Filter {
    public void fieldIs(FilterField field, String string);
    public void fieldContains(FilterField field, String string);

    public String getFieldIs(FilterField field);
    // TODO: delete exactValue and use getFieldIs
    public String getFieldContains(FilterField field, boolean exactValue);
}
