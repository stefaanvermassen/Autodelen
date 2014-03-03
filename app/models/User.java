package models;

import java.util.*;

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
    private Address address;
    private DriverLicense license;
    private UserStatus status;
    private IdentityCard identityCard;
    private EnumSet<UserRole> roles = EnumSet.noneOf(UserRole.class);

    public User(int id, String email, String firstName, String lastName){
        this(id, email, firstName, lastName, null, null);
    }

    public User(String email) {
        this(0, email, null, null, null, null);
    }

    public User() {
        this(0, null, null, null, null, null);
    }

    public User(int id, String email, String firstName, String lastName, String password, Address address){
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.address = address;

        roles.add(UserRole.USER);
        this.status = UserStatus.REGISTERED;
    }

    public void setId(int id){
        this.id = id;
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

    public boolean hasRole(UserRole role) { return roles.contains(role); }

    public void addRole(UserRole role) { roles.add(role); }

    public void dropRole(UserRole role) {
        roles.remove(role);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
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

    @Override
    public String toString(){
        return firstName + " " + lastName;
    }
    
}