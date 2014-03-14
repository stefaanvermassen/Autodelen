package models;

/**
 * Created by Cedric on 2/16/14.
 */
public class User {

    private int id;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private String phone;
    private String cellphone;
    private Address addressDomicile;
    private Address addressResidence;
    private DriverLicense license;
    private UserStatus status;
    private IdentityCard identityCard;
    private String damageHistory;
    private boolean payedDeposit;
    private boolean agreeTerms;
    private User contractManager;
    // TODO: image

    public User(int id, String email, String firstName, String lastName){
        this(id, email, firstName, lastName, null);
    }

    public User(String email) {
        this(0, email, null, null, null);
    }

    public User() {
        this(0, null, null, null, null);
    }

    public User(int id, String email, String firstName, String lastName, String password){
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;

        this.status = UserStatus.REGISTERED;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getCellphone() {
		return cellphone;
	}

	public void setCellphone(String cellphone) {
		this.cellphone = cellphone;
	}

	public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Address getAddressDomicile() {
        return addressDomicile;
    }

    public void setAddressDomicile(Address addressDomicile) {
        this.addressDomicile = addressDomicile;
    }

    public Address getAddressResidence() {
        return addressResidence;
    }

    public void setAddressResidence(Address addressResidence) {
        this.addressResidence = addressResidence;
    }
    public DriverLicense getLicense() {
        return license;
    }

    public void setLicense(DriverLicense license) {
        this.license = license;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public IdentityCard getIdentityCard() {
        return identityCard;
    }

    public void setIdentityCard(IdentityCard identityCard) {
        this.identityCard = identityCard;
    }

    public String getDamageHistory() {
        return damageHistory;
    }

    public void setDamageHistory(String damageHistory) {
        this.damageHistory = damageHistory;
    }

    public boolean isPayedDeposit() {
        return payedDeposit;
    }

    public void setPayedDeposit(boolean payedDeposit) {
        this.payedDeposit = payedDeposit;
    }

    public User getContractManager() {
        return contractManager;
    }

    public void setContractManager(User contractManager) {
        this.contractManager = contractManager;
    }

    public boolean isAgreeTerms() {
        return agreeTerms;
    }

    public void setAgreeTerms(boolean agreeTerms) {
        this.agreeTerms = agreeTerms;
    }

    @Override
    public String toString(){
        return firstName + " " + lastName;
    }
    
}