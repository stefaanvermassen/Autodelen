package database;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Cedric on 3/8/14.
 */
public class DatabaseConfiguration {

    private String server;
    private int port;
    private String database;
    private String username;
    private String password;

    private DatabaseConfiguration(String server, int port, String database, String username, String password){
        this.server = server;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public static DatabaseConfiguration loadConfiguration(String path) throws IOException {
        try(FileInputStream fi = new FileInputStream(path)){
            return getConfiguration(fi);
        } catch(IOException ex){
            throw ex;
        }
    }

    public static DatabaseConfiguration getConfiguration(InputStream stream) throws IOException {
        Properties p = new Properties();
        p.load(stream);

        return new DatabaseConfiguration(p.getProperty("server"), Integer.parseInt(p.getProperty("port", "3306")), p.getProperty("database"), p.getProperty("username"), p.getProperty("password", ""));
    }

    public String getServer() {
        return server;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
