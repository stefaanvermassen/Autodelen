package models;

import java.util.Date;

/**
 * Created by Cedric on 4/21/2014.
 */
public class Setting {

    private int id;
    private String name;
    private String value;
    private Date afterDate;

    public Setting(int id, String name, String value, Date afterDate) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.afterDate = afterDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getAfterDate() {
        return afterDate;
    }

    public void setAfterDate(Date afterDate) {
        this.afterDate = afterDate;
    }
}
