package models;

/**
 * Created by stefaan on 04/03/14.
 */
public enum MailType {
    VERIFICATION(1), WELCOME(2), INFOSESSION_ENROLLED(3), RESERVATION_APPROVE_REQUEST(4), RESERVATION_APPROVED_BY_OWNER(5), PASSWORD_RESET(6);
    private final int key;

    private MailType(final int key) {
        this.key = key;
    }

    public int getKey(){
        return key;
    }
}