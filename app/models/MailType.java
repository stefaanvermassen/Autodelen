package models;

/**
 * Created by stefaan on 04/03/14.
 */
public enum MailType {
    VERIFICATION(1);
    private final int key;

    private MailType(final int key) {
        this.key = key;
    }

    public int getKey(){
        return key;
    }
}
