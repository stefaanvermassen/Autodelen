package database;

/**
 * Created by Benjamin on 23/03/2014.
 */

/**
 * Fields we can filter on the DAOs
 */
public enum FilterField {

    CAR_NAME(false), CAR_BRAND(false), CAR_SEATS(true), CAR_GPS(true), CAR_HOOK(true), CAR_ID(true), CAR_FUEL(true),
    CAR_COST_STATUS(false), CAR_COST_DATE(true),
    USER_NAME(false), USER_FIRSTNAME(false), USER_LASTNAME(false), USER_ID(true),
    ZIPCODE(false),
    INFOSESSION_DATE(true), INFOSESSION_TYPE(false),
    RESERVATION_USER_OR_OWNER_ID(true), RESERVATION_STATUS(true),
    MESSAGE_RECEIVER_ID(true), MESSAGE_SENDER_ID(true),
    NOTIFICATION_READ(true),
    FROM(true), UNTIL(true);

    boolean exactValue;

    private FilterField(boolean exactValue) {
        this.exactValue = exactValue;
    }

    public boolean useExactValue() {
        return exactValue;
    }

    /**
     *
     * @param string The string corresponding to the FilterField
     * @return The corresponding FilterField (or null if there is none)
     */
    public static FilterField stringToField(String string) {
        FilterField field = null;
        switch(string) {
            case "car_id":
                field = CAR_ID;
                break;
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
            case "fuel":
                field = CAR_FUEL;
                break;
            case "from":
                field = FROM;
                break;
            case "until":
                field = UNTIL;
                break;
            case "infosession_date" :
                field = INFOSESSION_DATE;
                break;
            case "infosession_type" :
                field = INFOSESSION_TYPE;
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
            case "message_receiver_id":
                field = MESSAGE_RECEIVER_ID;
                break;
            case "message_sender_id":
                field = MESSAGE_SENDER_ID;
                break;
            case "notification_read":
                field = NOTIFICATION_READ;
                break;
            case "status":
                field = RESERVATION_STATUS;
                break;
            case "car_cost_status":
                field = CAR_COST_STATUS;
                break;
            case "car_cost_date":
                field = CAR_COST_DATE;
                break;
        }
        return field;
    }
}