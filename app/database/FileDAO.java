package database;

import models.File;

import java.util.List;

/**
 * Created by Cedric on 4/11/2014.
 */
public interface FileDAO {
    public File getFile(int id) throws DataAccessException;
    public List<File> getFiles(int fileGroup) throws DataAccessException;
    public File createFile(String path, int fileGroup) throws DataAccessException;
    public File createFile(String path) throws DataAccessException;
}
