package database;

/**
 * Created by Benjamin on 23/03/2014.
 */

/**
 * Fields we can filter on the DAOs
 */
public enum FilterField {
    CAR_NAME, CAR_BRAND, CAR_SEATS, CAR_GPS, CAR_HOOK,
    USER_NAME, USER_FIRSTNAME, USER_LASTNAME,
    ZIPCODE,
    INFOSESSION_DATE,
    RESERVATION_USER_OR_OWNER_ID, RESERVATION_STATUS,
    FROM, UNTIL,
    UNKNOWN;

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
                field = INFOSESSION_DATE;
                break;
            case "user_name":
                field = USER_NAME;
                break;
            case "user_firstname":
                field = USER_FIRSTNAME;
                break;
            case "user_lastname":
                field = USER_LASTNAME;
                break;
            case "status":
                field = RESERVATION_STATUS;
                break;
        }
        return field;
    }
}