/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.List;

/**
 *
 * @author Laurent
 */
public class InfoSession {
    
    private int id;
    private String time;
    private String address;
    private User host;
    private List<Enrollee> enrolled;

    public InfoSession(int id, String time, String address, User host, List<Enrollee> enrolled) {
        this.id = id;
        this.time = time;
        this.address = address;
        this.host = host;
        this.enrolled = enrolled;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
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
    
    public void addEnrollee(Enrollee enrollee){
        this.enrolled.add(enrollee);
    }
    
    public void deleteEnrollee(Enrollee enrollee){
        if(this.enrolled.contains(enrollee)){
            this.enrolled.remove(enrollee);
        }        
    }
    
}
