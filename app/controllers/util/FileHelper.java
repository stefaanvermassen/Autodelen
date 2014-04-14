package controllers.util;

import controllers.Assets;
import database.DataAccessContext;
import database.FileDAO;
import play.Logger;
import play.api.Play;
import play.api.mvc.MultipartFormData;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.*;
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

    //Source: http://www.cs.helsinki.fi/u/hahonen/uusmedia/sisalto/cgi_perl_ssi/mime.html
    public static final List<String> IMAGE_CONTENT_TYPES = Arrays.asList(new String[]{"image/gif", "image/jpeg", "image/png", "image/tiff"}); // array is too small to allocate a Set
    public static final List<String> DOCUMENT_CONTENT_TYPES = Arrays.asList(new String[]{"text/plain", "application/pdf"});

    private static String uploadFolder;

    // Initialize path to save uploads to
    static {
        String property = ConfigurationHelper.getConfigurationString("uploads.path");
        if (property.startsWith("./")) {
            uploadFolder = Paths.get(Play.current().path().getAbsolutePath(), property.substring(2)).toString(); // Get relative path to Play
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

        Logger.debug("File (" + filePart.getContentType() + ") upload to " + path);
        return Paths.get(subfolder, newFileName).toString();
    }

    public static Result getFileStreamResult(FileDAO dao, int fileId){
        models.File file = dao.getFile(fileId);
        if(file != null){
            try {
                FileInputStream is = new FileInputStream(Paths.get(uploadFolder, file.getPath()).toFile()); //TODO: this cannot be sent with a Try-with-resources (stream already closed), check if Play disposes properly
                return file.getContentType() != null && !file.getContentType().isEmpty() ? Controller.ok(is).as(file.getContentType()) : Controller.ok(is);
            } catch (FileNotFoundException e) {
                Logger.error("Missing file: " + file.getPath());
                return Controller.notFound();
            }
        } else return Controller.notFound();
    }

    public static boolean isImageContentType(String contentType){
        return IMAGE_CONTENT_TYPES.contains(contentType);
    }

    public static boolean isDocumentContentType(String contentType){
        return isImageContentType(contentType) || DOCUMENT_CONTENT_TYPES.contains(contentType);
    }

    /**
     * Returns a file in the public directory
     * @param path
     * @return
     */
    public static Result getPublicFile(String path, String contentType) {
        String playPath = Play.current().path().getAbsolutePath();
        try {
            FileInputStream is = new FileInputStream(Paths.get(playPath, "public", path).toFile());
            return Controller.ok(is).as(contentType);
        } catch (FileNotFoundException e) {
            return Controller.notFound();
        }
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
