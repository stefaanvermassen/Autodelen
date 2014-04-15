package models;

import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * Created by Stefaan Vermassen on 15/04/14.
 */
public class CarCost {

    private int id;
    private Car car;
    private int amount;
    private DateTime time;
    private BigDecimal mileage;
    private String description;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public BigDecimal getMileage() {
        return mileage;
    }

    public void setMileage(BigDecimal mileage) {
        this.mileage = mileage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public CarCost(int id, Car car, int amount, BigDecimal mileage, String description, DateTime time){
        this.id = id;
        this.car = car;
        this.amount = amount;
        this.mileage = mileage;
        this.description = description;
        this.time = time;
    }
}
