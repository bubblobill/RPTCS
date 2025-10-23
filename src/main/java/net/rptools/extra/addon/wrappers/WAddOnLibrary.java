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
package net.rptools.extra.addon.wrappers;

import net.rpTools.aoTool.addon.addon.AddOnLibrary;
import net.rpTools.aoTool.addon.addon.LibraryInfo;
import net.rpTools.aoTool.addon.addon.StatSheet;
import net.rpTools.aoTool.addon.addon.Token;
import net.rpTools.aoTool.addon.addon.data.LibraryData;
import net.rpTools.aoTool.addon.addon.macro.MTScriptMacroInfo;
import net.rpTools.aoTool.addon.addon.macro.MacroDetails;
import net.rpTools.aoTool.addon.addon.macro.MacroManager;
import net.rpTools.aoTool.addon.addon.macro.MapToolMacroContext;
import net.rpTools.aoTool.addon.addon.proto.*;
import net.rpTools.aoTool.asset.Asset;
import net.rpTools.aoTool.asset.Asset.Type;
import net.rpTools.aoTool.asset.MD5Key;
import net.rpTools.aoTool.files.PathNode;
import net.rpTools.aoTool.files.Prompt;
import net.rpTools.aoTool.language.I18n;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Class wrapper and utilities for {@link AddOnLibrary}.
 */
@SuppressWarnings("unused")
public class WAddOnLibrary implements Serializable {
    /**
     * Logger instance for this class.
     */
    private static final Logger logger = LogManager.getLogger(WAddOnLibrary.class);
    /**
     * The name of the event for first time initialization.
     */
    private static final String FIRST_INIT_EVENT = "onFirstInit";
    /**
     * The name of the event for initialization.
     */
    private static final String INIT_EVENT = "onInit";
    /**
     * The prefix for the name of the JavaScript context for this addon.
     */
    private static final String JS_CONTEXT_PREFIX = "addon:";
    /**
     * The directory where the files exposed URI are stored.
     */
    private static final String URL_PUBLIC_DIR = "public/";
    /**
     * The directory where MT MacroScripts are stored.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private static final String MT_SCRIPT_DIR = "mtscript/";
    /**
     * The directory where public MT MacroScripts are stored.
     */
    private static final String MT_SCRIPT_PUBLIC_DIR = "public/";
    /**
     * Field names for maps that are not gettable.
     */
    private static final List<String> MAP_FIELD_NAMES = List.of("pathAssetMap", "urlPathAssetMap", "jsEventNameMap", "legacyEventNameMap", "mtScriptEventNameMap", "mtsFunctionAssetMap", "slashCommands");

    /**
     * Record used to store information about the MacroScript functions for this library.
     */
    public record MTScript(String path, boolean autoExecute, String description, MD5Key md5Key) {

    }
    /**
     * The Asset for the library read me file.
     */
    private String readMeFile;

    /**
     * The Asset for the library license file.
     */
    private String licenseFile;

    /**
     * The name of the add-on library.
     */
    private String name;

    /**
     * The version of the add-on library.
     */
    private String version;

    /**
     * The website for the add-on library.
     */
    private String website;

    /**
     * The authors for the add-on library.
     */
    private String[] authors;

    /**
     * The git url of the add-on library.
     */
    private String gitUrl;

    /**
     * The license for the add-on library.
     */
    private String license;

    /**
     * The namespace for the add-on library.
     */
    private String namespace;

    /**
     * The description for the add-on library.
     */
    private String description;

    /**
     * The short description for the add-on library.
     */
    private String shortDescription;

    /**
     * if the add-on library allows URI access or not.
     */
    private boolean allowsUriAccess;

    /**
     * The mapping between paths and asset information.
     */
    private Map<String, Pair<MD5Key, Type>> pathAssetMap = new HashMap<>();

    /**
     * The mapping between url paths and asset information.
     */
    private Map<String, Pair<MD5Key, Type>> urlPathAssetMap = new HashMap<>();

    /**
     * The mapping between MTScript function paths and asset information.
     */
    private Map<String, MTScript> mtsFunctionAssetMap = new HashMap<>();

    /**
     * The mapping between MTScript function paths and legacy events.
     */
    private Map<String, String> legacyEventNameMap = new HashMap<>();

    /**
     * The mapping between MTScript function paths and non legacy events.
     */
    private Map<String, String> mtScriptEventNameMap = new HashMap<>();

    /**
     * The mapping between JavaScript script paths and non legacy events.
     */
    private Map<String, String> jsEventNameMap = new HashMap<>();

    /**
     * The Stat Sheets defined by the add-on library.
     */
    private Set<StatSheet> statSheets = new HashSet<>();

    /**
     * The mapping between slash command names and slash command details.
     */
    private Map<String, MacroDetails> slashCommands = new HashMap<>();

    /**
     * The ID of the asset for the whole of the add-on Library.
     */
    private MD5Key assetKey;
    /**
     * The name of the JavaScript context for the add-on library.
     */
    private String jsContextName;
    /**
     * Path to the ".mtlib" or ".zip" file.
     */
    private Path archiveLocation = null;
    /**
     * The information about the add-on library.
     */
    private LibraryInfo libraryInfo;
    /**
     * Folder that contains Add-On content
     */
    private Path linkedDirectory = null;

    /**
     * Tree-node that reflects the file structure
     */
    private PathNode pathNode = null;

    public void setLinkedDirectory(Path folder) {
        this.linkedDirectory = folder;
    }
    public Path getLinkedDirectory() {
        return linkedDirectory;
    }
    public void setArchiveLocation(Path archive) {
        this.archiveLocation = archive;
    }
    public Path getArchiveLocation() {
        return archiveLocation;
    }
    public PathNode getPathNode(){
        return pathNode;
    }
    public void setPathNode(PathNode pathNode){
        this.pathNode = pathNode;
    }
    private AddOnLibrary delegate;

    /**
     * Creates a new Drop In Library from the given {@link AddOnLibraryDto}, {@link MTScriptPropertiesDto},
     * and file path assets map.
     *
     * @param dto              The Drop In Libraries Data Transfer Object.
     * @param mtsDto           The MTScript Properties Data Transfer Object.
     * @param eventsDto        The Events Data Transfer Object.
     * @param statSheetsDto    The Popo-up Data Transfer Object.
     * @param slashCommandsDto The Slash Commands Data Transfer Object.
     * @param pathAssetMap     mapping of paths in the library to {@link MD5Key}s and {@link Type}s.
     */
    public WAddOnLibrary(
            MD5Key libraryAssetKey,
            AddOnLibraryDto dto,
            MTScriptPropertiesDto mtsDto,
            AddOnLibraryEventsDto eventsDto,
            AddOnStatSheetsDto statSheetsDto,
            AddonSlashCommandsDto slashCommandsDto,
            Map<String, Pair<MD5Key, Type>> pathAssetMap) {
        super();
        try {
            Constructor<AddOnLibrary> constructor = AddOnLibrary.class.getDeclaredConstructor(
                    MD5Key.class,
                    AddOnLibraryDto.class,
                    MTScriptPropertiesDto.class,
                    AddOnLibraryEventsDto.class,
                    AddOnStatSheetsDto.class,
                    AddonSlashCommandsDto.class,
                    Map.class
            );
            constructor.setAccessible(true);
            delegate = ConstructorUtils.invokeConstructor(AddOnLibrary.class, libraryAssetKey, dto, mtsDto, eventsDto, statSheetsDto, slashCommandsDto, pathAssetMap);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 InstantiationException e) {
            throw new RuntimeException(e);
        }

        Objects.requireNonNull(dto, I18n.getText("library.error.invalidDefinition"));
        name = Objects.requireNonNull(dto.getName(), I18n.getText("library.error.emptyName"));
        version = Objects.requireNonNull(dto.getVersion(), I18n.getText("library.error.emptyVersion", name));
        website = Objects.requireNonNull(dto.getWebsite(), "");
        authors = dto.getAuthorsList().toArray(String[]::new);
        gitUrl = dto.getGitUrl();
        license = dto.getLicense();
        namespace = dto.getNamespace();
        description = dto.getDescription();
        shortDescription = dto.getShortDescription();
        this.pathAssetMap.putAll(Map.copyOf(pathAssetMap));
        allowsUriAccess = dto.getAllowsUriAccess();
        assetKey = libraryAssetKey;

        var urlsMap = new HashMap<String, Pair<MD5Key, Type>>();
        var mtsMap = new HashMap<String, MTScript>();

        var autoExecSet = new HashSet<String>();
        var descriptionMap = new HashMap<String, String>();

        for (var properties : mtsDto.getPropertiesList()) {
            var path = MT_SCRIPT_DIR + properties.getFilename();
            if (properties.getAutoExecute()) {
                autoExecSet.add(path);
            }

            descriptionMap.put(path, properties.getDescription());
        }

        for (var s : statSheetsDto.getStatSheetsList()) {
            try {
                statSheets.add(
                        new StatSheet(
                                s.getName(),
                                s.getDescription(),
                                new URI("lib://" + namespace + "/" + s.getEntry()).toURL(),
                                new HashSet<>(s.getPropertyTypesList()),
                                namespace));
            } catch (Exception e) {
                Prompt.showError(I18n.getText("library.error.addOn.sheet", namespace, s.getName()), e);
            }
        }

        eventsDto.getEventsList().stream()
                .filter(e -> !e.getMts().isEmpty())
                .forEach(e -> mtScriptEventNameMap.put(e.getName(), e.getMts()));

        eventsDto.getEventsList().stream()
                .filter(e -> !e.getJs().isEmpty())
                .forEach(e -> jsEventNameMap.put(e.getName(), e.getJs()));

        eventsDto.getLegacyEventsList().stream()
                .filter(e -> !e.getMts().isEmpty())
                .forEach(e -> legacyEventNameMap.put(e.getName(), e.getMts()));

        for (var entry : this.pathAssetMap.entrySet()) {
            String path = entry.getKey();
            if (path.startsWith(URL_PUBLIC_DIR)) {
                urlsMap.put(path.substring(URL_PUBLIC_DIR.length()), entry.getValue());
            } else if (path.startsWith(MT_SCRIPT_DIR)) {
                if (path.toLowerCase().endsWith(".mts")) {
                    String name = path.substring(MT_SCRIPT_DIR.length(), path.length() - 4);
                    mtsMap.put(
                            name,
                            new MTScript(
                                    name,
                                    autoExecSet.contains(path),
                                    descriptionMap.getOrDefault(path, ""),
                                    entry.getValue().getValue0()));
                }
            }
        }

        urlPathAssetMap.putAll(Collections.unmodifiableMap(urlsMap));
        mtsFunctionAssetMap.putAll(Collections.unmodifiableMap(mtsMap));

        licenseFile = dto.getLicenseFile();
        readMeFile = dto.getReadMeFile();

        jsContextName = JS_CONTEXT_PREFIX + namespace;

        libraryInfo =
                new LibraryInfo(
                        name,
                        namespace,
                        version,
                        website,
                        gitUrl,
                        authors,
                        license,
                        description,
                        shortDescription,
                        allowsUriAccess,
                        readMeFile.isEmpty() ? null : readMeFile,
                        licenseFile.isEmpty() ? null : licenseFile);

        for (var s : slashCommandsDto.getSlashCommandsList()) {
            slashCommands.put(
                    s.getName(),
                    new MacroDetails(
                            s.getName(),
                            s.getCommand(),
                            s.getDescription(),
                            MacroManager.Scope.ADDON,
                            namespace,
                            name));
        }
    }

    public WAddOnLibrary(AddOnLibrary addOnLibrary) {
        this.delegate = addOnLibrary;
        try {
            this.assetKey = addOnLibrary.getAssetKey();
            this.libraryInfo = getLibraryInfo().get();
            this.allowsUriAccess = libraryInfo.allowsUrlAccess();
            this.authors = libraryInfo.authors();
            this.description = libraryInfo.description();
            this.shortDescription = libraryInfo.shortDescription();
            this.gitUrl = libraryInfo.gitUrl();
            this.license = libraryInfo.license();
            this.licenseFile = libraryInfo.licenseFile();
            this.name = libraryInfo.name();
            this.namespace = libraryInfo.namespace();
            this.readMeFile = libraryInfo.readMeFile();
            this.website = libraryInfo.website();
            this.version = libraryInfo.version();
            this.jsContextName =  JS_CONTEXT_PREFIX + this.namespace;

            MAP_FIELD_NAMES.forEach(name -> {
                try {
                    Field field = addOnLibrary.getClass().getDeclaredField(name);
                    field.setAccessible(true);
                    Object value = field.get(null);
                    if (value instanceof Set<?> set) {
                        set.forEach(v -> {
                                    if (v instanceof StatSheet sheet) {
                                        statSheets.add(sheet);
                                    }
                                }
                        );
                    }
                    if (value instanceof Map<?, ?> map) {
                        if (!map.isEmpty()) {
                            Map.Entry<?, ?> mapEntry = map.entrySet().stream().toList().getFirst();
                            if (mapEntry.getKey() instanceof String) {
                                map.forEach((key1, value1) -> {
                                    if (key1 instanceof String key) {
                                        if (value1 instanceof MacroDetails macroDetails) {
                                            slashCommands.put(key, macroDetails);
                                        } else if (value1 instanceof MTScript mtScript) {
                                            mtsFunctionAssetMap.put(key, mtScript);
                                        } else if (value1 instanceof String string) {
                                            switch (name) {
                                                case "jsEventNameMap" -> jsEventNameMap.put(key, string);
                                                case "legacyEventNameMap" -> legacyEventNameMap.put(key, string);
                                                case "mtScriptEventNameMap" -> mtScriptEventNameMap.put(key, string);
                                            }
                                        } else if (value1 instanceof Pair<?, ?> pair) {
                                            if (pair.getValue0() instanceof MD5Key md5Key) {
                                                if (pair.getValue1() instanceof Type type) {
                                                    Pair<MD5Key, Type> p = new Pair<>(md5Key, type);
                                                    if (name.equals("pathAssetMap")) {
                                                        pathAssetMap.put(key, p);
                                                    } else {
                                                        urlPathAssetMap.put(key, p);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                } catch (IllegalAccessException|NoSuchFieldException e) {
                    logger.info("{}: {}", name, e.getLocalizedMessage(), e);
                }
            });
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static WAddOnLibrary fromDto(MD5Key libraryAssetKey, AddOnLibraryDto dto, MTScriptPropertiesDto mtsDto, AddOnLibraryEventsDto eventsDto, AddOnStatSheetsDto statSheetsDto, AddonSlashCommandsDto slashCommandsDto, Map<String, Pair<MD5Key, Type>> pathAssetMap) {
        return new WAddOnLibrary(AddOnLibrary.fromDto(libraryAssetKey, dto, mtsDto, eventsDto, statSheetsDto, slashCommandsDto, pathAssetMap));
    }

    public CompletableFuture<String> getDescription() {
        return delegate.getDescription();
    }

    public CompletableFuture<String> getShortDescription() {
        return delegate.getShortDescription();
    }

    public CompletableFuture<Boolean> allowsUriAccess() {
        return delegate.allowsUriAccess();
    }

    public CompletableFuture<LibraryInfo> getLibraryInfo() {
        return delegate.getLibraryInfo();
    }

    public CompletableFuture<Boolean> locationExists(URL location) throws IOException {
        return delegate.locationExists(location);
    }

    public CompletableFuture<InputStream> read(URL location) throws IOException {
        return delegate.read(location);
    }

    public CompletableFuture<Optional<MTScriptMacroInfo>> getMTScriptMacroInfoForUriPath(String macroPath) {
        return delegate.getMTScriptMacroInfoForUriPath(macroPath);
    }

    public Set<MacroDetails> getSlashCommands() {
        return delegate.getSlashCommands();
    }

    public CompletableFuture<Boolean> isAsset(URL location) {
        return delegate.isAsset(location);
    }

    public CompletableFuture<Optional<Asset>> getReadMeAsset() {
        return delegate.getReadMeAsset();
    }

    public CompletableFuture<Optional<MD5Key>> getAssetKey(URL location) {
        return delegate.getAssetKey(location);
    }

    public CompletableFuture<Optional<MTScriptMacroInfo>> getMTScriptMacroInfo(String macroName) {
        return delegate.getMTScriptMacroInfo(macroName);
    }

    public CompletableFuture<Optional<Asset>> getLicenseAsset() {
        return delegate.getLicenseAsset();
    }

    public CompletableFuture<Optional<MTScriptMacroInfo>> getPrivateMacroInfo(String macroName) {
        return delegate.getPrivateMacroInfo(macroName);
    }

    public CompletableFuture<List<String>> getAllFiles() {
        return delegate.getAllFiles();
    }

    public void cleanup() {
        delegate.cleanup();
    }

    public CompletableFuture<String> getVersion() {
        return delegate.getVersion();
    }

    public CompletableFuture<String> readAsString(URL location) throws IOException {
        return delegate.readAsString(location);
    }

    public CompletableFuture<LibraryData> getLibraryData() {
        return delegate.getLibraryData();
    }

    public CompletableFuture<Optional<String>> getLegacyEventHandlerName(String eventName) {
        return delegate.getLegacyEventHandlerName(eventName);
    }

    public boolean canMTScriptAccessPrivate(MapToolMacroContext context) {
        return delegate.canMTScriptAccessPrivate(context);
    }

    public CompletableFuture<Optional<Token>> getAssociatedToken() {
        return delegate.getAssociatedToken();
    }

    public CompletableFuture<String> getWebsite() {
        return delegate.getWebsite();
    }



    public String getReadMeFile() {
        return readMeFile;
    }

    public void setReadMeFile(String readMeFile) {
        this.readMeFile = readMeFile;
    }

    public String getLicenseFile() {
        return licenseFile;
    }

    public void setLicenseFile(String licenseFile) {
        this.licenseFile = licenseFile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String[] getAuthors() {
        return authors;
    }

    public void setAuthors(String[] authors) {
        this.authors = authors;
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public boolean isAllowsUriAccess() {
        return allowsUriAccess;
    }

    public void setAllowsUriAccess(boolean allowsUriAccess) {
        this.allowsUriAccess = allowsUriAccess;
    }

    public Map<String, Pair<MD5Key, Type>> getPathAssetMap() {
        return pathAssetMap;
    }

    public void setPathAssetMap(Map<String, Pair<MD5Key, Type>> pathAssetMap) {
        this.pathAssetMap = pathAssetMap;
    }

    public Map<String, Pair<MD5Key, Type>> getUrlPathAssetMap() {
        return urlPathAssetMap;
    }

    public void setUrlPathAssetMap(Map<String, Pair<MD5Key, Type>> urlPathAssetMap) {
        this.urlPathAssetMap = urlPathAssetMap;
    }

    public Map<String, MTScript> getMtsFunctionAssetMap() {
        return mtsFunctionAssetMap;
    }

    public void setMtsFunctionAssetMap(Map<String, MTScript> mtsFunctionAssetMap) {
        this.mtsFunctionAssetMap = mtsFunctionAssetMap;
    }

    public Map<String, String> getLegacyEventNameMap() {
        return legacyEventNameMap;
    }

    public void setLegacyEventNameMap(Map<String, String> legacyEventNameMap) {
        this.legacyEventNameMap = legacyEventNameMap;
    }

    public Map<String, String> getMtScriptEventNameMap() {
        return mtScriptEventNameMap;
    }

    public void setMtScriptEventNameMap(Map<String, String> mtScriptEventNameMap) {
        this.mtScriptEventNameMap = mtScriptEventNameMap;
    }

    public Map<String, String> getJsEventNameMap() {
        return jsEventNameMap;
    }

    public void setJsEventNameMap(Map<String, String> jsEventNameMap) {
        this.jsEventNameMap = jsEventNameMap;
    }

    public Set<StatSheet> getStatSheets() {
        return statSheets;
    }

    public void setStatSheets(Set<StatSheet> statSheets) {
        this.statSheets = statSheets;
    }

    public void setSlashCommands(Map<String, MacroDetails> slashCommands) {
        this.slashCommands = slashCommands;
    }

    public MD5Key getAssetKey() {
        return assetKey;
    }

    public void setAssetKey(MD5Key assetKey) {
        this.assetKey = assetKey;
    }

    public String getJsContextName() {
        return jsContextName;
    }

    public void setJsContextName(String jsContextName) {
        this.jsContextName = jsContextName;
    }

    public void setLibraryInfo(LibraryInfo libraryInfo) {
        this.libraryInfo = libraryInfo;
    }

    public AddOnLibrary getDelegate() {
        return delegate;
    }

    public void setDelegate(AddOnLibrary delegate) {
        this.delegate = delegate;
    }

    public CompletableFuture<AddOnLibraryListDto> toDto() {

        return CompletableFuture.supplyAsync(
                () -> {
                    var dto = AddOnLibraryListDto.newBuilder();
                        var detailDto = AddOnLibraryDto.newBuilder();
                        detailDto.setName(delegate.getName().join());
                        detailDto.setVersion(delegate.getVersion().join());
                        detailDto.setWebsite(delegate.getWebsite().join());
                        detailDto.setGitUrl(delegate.getGitUrl().join());
                        detailDto.addAllAuthors(Arrays.asList(delegate.getAuthors().join()));
                        detailDto.setLicense(delegate.getLicense().join());
                        detailDto.setNamespace(delegate.getNamespace().join());
                        detailDto.setDescription(delegate.getDescription().join());
                        detailDto.setShortDescription(delegate.getShortDescription().join());
                    return dto.build();
                });
    }
}