/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import org.joda.time.DateTime;

/**
 *
 * @author Laurent
 */
public class Reservation {
    
    private int id;
    private ReservationStatus status;
    private Car car;
    private User user;
    private DateTime from;
    private DateTime to;

    public Reservation(int id, Car car, User user, DateTime from, DateTime to) {
        this.car = car;
        this.user = user;
        this.from = from;
        this.to = to;
        this.status = ReservationStatus.REQUEST;
    }    
    
    public int getId() {
        return id;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DateTime getFrom() {
        return from;
    }

    public void setFrom(DateTime from) {
        this.from = from;
    }

    public DateTime getTo() {
        return to;
    }

    public void setTo(DateTime to) {
        this.to = to;
    }
    
}
