package net.rptools.extra.addon.wrappers;

import com.google.gson.JsonObject;
import net.rpTools.aoTool.asset.MD5Key;

import java.nio.file.Path;

public class WMTScript {
    public record MTScript(String path, boolean autoExecute, String description, MD5Key md5Key) {
    }
    Path filename;
    boolean autoExecute;
    public WMTScript(){}
    public WMTScript(JsonObject mtsObject){
        if(mtsObject.keySet().contains("filename")) {
            this.filename = Path.of(mtsObject.get("filename").getAsString());
        }
        if(mtsObject.keySet().contains("autoExecute")) {
            this.autoExecute = mtsObject.get("autoExecute").getAsBoolean();
        }
    }
    public WMTScript setAutoExecute(boolean autoExecute) {
        this.autoExecute = autoExecute;
        return this;
    }

    public WMTScript setFilename(Path filename) {
        this.filename = filename;
        return this;
    }

    public boolean isAutoExecute() {
        return autoExecute;
    }

    public Path getFilename() {
        return filename;
    }
}
