package net.rptools.extra.files;

import net.rpTools.aoTool.app.Constants;
import net.rpTools.aoTool.app.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;

public class FileUtils {
    private static final Logger log = LogManager.getLogger(FileUtils.class);
    public static File createFolder(Path path){
        try {
            if(Files.exists(path)){
                return path.toFile();
            }
            File folder = Files.createDirectory(path).toFile();

            if(!folder.createNewFile() || !folder.setReadable(true) || !folder.setWritable(true)){
                throw new IOException("Unable to set permissions.");
            }
            return folder;
        } catch (IOException e) {
            log.info(e.getLocalizedMessage(), e);
            return null;
        }
    }
    public static File createFile(Path path){
        try {
            if(Files.exists(path)){
                return path.toFile();
            }

            File file = path.toFile();

            if(!file.createNewFile() || !file.setReadable(true) || !file.setWritable(true)){
                throw new IOException("Unable to set permissions.");
            }
            return file;
        } catch (IOException e) {
            log.info(e.getLocalizedMessage(), e);
            return null;
        }
    }

    public static File copyConfigFile(String configFileName, Path path){
        try(InputStream is = FileUtils.class.getResourceAsStream(Constants.RESOURCE_SAMPLES_MODELS_PATH + configFileName)){
            if(is == null){
                throw new IOException(MessageFormat.format("Unable to read source file: {0}", Constants.RESOURCE_SAMPLES_MODELS_PATH, configFileName));
            }
            Files.copy(is, path);
            return path.toFile();
        } catch (IOException ioe){
            log.info(ioe.getLocalizedMessage(), ioe);
            return null;
        }
    }
    public static boolean delete(Path path){
        try {
            if (Files.exists(path)) {
                Files.delete(path);
                return true;
            }
        } catch (IOException e){
            log.info(e.getLocalizedMessage(), e);
        }
        return false;
    }
    public static void deleteTempFiles(){
        Data.TEMP_FOLDERS.forEach((key, value) -> {
            if (!delete(value)) {
                log.info("Delete failed for: \"{}\", \"{}\"", key, value);
            } else {
                log.info("Deleted: \"{}\", \"{}\"", key, value);
            }
        });
    }
}
