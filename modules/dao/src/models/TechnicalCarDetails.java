package models;

/**
 * Created by HannesM on 23/04/14.
 */
public class TechnicalCarDetails {
    private Integer id = null;
    private String licensePlate;
    private FileGroup registration;
    private Integer chassisNumber;

    public TechnicalCarDetails(String licensePlate, FileGroup registration, Integer chassisNumber) {
        this(null, licensePlate, registration, chassisNumber);
    }

    public TechnicalCarDetails(Integer id, String licensePlate, FileGroup registration, Integer chassisNumber) {
        this.id = id;
        this.licensePlate = licensePlate;
        this.registration = registration;
        this.chassisNumber = chassisNumber;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public FileGroup getRegistration() {
        return registration;
    }

    public void setRegistration(FileGroup registration) {
        this.registration = registration;
    }

    public Integer getChassisNumber() {
        return chassisNumber;
    }

    public void setChassisNumber(Integer chassisNumber) {
        this.chassisNumber = chassisNumber;
    }
}
