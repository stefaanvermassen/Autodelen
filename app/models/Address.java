package models;

/**
 * Created by Cedric on 2/21/14.
 */
public class Address {
    private int id;
    private String zip;
    private String street;
    private int number; // TODO: convert to varchar
    private String bus;


    public Address(String zip, String street, int number, String bus) {
        this(0, zip, street, number, bus);
    }

    public Address(int id, String zip, String street, int number, String bus) {
        this.id = id;
        this.zip = zip;
        this.street = street;
        this.number = number;
        this.bus = bus;
    }

    public int getId(){
        return id;
    }

    public String getZip() {
        return zip;
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

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getBus() {
        return bus;
    }

    public void setBus(String bus) {
        this.bus = bus;
    }
}
