    package models;
    import org.joda.time.DateTime;

    import java.sql.Date;
    import java.text.DateFormat;
    import java.text.SimpleDateFormat;

public class Receipt {

    private int id;
    private String name;
    private File files;
    private DateTime date;
    private User user;
    private int price;

    public Receipt(int id, String name, File files, DateTime date, User user, int price) {
        this.id = id;
        this.name = name;
        this.files = files;
        this.date = date;
        this.user=user;
        this.price=price;
    }

    public Receipt(int id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
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

    public File getFiles() {
        return files;
    }

    public void setFiles(File files) {
        this.files = files;
    }

    public DateTime getDate() {
        return date;
    }

    public String getDateString() {
        return new SimpleDateFormat("dd-MM-yyyy").format(date.toDate());
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}

