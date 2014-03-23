package database.fields;

/**
 * Created by HannesM on 18/03/14.
 */
public enum CarField { // Not Garfield
        // TODO: add more fields
        NAME, BRAND;

        public static CarField stringToField(String string) {
            CarField field = null;
            switch(string) {
                case "name":
                    field = NAME;
                    break;
                case "brand" :
                    field = BRAND;
                    break;
            }
            return field;
        }
}
