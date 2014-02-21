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
    private EnumSet<UserRole> roles = EnumSet.noneOf(UserRole.class);

    public User(String email) {
        this.email = email;
        roles.add(UserRole.USER);
    }

    public User(int id, String email, String firstName, String lastName, String password){
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
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

    public boolean gotRole(UserRole role) { return roles.contains(role); }

    public void addRole(UserRole role) { roles.add(role); }
}