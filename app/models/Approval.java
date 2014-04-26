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

    private int id;
    private User user;
    private User admin;
    private DateTime submitted;
    private DateTime reviewed;
    private InfoSession session;
    private ApprovalStatus status;

    public Approval(int id, User user, User admin, DateTime submitted, DateTime reviewed, InfoSession session, ApprovalStatus status) {
        this.id = id;
        this.user = user;
        this.admin = admin;
        this.submitted = submitted;
        this.reviewed = reviewed;
        this.session = session;
        this.status = status;
    }

    public int getId(){
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getAdmin() {
        return admin;
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public DateTime getSubmitted() {
        return submitted;
    }

    public void setSubmitted(DateTime submitted) {
        this.submitted = submitted;
    }

    public DateTime getReviewed() {
        return reviewed;
    }

    public void setReviewed(DateTime reviewed) {
        this.reviewed = reviewed;
    }

    public InfoSession getSession() {
        return session;
    }

    public void setSession(InfoSession session) {
        this.session = session;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }
}
