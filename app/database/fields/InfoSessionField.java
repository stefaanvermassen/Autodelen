package database.fields;

/**
 * Created by HannesM on 22/03/14.
 */
public enum InfoSessionField {
    // TODO: add more fields
    DATE;

    public static InfoSessionField stringToField(String string) {
        InfoSessionField field = null;
        switch(string) {
            case "date" :
                field = DATE;
                break;
        }
        return field;
    }
}
