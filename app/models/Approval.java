package models;

import org.joda.time.DateTime;

/**
 * Created by Cedric on 3/30/2014.
 */
public class Approval {

    public enum ApprovalStatus {
        PENDING,
        ACCEPTED,
        DENIED
    }

    public User user;
    public User admin;
    public DateTime submitted;
    public DateTime reviewed;
    public InfoSession session;

}
