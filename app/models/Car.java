/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

/**
 *
 * @author Laurent
 */
public class Car {
    
    private int id;
    private String name;
    private String brand;
    private String type;
    private Address location;
    private int seats;
    private int doors;
    private int year;
    private boolean gps;
    private boolean hook;
    private CarFuel fuel;
    private int fuelEconomy;
    private int estimatedValue;
    private int ownerAnnualKm;
    private User owner;
    private String comments;
    private String lastEdit;

    public Car() {
        this(0, null, null, null, null, 0, 0, 0, false, false, null, 0, 0, 0, null, null, null);
    }

    public Car(int id, String name, String brand, String type, Address location, int seats, int doors, int year, boolean gps, boolean hook, CarFuel fuel, int fuelEconomy, int estimatedValue, int ownerAnnualKm, User owner, String comments, String lastEdit) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.type = type;
        this.location = location;
        this.seats = seats;
        this.doors = doors;
        this.year = year;
        this.gps = gps;
        this.hook = hook;
        this.fuel = fuel;
        this.fuelEconomy = fuelEconomy;
        this.estimatedValue = estimatedValue;
        this.ownerAnnualKm = ownerAnnualKm;
        this.owner = owner;
        this.comments = comments;
        this.lastEdit = lastEdit;
    }
   
    public int getId() {
        return id;
    }
    
    public void setId(int id){
        this.id=id;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Address getLocation() {
        return location;
    }

    public void setLocation(Address location) {
        this.location = location;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public int getDoors() {
        return doors;
    }

    public void setDoors(int doors) {
        this.doors = doors;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isGps() {
        return gps;
    }

    public void setGps(boolean gps) {
        this.gps = gps;
    }

    public boolean isHook() {
        return hook;
    }

    public void setHook(boolean hook) {
        this.hook = hook;
    }

    public CarFuel getFuel() {
        return fuel;
    }

    public void setFuel(CarFuel fuel) {
        this.fuel = fuel;
    }

    public int getFuelEconomy() {
        return fuelEconomy;
    }

    public void setFuelEconomy(int fuelEconomy) {
        this.fuelEconomy = fuelEconomy;
    }

    public int getEstimatedValue() {
        return estimatedValue;
    }

    public void setEstimatedValue(int estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    public int getOwnerAnnualKm() {
        return ownerAnnualKm;
    }

    public void setOwnerAnnualKm(int ownerAnnualKm) {
        this.ownerAnnualKm = ownerAnnualKm;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getLastEdit() {
        return lastEdit;
    }

    public void setLastEdit(String lastEdit) {
        this.lastEdit = lastEdit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Car car = (Car) o;

        if (doors != car.doors) return false;
        if (estimatedValue != car.estimatedValue) return false;
        if (fuelEconomy != car.fuelEconomy) return false;
        if (gps != car.gps) return false;
        if (hook != car.hook) return false;
        if (id != car.id) return false;
        if (ownerAnnualKm != car.ownerAnnualKm) return false;
        if (seats != car.seats) return false;
        if (year != car.year) return false;
        if (!brand.equals(car.brand)) return false;
        if (comments != null ? !comments.equals(car.comments) : car.comments != null) return false;
        if (fuel != car.fuel) return false;
        if (location != null ? !location.equals(car.location) : car.location != null) return false;
        if (!name.equals(car.name)) return false;
        if (owner.getId() != car.owner.getId()) return false;
        if (!type.equals(car.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        result = 31 * result + brand.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + seats;
        result = 31 * result + doors;
        result = 31 * result + year;
        result = 31 * result + (gps ? 1 : 0);
        result = 31 * result + (hook ? 1 : 0);
        result = 31 * result + (fuel != null ? fuel.hashCode() : 0);
        result = 31 * result + fuelEconomy;
        result = 31 * result + estimatedValue;
        result = 31 * result + ownerAnnualKm;
        result = 31 * result + owner.getId();
        result = 31 * result + (comments != null ? comments.hashCode() : 0);
        return result;
    }
}
