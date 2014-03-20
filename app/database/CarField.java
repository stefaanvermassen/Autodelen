package database;

/**
 * Created by HannesM on 18/03/14.
 */
public enum CarField { // Not Garfield
        NAME, BRAND;

        public static CarField stringToField(String string) {
            CarField carField = null;
            switch(string) {
                case "brand" :
                    carField = CarField.BRAND;
                    break;
                default: // also name
                    carField = CarField.NAME;
                    break;

            }
            return carField;
        }
}
