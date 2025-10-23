/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.extra.addon.addon;

import com.google.common.net.MediaType;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import net.rpTools.aoTool.addon.addon.macro.MacroDetails;
import net.rpTools.aoTool.addon.addon.macro.MacroManager;
import net.rpTools.aoTool.addon.addon.proto.AddOnLibraryEventsDto;
import net.rpTools.aoTool.addon.addon.proto.AddOnStatSheetsDto;
import net.rpTools.aoTool.addon.addon.proto.AddonSlashCommandsDto;
import net.rpTools.aoTool.addon.addon.proto.MTScriptPropertiesDto;
import net.rpTools.aoTool.addon.wrappers.WAddOnLibrary;
import net.rpTools.aoTool.addon.wrappers.WMTScript;
import net.rpTools.aoTool.app.Data;
import net.rpTools.aoTool.asset.Asset;
import net.rpTools.aoTool.asset.Asset.Type;
import net.rpTools.aoTool.asset.AssetManager;
import net.rpTools.aoTool.asset.MD5Key;
import net.rpTools.aoTool.files.JsonUtil;
import net.rpTools.aoTool.files.Util;
import net.rpTools.aoTool.language.I18n;
import net.rpTools.aoTool.system.SystemProps;
import org.apache.commons.io.file.SimplePathVisitor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import javax.swing.filechooser.FileFilter;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Class for importing Drop In Libraries.
 */
public class AddOnLibraryImporter {
    private static final Logger log = LogManager.getLogger(AddOnLibraryImporter.class);
    /**
     * The file extension for add-on library files.
     */
    public static final String DROP_IN_LIBRARY_EXTENSION = ".mtlib";

    /**
     * The name of the add-on library config file.
     */
    public static final String LIBRARY_INFO_FILE = "library.json";

    /**
     * the directory where all the content files in the library live.
     */
    public static final String CONTENT_DIRECTORY = "library/";

    /**
     * The name of the file with the macro script function properties.
     */
    public static final String MACROSCRIPT_PROPERTY_FILE = "mts_properties.json";

    /**
     * The name of the file with event properties.
     */
    public static final String EVENT_PROPERTY_FILE = "events.json";

    /**
     * The name of the file with the stat sheets.
     */
    public static final String STATS_SHEET_FILE = "stat_sheets.json";

    /**
     * The name of the file with the slash commands.
     */
    public static final String SLASH_COMMAND_FILE = "slash_commands.json";

    /**
     * The directory where metadata from the root dir of the zip directory is copied to.
     */
    public static final String METADATA_DIR = "metadata/";

    /**
     * Returns the {@link FileFilter} for add on library files.
     *
     * @return the {@link FileFilter} for add on library files.
     */
    public static FileFilter getAddOnFileFilter() {
        return new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.isDirectory() || isAddOn(f.getName());
            }

            @Override
            public String getDescription() {
                return I18n.getText("file.ext.addOnLib");
            }
        };
    }

    /**
     * Returns if this filename is a valid filename for an add-on library.
     *
     * @param fileName The name of the file to check.
     * @return {@code true} if this is a valid add-on library file name.
     */
    public static boolean isAddOn(String fileName) {
        return fileName.endsWith(DROP_IN_LIBRARY_EXTENSION);
    }

    /**
     * Returns if the asset in the file is an add-on..
     *
     * @param filename The name of the file to check.
     * @return {@code true} if this is contains an add-on.
     */
    public static boolean isAssetFileAddOn(String filename) {
        try (var zip = new ZipFile(new File(filename))) {
            ZipEntry entry = zip.getEntry(LIBRARY_INFO_FILE);
            return entry != null;
        } catch (IOException ioe) {
            return false;
        }
    }

    /**
     * Imports the add-on library from the specified asset.
     *
     * @param asset the asset to use for import.
     * @return the {@link WAddOnLibrary} that was imported.
     * @throws IOException if an error occurs while reading the asset.
     */
    public static WAddOnLibrary importFromAsset(Asset asset) throws IOException {
        // Copy the data to temporary file, its a bit hacky, but it works, and we can't create a
        // ZipFile from anything but a file.
        File tempFile = File.createTempFile("mtlib", "tmp");
        tempFile.deleteOnExit();
        try (OutputStream outputStream = Files.newOutputStream(tempFile.toPath())) {
            outputStream.write(asset.getData());
        }
        return importFromFile(tempFile);
    }

    /**
     * Imports the add-on library from the specified file.
     *
     * @param file the file to use for import.
     * @return the {@link WAddOnLibrary} that was imported.
     * @throws IOException if an error occurs while reading the asset.
     */
    public static WAddOnLibrary importFromFile(File file) throws IOException {
        Gson gson = new Gson();
        ZipFile zip = null;
        final List<File> rootFiles = new ArrayList<>();
        boolean isZip = false;
        try {
            zip = new ZipFile(file);
            isZip = true;
        } catch (IOException ignored) {
            if (!file.isDirectory()) {
                return null;
            } else {
                File[] files = file.listFiles();
                if (files == null || files.length == 0) {
                    return null;
                }
                rootFiles.addAll(List.of(files));
            }
        }

        ZipFile finalZip = zip;
        Function<String, InputStreamReader> getReader = (fileName) -> {
            try {
                if (finalZip != null) {
                    ZipEntry entry = finalZip.getEntry(fileName);
                    if (entry != null) {
                        return new InputStreamReader(finalZip.getInputStream(entry));
                    }
                } else {
                    List<File> filtered = rootFiles.stream().filter(f -> f.getName().equalsIgnoreCase(fileName)).toList();
                    if (!filtered.isEmpty()) {
                        return new FileReader(filtered.getFirst());
                    }
                }
            } catch (IOException ignored) {
            }
            return null;
        };


        InputStreamReader isr;
        JsonReader jsonReader;
        JsonElement element;
        JsonArray array;

        isr = getReader.apply(LIBRARY_INFO_FILE);
        if (isr == null) {
            throw new IOException(I18n.getText("library.error.addOnLibrary.noConfigFile", file.getPath()));
        }
        jsonReader = new JsonReader(isr);
        element = JsonUtil.parse(jsonReader);
        LibraryInfo libraryInfo = gson.fromJson(element, LibraryInfo.class);
        if (libraryInfo == null) {
            throw new IOException(I18n.getText("library.error.addOnLibrary.noConfigFile", file.getPath()));
        }

        // Assets
        Map<String, Pair<MD5Key, Type>> pathAssetMap;
        if (isZip) {
            pathAssetMap = processAssets(libraryInfo.namespace(), zip);
        } else {
            pathAssetMap = processFileAssets(libraryInfo.namespace(), file.toPath());
        }

        // MT MacroScript properties
        Map<String, WMTScript.MTScript> mtsFunctionAssetMap = new HashMap<>();
        isr = getReader.apply(MACROSCRIPT_PROPERTY_FILE);
        if (isr != null) {
            jsonReader = new JsonReader(isr);
            element = JsonUtil.parse(jsonReader);
            if (element.getAsJsonObject().keySet().contains("properties")) {
                array = element.getAsJsonObject().getAsJsonArray("properties");
                array.forEach(el -> {
                    JsonObject jso = el.getAsJsonObject();
                    String filePath = jso.get("filename").getAsString();
                    String fileName = filePath;
                    if (fileName.contains("/") && !fileName.endsWith("/")) {
                        fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
                    }
                    boolean autoExecute = jso.get("autoExecute").getAsBoolean();
                    String description = "";
                    if (jso.keySet().contains("description")) {
                        description = jso.get("description").getAsString();
                    }
                    mtsFunctionAssetMap.put(fileName,
                            new WMTScript.MTScript(
                                    filePath,
                                    autoExecute,
                                    description,
                                    null)
                    );
                });
            }
        }

        // WEvent properties
        Map<String, String> legacyEventNameMap = new HashMap<>();
        Map<String, String> mtScriptEventNameMap = new HashMap<>();
        Map<String, String> jsEventNameMap = new HashMap<>();

        isr = getReader.apply(EVENT_PROPERTY_FILE);
        if (isr != null) {
            jsonReader = new JsonReader(isr);
            element = JsonUtil.parse(jsonReader);
            if (element.getAsJsonObject().keySet().contains("events")) {
                array = element.getAsJsonObject().getAsJsonArray("events");
                array.forEach(el -> {
                    JsonObject jso = el.getAsJsonObject();
                    mtScriptEventNameMap.put(jso.get("name").getAsString(), jso.get("mts").getAsString());
                });
            }
            if (element.getAsJsonObject().keySet().contains("legacyEvents")) {
                array = element.getAsJsonObject().getAsJsonArray("legacyEvents");
                array.forEach(el -> {
                    JsonObject jso = el.getAsJsonObject();
                    if (jso.keySet().contains("mts")) {
                        legacyEventNameMap.put(jso.get("name").getAsString(), jso.get("mts").getAsString());
                    }
                    if (jso.keySet().contains("js")) {
                        jsEventNameMap.put(jso.get("name").getAsString(), jso.get("js").getAsString());
                    }
                });
            }
            if (element.getAsJsonObject().keySet().contains("legacyEvents")) {
                array = element.getAsJsonObject().getAsJsonArray("legacyEvents");
                array.forEach(el -> {
                    JsonObject jso = el.getAsJsonObject();
                    if (jso.keySet().contains("mts")) {
                        legacyEventNameMap.put(jso.get("name").getAsString(), jso.get("mts").getAsString());
                    }
                    if (jso.keySet().contains("js")) {
                        jsEventNameMap.put(jso.get("name").getAsString(), jso.get("js").getAsString());
                    }
                });
            }

        }

        // Stat Sheets
        Set<StatSheet> statSheets = new HashSet<>();
        isr = getReader.apply(STATS_SHEET_FILE);
        if (isr != null) {
            jsonReader = new JsonReader(isr);
            element = JsonUtil.parse(jsonReader);
            if (element.getAsJsonObject().keySet().contains("statSheets")) {
                array = element.getAsJsonObject().getAsJsonArray("statSheets");
                array.forEach(el -> {
                    JsonObject jso = el.getAsJsonObject();
                    try {
                        statSheets.add(new StatSheet(
                                jso.get("name").getAsString(),
                                jso.get("description").getAsString(),
                                new URI("lib://" + libraryInfo.namespace() + "/" + jso.get("entry").getAsString()).toURL(),
                                jso.get("propertyTypes").getAsJsonArray().asList().stream().map(JsonElement::getAsString).collect(Collectors.toSet()),
                                libraryInfo.namespace())
                        );
                    } catch (MalformedURLException | URISyntaxException e) {
                        log.info(e.getLocalizedMessage(), e);
                    }
                });
            }
        }

        // Slash commands
        Map<String, MacroDetails> slashCommands = new HashMap<>();
        isr = getReader.apply(SLASH_COMMAND_FILE);
        if (isr != null) {
            jsonReader = new JsonReader(isr);
            element = JsonUtil.parse(jsonReader);
            if (element.getAsJsonObject().keySet().contains("slashCommands")) {
                array = element.getAsJsonObject().getAsJsonArray("slashCommands");
                array.forEach(el -> {
                    JsonObject jso = el.getAsJsonObject();
                    try {
                        String name = jso.get("name").getAsString();
                        slashCommands.put(name, new MacroDetails(
                                name,
                                jso.get("description").getAsString(),
                                jso.get("command").getAsString(),
                                MacroManager.Scope.ADDON,
                                libraryInfo.namespace(),
                                libraryInfo.name()
                        ));
                    } catch (Exception e) {
                        log.info(e.getLocalizedMessage(), e);
                    }
                });
            }
        }
        Asset asset;
        MD5Key md5Key = null;
        WAddOnLibrary wAddOnLibrary = null;
        // Copy Metadata
        if (isZip) {
            addMetaData(libraryInfo.namespace(), zip, pathAssetMap);
            byte[] data = Files.readAllBytes(file.toPath());
            asset = Type.MTLIB.getFactory().apply(libraryInfo.namespace(), data);

            try {
                zip.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            legacyEventNameMap.putAll(mtScriptEventNameMap);
            legacyEventNameMap.putAll(jsEventNameMap);
            wAddOnLibrary = new WAddOnLibrary(AddOnLibrary.fromDto(
                    md5Key,
                    libraryInfo.toDto(),
                    MTScriptPropertiesDto.parseFrom(gson.toJson(mtsFunctionAssetMap).getBytes(StandardCharsets.UTF_8)),
                    AddOnLibraryEventsDto.parseFrom(gson.toJson(legacyEventNameMap).getBytes(StandardCharsets.UTF_8)),
                    AddOnStatSheetsDto.parseFrom(gson.toJson(statSheets).getBytes(StandardCharsets.UTF_8)),
                    AddonSlashCommandsDto.parseFrom(gson.toJson(slashCommands).getBytes(StandardCharsets.UTF_8)),
                    pathAssetMap
            ));
            try {
                asset = Type.MTLIB.getFactory().apply(libraryInfo.namespace(), wAddOnLibrary.toDto().get().toByteArray());
                md5Key = asset.getMD5Key();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

//            addMetaData(builder.getNamespace(), zip, pathAssetMap);
//            var addOnLib = builder.build();
            // TODO fix this
            byte[] data = Files.readAllBytes(file.toPath());
            asset = Type.MTLIB.getFactory().apply(libraryInfo.namespace(), data);

        }

            if(isZip){
                if(!file.getPath().startsWith(SystemProps.get(SystemProps.TEMP_DIR))) {
//                    wAddOnLibrary.setArchiveLocation(file.toPath());
                }
            } else {
                wAddOnLibrary.setLinkedDirectory(file.toPath());
            }

        addAsset(asset);

        Data.addLoadedLibrary(wAddOnLibrary);
        return wAddOnLibrary;
    }
    private static int count = 0;
    public static WAddOnLibrary importFromClassPath(String path) throws IOException {
        // Copy the data to temporary file, its a bit hacky, but it works, and we can't create a
        // ZipFile from anything but a file.
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        File tempFile = File.createTempFile("mtlib", count + "-tmp");
        count++;
        tempFile.deleteOnExit();

        try (var outputStream = Files.newOutputStream(tempFile.toPath())) {
            try (var inputStream = AddOnLibraryImporter.class.getResourceAsStream(path)) {
                inputStream.transferTo(outputStream);
            }
        }
        return importFromFile(tempFile);
    }

    /**
     * Adds the metadata from the root directory of the zip file to the metadata directory.
     *
     * @param namespace    namespace of the add-on library.
     * @param zip          the zipfile containing the add-on library.
     * @param pathAssetMap the map of asset paths and asset details.
     * @throws IOException
     */
    private static void addMetaData(
            String namespace, ZipFile zip, Map<String, Pair<MD5Key, Type>> pathAssetMap)
            throws IOException {
        var entries = zip.stream().filter(e -> !e.getName().contains("/")).toList();
        for (var entry : entries) {
            String path = METADATA_DIR + entry.getName();
            try (InputStream inputStream = zip.getInputStream(entry)) {
                byte[] bytes = inputStream.readAllBytes();
                MediaType mediaType = Asset.getMediaType(entry.getName(), bytes);
                Asset asset = Type.fromMediaType(mediaType).getFactory().apply(namespace + "/" + path, bytes);
                addAsset(asset);
                pathAssetMap.put(path, new Pair<>(asset.getMD5Key(), asset.getType()));
            }
        }
    }

    /**
     * Reads the assets from the add-on library and adds them to the asset manager.
     *
     * @param namespace the namespace of the add-on library.
     * @param zip       the zipfile containing the add-on library.
     * @return a map of asset paths and asset details.
     * @throws IOException if there is an error reading the assets from the add-on library.
     */
    private static Map<String, Pair<MD5Key, Type>> processAssets(String namespace, ZipFile zip)
            throws IOException {
        var pathAssetMap = new HashMap<String, Pair<MD5Key, Type>>();
        var entries =
                zip.stream()
                        .filter(e -> !e.isDirectory())
                        .filter(e -> e.getName().startsWith(CONTENT_DIRECTORY))
                        .toList();
        for (var entry : entries) {
            String path = entry.getName().substring(Math.min(
                    entry.getName().length(),
                    entry.getName().lastIndexOf('/') + 1));
            MediaType mediaType = MediaType.PLAIN_TEXT_UTF_8;
            if(path.contains(".")){
                String extension = Util.getFileExtension(entry.getName());
                mediaType = Asset.getMediaTypeFromFileExtension(extension);
            }
            try (InputStream inputStream = zip.getInputStream(entry)) {
                byte[] bytes = inputStream.readAllBytes();
                if(mediaType.equals(MediaType.PLAIN_TEXT_UTF_8)) {
                    mediaType = Asset.getMediaType(entry.getName(), bytes);
                }
                Asset asset =
                        Type.fromMediaType(mediaType).getFactory().apply(namespace + "/" + path, bytes);
                addAsset(asset);
                pathAssetMap.put(path, new Pair<>(asset.getMD5Key(), asset.getType()));
            }
        }
        return pathAssetMap;
    }

    private static Map<String, Pair<MD5Key, Type>> processFileAssets(String namespace, Path rootFolder)
            throws IOException {
        var pathAssetMap = new HashMap<String, Pair<MD5Key, Type>>();
        int rootLength = rootFolder.getNameCount();
        var entries = Files.walkFileTree(rootFolder, new SimplePathVisitor() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relativePath = file.subpath(rootLength, file.getNameCount() - 1);
                try (InputStream is = Files.newInputStream(file, StandardOpenOption.READ)) {
                    byte[] bytes = is.readAllBytes();
                    MediaType mediaType = Asset.getMediaType(file.getFileName().toString(), bytes);
                    Asset asset = Type.fromMediaType(mediaType).getFactory().apply(namespace + "/" + relativePath, bytes);
                    addAsset(asset);
                    pathAssetMap.put(relativePath.toString(), new Pair<>(asset.getMD5Key(), asset.getType()));
                }
                return super.visitFile(file, attrs);
            }
        });
        return pathAssetMap;
    }

    /**
     * Adds the {@link Asset} to the {@link AssetManager} if it does not already exist.
     *
     * @param asset the {@link Asset} to add.
     */
    private static void addAsset(Asset asset) {
        if (!AssetManager.hasAsset(asset)
                || AssetManager.getAsset(asset.getMD5Key()).getData().length == 0) {
            AssetManager.putAsset(asset);
        }
    }
}
