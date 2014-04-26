package models;

/**
 * Created by HannesM on 23/04/14.
 */
public class TechnicalCarDetails {
    private Integer id = null;
    private String licensePlate;
    private String registration;
    private Integer chassisNumber;

    public TechnicalCarDetails(String licensePlate, String registration, Integer chassisNumber) {
        this.licensePlate = licensePlate;
        this.registration = registration;
        this.chassisNumber = chassisNumber;
    }

    public TechnicalCarDetails(int id, String licensePlate, String registration, Integer chassisNumber) {
        this.id = id;
        this.licensePlate = licensePlate;
        this.registration = registration;
        this.chassisNumber = chassisNumber;
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public Integer getChassisNumber() {
        return chassisNumber;
    }

    public void setChassisNumber(Integer chassisNumber) {
        this.chassisNumber = chassisNumber;
    }
}
