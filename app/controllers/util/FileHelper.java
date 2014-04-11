package controllers.util;

import play.api.Play;
import play.api.mvc.MultipartFormData;
import play.mvc.Http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Cedric on 4/11/2014.
 */
public class FileHelper {

    private static final boolean MOVE_INSTEAD_OF_COPY = true;

    public static final Set<String> IMAGE_CONTENT_TYPES = new HashSet<>(Arrays.asList(new String[]{"image/gif", "image/jpeg", "image/png"}));

    private static String uploadFolder;

    // Initialize path to save uploads to
    static {
        String property = ConfigurationHelper.getConfigurationString("uploads.path"); //TODO: Fix this uuuugly hack with a normal getString??
        if (property.startsWith("./")) {
            uploadFolder = Play.current().path().getAbsolutePath() + property.substring(2); // Get relative path to Play
        } else uploadFolder = property;
    }

    public static String saveFile(Http.MultipartFormData.FilePart filePart, String subfolder) throws IOException {
        File file = filePart.getFile();
        String fileName = filePart.getFilename();
        if (fileName.contains("/") || fileName.contains("\\"))
            throw new RuntimeException("Filename contains slashes.");

        String uuid = UUID.randomUUID().toString();
        String newFileName = uuid + "-" + fileName;

        Path path = Paths.get(uploadFolder, subfolder, newFileName);

        // Create subdirectories if not exist
        Files.createDirectories(path.getParent());

        // Copy or move upload data to our upload folder
        File toFile = new File(path.toAbsolutePath().toString());
        if (MOVE_INSTEAD_OF_COPY)
            moveFile(file, toFile);
        else
            copyFile(file, toFile);

        return Paths.get(subfolder, newFileName).toString();
    }

    /**
     * Deletes a file relative to the upload path
     *
     * @param path The file path to delete
     * @returns Whether the delete operation was successfull
     */
    public static boolean deleteFile(String path) throws IOException {
        try {
            Path absPath = Paths.get(uploadFolder, path);
            Files.delete(absPath);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private static void moveFile(File sourceFile, File destFile) throws IOException {
        Files.move(java.nio.file.Paths.get(sourceFile.getAbsolutePath()), java.nio.file.Paths.get(destFile.getAbsolutePath()));
    }

    private static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }
}
