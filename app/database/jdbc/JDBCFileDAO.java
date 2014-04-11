package database.jdbc;

import database.DataAccessException;
import database.FileDAO;
import models.File;

import java.util.List;

public class JDBCFileDAO implements FileDAO {
    @Override
    public File getFile(int id) throws DataAccessException {
        return null;
    }

    @Override
    public List<File> getFiles(int fileGroup) throws DataAccessException {
        return null;
    }

    @Override
    public File createFile(String path, int fileGroup) throws DataAccessException {
        return null;
    }

    @Override
    public File createFile(String path) throws DataAccessException {
        return null;
    }
}
