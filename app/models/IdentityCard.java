/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

/**
 *
 * @author Laurent
 */
public class IdentityCard {
    
    private int id;
    private String file;

    public IdentityCard(int id, String file) {
        this.id = id;
        this.file = file;
    }

    
    public int getId() {
        return id;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
    
    
}
