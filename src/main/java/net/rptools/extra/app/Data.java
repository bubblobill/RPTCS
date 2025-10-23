package net.rptools.extra.app;

import com.google.gson.*;
import net.rpTools.aoTool.addon.addon.AddOnLibraryImporter;
import net.rpTools.aoTool.addon.wrappers.WAddOnLibrary;
import net.rpTools.aoTool.app.preference.Preferences;
import net.rpTools.aoTool.files.FileIcons;
import net.rpTools.aoTool.files.JsonUtil;
import net.rpTools.aoTool.files.PathNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class Data {
    private static final Logger log = LogManager.getLogger(Data.class);
    public static final Map<String, Path> TEMP_FOLDERS = new HashMap<>();
    public static final List<WAddOnLibrary> LOADED_LIBRARIES = new ArrayList<>();
    public static AppFrame AppFrame;

    static {
        TEMP_FOLDERS.put("addOnEditor", Constants.TEMP_FOLDER_PATH);
    }
    public static void addLoadedLibrary(WAddOnLibrary WAddOnLibrary){
        if(!LOADED_LIBRARIES.contains(WAddOnLibrary)){
            LOADED_LIBRARIES.add(WAddOnLibrary);
        }
    }
    public static void backup(){
        JsonArray jsa = new JsonArray(LOADED_LIBRARIES.stream().distinct().toList().size());
        for(WAddOnLibrary ao: LOADED_LIBRARIES.stream().distinct().toList()){
            JsonObject jso = new JsonObject();
            try {
                jso.add("name", new JsonPrimitive(ao.getLibraryInfo().get().name()));
                jso.add("namespace", new JsonPrimitive(ao.getLibraryInfo().get().namespace()));
                jso.add("version", new JsonPrimitive(ao.getLibraryInfo().get().version()));
                try {
                    jso.add("file",
                            ao.getArchiveLocation() == null ?
                                    JsonNull.INSTANCE :
                                    new JsonPrimitive(ao.getArchiveLocation().toUri().toString()));
                } catch (Exception e){
                    jso.add("file", JsonNull.INSTANCE);
                }
                try {
                jso.add("folder",
                        ao.getLinkedDirectory() == null ?
                                JsonNull.INSTANCE :
                                new JsonPrimitive(ao.getLinkedDirectory().toUri().toString()));
                } catch (Exception e){
                    jso.add("folder", JsonNull.INSTANCE);
                }
                jsa.add(jso);
            } catch (InterruptedException|ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        if(!jsa.isEmpty()) {
            Preferences.set(Preferences.Key.LAST_LOADED_ADDONS, jsa.toString());
        }
    }
    public static void restore(){
        String lastLoadedAddons = Preferences.get(Preferences.Key.LAST_LOADED_ADDONS);
        if(lastLoadedAddons == null || lastLoadedAddons.isEmpty()){
            return;
        }
        JsonArray jsa = JsonUtil.parse(lastLoadedAddons).getAsJsonArray();
        for(JsonElement el: jsa){
            try {
                String file = null, folder = null;
                URI fileUri = null, folderUri = null;
                WAddOnLibrary ao = null;
                JsonObject jso = el.getAsJsonObject();
                if(jso.keySet().contains("file")){
                    JsonElement element = jso.get("file");
                    if(element != JsonNull.INSTANCE) {
                        file = element.getAsString();
                        if (!file.isEmpty()) {
                            fileUri = URI.create(file);
                            ao = AddOnLibraryImporter.importFromFile(Paths.get(fileUri).toFile());
                        }
                    }
                }
                if(jso.keySet().contains("folder")){
                    JsonElement element = jso.get("folder");
                    if(element != JsonNull.INSTANCE) {
                        folder = element.getAsString();
                        if (!folder.isEmpty()) {
                            folderUri = URI.create(folder);
                            if (ao == null) {
                                ao = AddOnLibraryImporter.importFromFile(Paths.get(folderUri).toFile());
                            }
                        }
                    }
                }
                if(ao != null) {
                    ao.setArchiveLocation(fileUri == null ? null : Paths.get(fileUri));
                    ao.setLinkedDirectory(folderUri == null ? null : Paths.get(folderUri));
                    addLoadedLibrary(ao);
                }
            } catch (IOException|IllegalArgumentException iae){
                log.info("error restoring add-on.\n{}", iae.getLocalizedMessage(), iae);
            }
        }
    }
    public static Supplier<Path> mruAddOn = () -> {
        List<String> mru = Preferences.getList(Preferences.Key.MRU_ADD_ONS);
        if(mru != null && !mru.isEmpty()){
            return Paths.get(mru.getLast());
        } else {
            return null;
        }
    };
    private static final Supplier<PathNode> createModelPathNode = () -> {
        try {
            /*
    Structure
    library.json            <-- Configuration information for the add-on library
    mts_properties.json     <-- Properties for macro script functions in library
    events.json             <-- WEvent definition for functions in the library
    library/                <-- Content of the library
    library/public          <-- Content of the library acessable via `lib:// URI`
    library/mtscript        <-- MTSCript files
    library/mtscript/public <-- MTSCript files that can be called via `[macro(): ]` outside of the library.
     */
            PathNode structure = new PathNode("/", true, FileIcons.menuItemArrowIcon);
            // root files
            PathNode libProps  = new PathNode("library.json", false, FileIcons.fileIcon);
            PathNode mtsProps  = new PathNode("mts_properties.json", false, FileIcons.fileIcon);
            PathNode evtProps  = new PathNode("events.json", false, FileIcons.fileIcon);
            PathNode sstProps  = new PathNode("stat_sheets.json", false, FileIcons.fileIcon);
            // directories
            PathNode library   = new PathNode("library", true, FileIcons.directoryIcon);
            PathNode mtScript  = new PathNode("mtscript", true, FileIcons.directoryIcon);
            PathNode pubScript = new PathNode("public", true, FileIcons.directoryIcon);
            PathNode pub       = new PathNode("public", true, FileIcons.directoryIcon);
            PathNode sheets    = new PathNode("sheets", true, FileIcons.directoryIcon);

            structure.add(libProps);
            structure.add(mtsProps);
            structure.add(evtProps);
            structure.add(sstProps);

            structure.add(library);
            library.add(mtScript);
            mtScript.add(pubScript);
            library.add(pub);
            pub.add(sheets);

            return structure;
        } catch (Exception e){
            log.info(e.getMessage(), e);
        }
        return null;
    };
    private static PathNode modelTreeRootNode = null;
    public static PathNode getModelTreeRootNode(){
        if(modelTreeRootNode == null){
            modelTreeRootNode = createModelPathNode.get();
        }
        return new PathNode(modelTreeRootNode);}
    public static class AddOnLocations {
        String keyString;
        Path archivePath = null;
        Path directoryPath = null;

    }
}