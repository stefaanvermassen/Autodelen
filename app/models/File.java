package models;

public class File {
    private int fileId;
    private String path;

    private String fileName;
    private String contentType;

    public File(int fileId, String path, String fileName, String contentType) {
        this.fileId = fileId;
        this.path = path;
        this.fileName = fileName;
        this.contentType = contentType;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
