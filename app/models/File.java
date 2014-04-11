package models;

public class File {
    private int fileId;
    private String path;

    public File(int fileId, String path) {
        this.fileId = fileId;
        this.path = path;
    }

    public int getId(){
        return fileId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
