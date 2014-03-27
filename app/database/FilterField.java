package database;

/**
 * Created by Benjamin on 23/03/2014.
 */

/**
 * Fields we can filter on the DAOs
 */
public enum FilterField {
    CAR_NAME, ZIPCODE, CAR_SEATS, CAR_GPS, CAR_HOOK, DATE, FROM, UNTIL, UNKNOWN, CAR_BRAND;

    /**
     *
     * @param string The string corresponding to the FilterField
     * @return The corresponding FilterField (or null if there is none)
     */
    public static FilterField stringToField(String string) {
        FilterField field = null;
        switch(string) {
            case "name":
                field = CAR_NAME;
                break;
            case "brand":
                field = CAR_BRAND;
                break;
            case "zipcode":
                field = ZIPCODE;
                break;
            case "seats":
                field = CAR_SEATS;
                break;
            case "gps":
                field = CAR_GPS;
                break;
            case "hook":
                field = CAR_HOOK;
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