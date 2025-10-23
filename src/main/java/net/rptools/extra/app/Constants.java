package net.rptools.extra.app;

import net.rpTools.aoTool.files.FileUtils;
import net.rpTools.aoTool.system.SystemProps;

import javax.naming.directory.BasicAttribute;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Constants {
    public static String RESOURCE_PATH = "/net/rpTools/aoTool/";
    public static String RESOURCE_SAMPLES_PATH = RESOURCE_PATH + "samples/";
    public static String RESOURCE_SAMPLES_MODELS_PATH = RESOURCE_PATH + "samples/emptyAddOn/";
    public static String RESOURCE_IMAGES_PATH = RESOURCE_PATH + "images/toolbarButtonGraphics/";
    public static String RESOURCE_I18N_PATH = RESOURCE_PATH + "language/i18n";
    public static final Path USER_HOME = Path.of(System.getProperty(SystemProps.USER_HOME));
    public static final List<String> CONFIG_FILES = List.of("events.json","library.json","mts_properties.json","stat_sheets.json");
    public static final Color TRANSPARENT =  new Color(0.5f,0.5f,0.5f, 0);

    public static final Set<BasicAttribute> BASIC_ATTRIBUTE_SET = Set.of(new BasicAttribute("create", "true"), new BasicAttribute("read", "true"), new BasicAttribute("write", "true"));

    public static final Set<PosixFilePermission> POSIX_PERMISSIONS = PosixFilePermissions.fromString("rw-rw-rw-");
    public static final FileAttribute<Set<PosixFilePermission>> POSIX_FILE_ATTRIBUTES = PosixFilePermissions.asFileAttribute(POSIX_PERMISSIONS);
    public static final Path TEMP_FOLDER_PATH = Path.of(SystemProps.get(SystemProps.TEMP_DIR), "addOnEditor");
    public static final File TEMP_FOLDER = FileUtils.createFolder(TEMP_FOLDER_PATH);
    public static final Map<String,?> FILESYSTEM_ENV = Map.of("create", "true", "defaultPermissions", Constants.POSIX_PERMISSIONS);

    /** Moving all the patterns together to ensure that we don't forget any */
    public static final Pattern AUTOEXEC_PATTERN = Pattern.compile("([^:]*)://(.*)/([^/]*)/([^?]*)(?:\\?(.*))?");
    public static final Pattern TOOLTIP_PATTERN = Pattern.compile("([^:]*)://(.*)/([^/]*)/([^?]*)(?:\\?(.*))?");
    /** Pattern to distinguish a link (group 1) from its data (group 2). */
    public static final Pattern LINK_DATA_PATTERN = Pattern.compile("((?s)[^:]*://.*/[^/]*/[^?]*\\?)(.*)?");
    public static final Pattern MACROLINK_PATTERN = Pattern.compile("(?s)([^:]*)://(.*)/([^/]*)/([^?]*)(?:\\?(.*))?");
    public static final Pattern TRIM_EXTENSION_PATTERN = Pattern.compile("^(.*)\\.([^\\.]*)$");

    public static final String ADD_ON_EXTENSION = "mtlib";
    public static final String[] MT_FILE_TYPES = new String[]{ "cmpgn", ADD_ON_EXTENSION, "mtmacro", "mtmacset", "mtprops", "mttable", "rpmap", "rptok"};
    public static final FileFilter MT_FILTER = new FileNameExtensionFilter("MapTool Files", MT_FILE_TYPES);
    public static final FileFilter ADD_ON_CONTENT = new FileNameExtensionFilter("Add-On Files", ADD_ON_EXTENSION, "css", "hbs", "html", "js", "json", "md", "txt");
    public static final FileFilter ADD_ON_FILES = new FileNameExtensionFilter("Add-On Files", ADD_ON_EXTENSION, "zip");
    public static final FileFilter IMAGE_FILES = new FileNameExtensionFilter("Image", "gif","jpg","jpeg","png","tiff","webp");

    public static final List<String> licenses = new ArrayList<>(){{
        add("");
        add("Academic Free License");
        add("Affero General Public License");
        add("Apache License");
        add("Apple Public Source License");
        add("Artistic License");
        add("Beerware");
        add("BSD License");
        add("Boost Software License");
        add("Creative Commons Zero");
        add("CC BY");
        add("CC BY-SA");
        add("CeCILL");
        add("Common Development and Distribution License");
        add("Common Public License");
        add("Cryptix General License");
        add("Eclipse Public License");
        add("Educational Community License");
        add("European Union Public Licence");
        add("FreeBSD");
        add("GNU Affero General Public License");
        add("GNU General Public License");
        add("GNU Lesser General Public License");
        add("IBM Public License");
        add("ISC license");
        add("LaTeX Project Public License");
        add("Microsoft Public License");
        add("MIT license / X11 license");
        add("Mozilla Public License");
        add("Netscape Public License");
        add("Open Software License");
        add("OpenSSL license");
        add("PHP License");
        add("Python Software Foundation License");
        add("Q Public License");
        add("Ruby License");
        add("Sleepycat License");
        add("Unlicense");
        add("W3C Software Notice and License");
        add("Do What The Fuck You Want To Public License (WTFPL)");
        add("XCore Open Source License");
        add("XFree86 1.1 License");
        add("zlib/libpng license");
    }};
}
