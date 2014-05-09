package models;

/**
 * Created by HannesM on 10/03/14.
 */
public class CarRide {
    private Reservation reservation; // reservation.getId() is CarRide-id
    private boolean status;
    private int startMileage;
    private int endMileage;
    private boolean damaged;

    private int refueling;

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

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getRefueling() {
        return refueling;
    }

    public void setRefueling(int refueling) {
        this.refueling = refueling;
    }

    public boolean isDamaged() {
        return damaged;
    }

    public void setDamaged(boolean damaged) {
        this.damaged = damaged;
    }
}
