/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

/**
 *
 * @author Laurent
 */
class Enrollee {
    
    private int id;
    private User user;
    private EnrollementStatus status;

    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public EnrollementStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollementStatus status) {
        this.status = status;
    }
    
    
}
