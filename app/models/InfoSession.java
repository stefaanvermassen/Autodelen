/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Laurent
 */
public class InfoSession {

    public static final List<Enrollee> NO_ENROLLEES = new ArrayList<Enrollee>(0);

    private int id;
    private DateTime time;
    private Address address;
    private User host;
    private List<Enrollee> enrolled;

    public InfoSession(int id, DateTime time, Address address, User host, List<Enrollee> enrolled) {
        this.id = id;
        this.time = time;
        this.address = address;
        this.host = host;
        if(enrolled == null)
            this.enrolled = NO_ENROLLEES;
        else
            this.enrolled = enrolled;
    }

    public InfoSession(int id, DateTime time, Address address, User host) {
        this(id, time, address, host, NO_ENROLLEES);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public User getHost() {
        return host;
    }

    public void setHost(User host) {
        this.host = host;
    }

    public List<Enrollee> getEnrolled() {
        return enrolled;
    }

    public void setEnrolled(List<Enrollee> enrolled) {
        this.enrolled = enrolled;
    }

    public void addEnrollee(Enrollee enrollee) {
        if (this.enrolled == NO_ENROLLEES) //lazy loading
            this.enrolled = new ArrayList<Enrollee>();

        this.enrolled.add(enrollee);
    }

    public boolean hasEnrolled(){
        return !this.enrolled.isEmpty();
    }

    public void deleteEnrollee(Enrollee enrollee) {
        this.enrolled.remove(enrollee);
    }

}
