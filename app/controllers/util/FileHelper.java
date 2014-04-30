package controllers.util;

import database.DataAccessContext;
import database.DataAccessException;
import database.FileDAO;
import database.UserDAO;
import models.User;
import models.UserRole;
import play.Logger;
import play.api.Play;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import providers.DataProvider;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

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

    private static Path createPath(String fileName, String subfolder){
        if (fileName.contains("/") || fileName.contains("\\"))
            throw new RuntimeException("Filename contains slashes.");

        String uuid = UUID.randomUUID().toString();
        String newFileName = uuid + "-" + fileName;

        return Paths.get(subfolder, newFileName);
    }

    public static Path saveFile(Http.MultipartFormData.FilePart filePart, String subfolder) throws IOException {
        Path path = createPath(filePart.getFilename(), subfolder);
        Path absolutePath = Paths.get(uploadFolder).resolve(path.toString());

        // Create subdirectories if not exist
        Files.createDirectories(absolutePath.getParent());

        // Copy or move upload data to our upload folder
        File file = filePart.getFile();
        File toFile = absolutePath.toFile();
        if (MOVE_INSTEAD_OF_COPY)
            moveFile(file, toFile);
        else
            copyFile(file, toFile);

        Logger.debug("File (" + filePart.getContentType() + ") upload to " + path);
        return path;
    }

    public static Path saveResizedImage(Http.MultipartFormData.FilePart filePart, String subfolder, int maxWidth) throws IOException {
        Path path = createPath(filePart.getFilename(), subfolder);
        Path absolutePath = Paths.get(uploadFolder).resolve(path.toString());

        // Create subdirectories if not exist
        Files.createDirectories(absolutePath.getParent());

        // Copy or move upload data to our upload folder
        File file = filePart.getFile();
        File toFile = absolutePath.toFile();
        createResizedJpeg(file, toFile, maxWidth);

        Logger.debug("File (%s - resized maxw=%d) uploaded to %s%n", filePart.getContentType(), maxWidth, path);
        return path;
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
    public static boolean deleteFile(Path path) throws IOException {
        try {
            Path absPath = Paths.get(uploadFolder).resolve(path);
            Files.delete(absPath);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private static void moveFile(File sourceFile, File destFile) throws IOException {
        Files.move(java.nio.file.Paths.get(sourceFile.getAbsolutePath()), java.nio.file.Paths.get(destFile.getAbsolutePath()));
    }

    private static void createResizedJpeg(File sourceFile, File destFile, int maxWidth) throws IOException {
        try(ImageInputStream iis = ImageIO.createImageInputStream(sourceFile)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            while(readers.hasNext()){
                ImageReader reader = readers.next();
                try {
                    reader.setInput(iis, false); // We want to enforce a specific reader to keep the format
                    if (reader.getNumImages(true) > 1) {
                        throw new RuntimeException("Multi-image containers are disabled.");
                    }

                    BufferedImage sourceImage = reader.read(0); //no support for multi-image files??
                    Image thumbnail = sourceImage.getScaledInstance(maxWidth, -1, Image.SCALE_SMOOTH);
                    BufferedImage bufferedThumbnail = new BufferedImage(thumbnail.getWidth(null), thumbnail.getHeight(null), BufferedImage.TYPE_INT_RGB);
                    bufferedThumbnail.getGraphics().drawImage(thumbnail, 0, 0, null);
                    ImageIO.write(bufferedThumbnail, reader.getFormatName(), new FileOutputStream(destFile, false));
                    return;
                } catch(IIOException ex){
                    continue;
                }
            }
            throw new RuntimeException("No image reader installed for given format.");
        } catch(IOException ex){
            //??
            throw ex;
        }
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

    public static Result genericFileAction(int userId, int fileId, FileAction action){
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            UserDAO udao = context.getUserDAO();
            FileDAO fdao = context.getFileDAO();
            User user = udao.getUser(userId, true);

            if (user == null) {
                return Controller.badRequest(views.html.unauthorized.render(new UserRole[]{UserRole.PROFILE_ADMIN}));
            }

            models.File file = action.getFile(fileId, user, fdao, context);
            if(file == null) {
                Controller.flash("danger", "Bestand niet gevonden.");
                return action.failAction(user);
            } else {
                try {
                    return action.process(file, fdao, context);
                } catch(IOException | DataAccessException ex){
                    context.rollback();
                    throw new RuntimeException(ex);
                }
            }
        } catch(DataAccessException ex){
            throw ex;
        }
    }
}
