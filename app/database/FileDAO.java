package database;

import models.File;
import models.FileGroup;

import java.util.List;

/**
 * Created by Cedric on 4/11/2014.
 */
public interface FileDAO {
    public File getFile(int id) throws DataAccessException;
    public FileGroup getFiles(int fileGroup) throws DataAccessException;
    public File createFile(String path, String fileName, String contentType, int fileGroup) throws DataAccessException;
    public File createFile(String path, String fileName, String contentType) throws DataAccessException;
}
