package database.fields;

/**
 * Created by Benjamin on 23/03/2014.
 */
public enum FilterField {
    NAME, ZIPCODE, SEATS, GPS, HOOK, DATE, FROM, UNTIL, UNKNOWN, BRAND;

    public static FilterField stringToField(String string) {
        FilterField field = null;
        switch(string) {
            case "name":
                field = NAME;
                break;
            case "brand":
                field = BRAND;
                break;
            case "zipcode":
                field = ZIPCODE;
                break;
            case "seats":
                field = SEATS;
                break;
            case "gps":
                field = GPS;
                break;
            case "hook":
                field = HOOK;
                break;
            case "from":
                field = FROM;
                break;
            case "until":
                field = UNTIL;
                break;
            case "date" :
                field = DATE;
                break;
        }
        return field;
    }
}