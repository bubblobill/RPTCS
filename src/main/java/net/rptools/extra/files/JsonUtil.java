package net.rptools.extra.files;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonUtil {
    private static final Logger log = LogManager.getLogger(JsonUtil.class);
    private static final Gson gson = new Gson();

    public static String stringify(Object o) {
        return gson.toJson(o); // Java object to json
    }
    public static JsonElement parse(Object source) {
        JsonElement jsonElement = JsonNull.INSTANCE;
        if(source instanceof String string) {
            jsonElement = JsonParser.parseString(string);
        } else if (source instanceof Reader reader) {
            jsonElement = JsonParser.parseReader(reader);
        } else if(source instanceof JsonReader reader) {
            jsonElement = JsonParser.parseReader(reader);
        } else if(source instanceof File file) {
            try (Reader reader = new FileReader(file)) {
                jsonElement = JsonParser.parseReader(reader);
            } catch (IOException ioe) {
                log.info(ioe.getLocalizedMessage(), ioe);
            }
        } else if(source instanceof Path path) {
            try (Reader reader = Files.newBufferedReader(path)) {
                jsonElement = JsonParser.parseReader(reader);
            } catch (IOException ioe) {
                log.info(ioe.getLocalizedMessage(), ioe);
            }
        }
        return jsonElement;
    }

}
