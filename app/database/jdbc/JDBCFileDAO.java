package database.jdbc;

import database.DataAccessException;
import database.FileDAO;
import models.File;
import models.FileGroup;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JDBCFileDAO implements FileDAO {

    private Connection connection;

    private PreparedStatement createFileStatement;
    private PreparedStatement getFileGroupStatement;
    private PreparedStatement getFileStatement;

    public JDBCFileDAO(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getGetFileStatement() throws SQLException {
        if (getFileStatement == null) {
            getFileStatement = connection.prepareStatement("SELECT file_id, file_path, file_name, file_content_type FROM files WHERE file_id = ?");
        }
        return getFileStatement;
    }

    private PreparedStatement getCreateFileStatement() throws SQLException {
        if (createFileStatement == null) {
            createFileStatement = connection.prepareStatement("INSERT INTO files(file_path, file_name, file_content_type, file_file_group_id) VALUES(?,?)", new String[]{"file_id"});
        }
        return createFileStatement;
    }

    private PreparedStatement getGetFileGroupStatement() throws SQLException {
        if (getFileGroupStatement == null) {
            getFileGroupStatement = connection.prepareStatement("SELECT file_id, file_path, file_name, file_content_type FROM files WHERE file_file_id = ?");
        }
        return getFileGroupStatement;
    }

    private static File populateFile(ResultSet rs) throws SQLException {
        return new File(rs.getInt("file_id"), rs.getString("file_path"), rs.getString("file_name"), rs.getString("file_content_type"));
    }

    @Override
    public File getFile(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getGetFileStatement();
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if(!rs.next())
                    return null;
                else
                    return populateFile(rs);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to read file resultset.", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get file from db.", ex);
        }
    }

    @Override
    public FileGroup getFiles(int fileGroup) throws DataAccessException {
        try {
            PreparedStatement ps = getGetFileGroupStatement();
            ps.setInt(1, fileGroup);

            try (ResultSet rs = ps.executeQuery()) {
                List<File> files = new ArrayList<>();
                while (rs.next()) {
                    files.add(populateFile(rs));
                }
                return new FileGroup(files);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to read filegroup resultset.", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get files from group.", ex);
        }
    }


    @Override
    public File createFile(String path, String fileName, String contentType, int fileGroup) throws DataAccessException {
        try {
            PreparedStatement ps = getCreateFileStatement();
            ps.setString(1, path);
            ps.setString(2, fileName);
            ps.setString(3, contentType);

            if (fileGroup == -1)
                ps.setNull(4, Types.NULL);
            else
                ps.setInt(4, fileGroup);

            if (ps.executeUpdate() != 1)
                throw new DataAccessException("New file record failed. No rows affected.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next())
                    throw new DataAccessException("Failed to read keys for new file record.");
                return new File(keys.getInt(1), path, fileName, contentType);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new file.", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create file in database.", ex);
        }
    }

    @Override
    public File createFile(String path, String fileName, String contentType) throws DataAccessException {
        return createFile(path, null, null, -1);
    }
}
