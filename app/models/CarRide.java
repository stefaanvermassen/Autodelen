package models;

import org.joda.time.DateTime;

/**
 * Created by HannesM on 10/03/14.
 */
public class CarRide {
    private Reservation reservation; // reservation.getId() is CarRide-id
    private CarRideStatus status;
    private int startMileage;
    private int endMileage;

    public CarRide(Reservation reservation) {
        this.reservation = reservation;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public int getStartMileage() {
        return startMileage;
    }

    public void setStartMileage(int startMileage) {
        this.startMileage = startMileage;
    }

    public int getEndMileage() {
        return endMileage;
    }

    public void setEndMileage(int endMileage) {
        this.endMileage = endMileage;
    }

    public CarRideStatus getStatus() {
        return status;
    }

    public void setStatus(CarRideStatus status) {
        this.status = status;
    }
}
