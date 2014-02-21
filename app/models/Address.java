package models;

/**
 * Created by Cedric on 2/21/14.
 */
public class Address {
    private int id;
    private String zip;
    private String city;
    private String street;
    private String number;
    private String bus;


    public Address(String zip, String city, String street, String number, String bus) {
        this(0, zip, city, street, number, bus);
    }

    public Address(int id, String zip, String city, String street, String number, String bus) {
        this.id = id;
        this.zip = zip;
        this.city = city;
        this.street = street;
        this.number = number;
        this.bus = bus;
    }

    public int getId() {
        return id;
    }

    public String getZip() {
        return zip;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getBus() {
        return bus;
    }

    public void setBus(String bus) {
        this.bus = bus;
    }
}
