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
package net.rptools.maptool.client;

import io.sentry.*;
import javafx.application.Platform;
import net.rptools.lib.*;
import net.rptools.lib.image.ThumbnailManager;
import net.rptools.lib.net.RPTURLStreamHandlerFactory;

import net.rptools.maptool.client.ui.theme.ThemeSupport;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.model.library.url.LibraryURLStreamHandler;
import net.rptools.maptool.transfer.AssetTransferManager;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.*;

/**
 *
 */
public class MapTool {
    private MapTool() {
        throw new Error("cannot construct MapTool object!");
    }

    private static final Logger log = LogManager.getLogger(MapTool.class);
    /**
     * Specifies the properties file that holds sound information. Only two sounds currently:
     * <b>Dink</b> and <b>Clink</b>.
     */
    private static final String SOUND_PROPERTIES = "net/rptools/maptool/client/sounds.properties";
    public static final String SND_INVALID_OPERATION = "invalidOperation";
    private static String clientId = "";
    private static final Dimension THUMBNAIL_SIZE = new Dimension(80,80);
    public static Dimension getThumbnailSize() {
        return THUMBNAIL_SIZE;
    }
    private static ThumbnailManager thumbnailManager;
    private static String version = "DEVELOPMENT";
    private static String vendor = "RPTools"; // Default, will get from JAR Manifest during normal
    // runtime
    private static AssetTransferManager assetTransferManager;

    public static AssetTransferManager getAssetTransferManager() {
        return assetTransferManager;
    }


    public static String getClientId() {
        return clientId;
    }

    /**
     * Launch the platform's web browser and ask it to open the given URL. Note that this should not
     * be called from any uncontrolled macros as there are both security and denial-of-service attacks
     * possible.
     *
     * <p>This should not be called from any uncontrolled macros as there are both security and
     * denial-of-service attacks possible.
     *
     * <p>This must be called on the AWT thread.
     *
     * @param url the URL to pass to the browser.
     */
    public static void showDocument(String url) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            String urlToBrowse = url;
            Desktop desktop = Desktop.getDesktop();
            URI uri = null;
            try {
                uri = new URI(urlToBrowse);
                if (uri.getScheme() == null) {
                    urlToBrowse = "https://" + urlToBrowse;
                    uri = new URI(urlToBrowse);
                }
                desktop.browse(uri);
            } catch (Exception e) {
//        // MapTool.showError(I18N.getText("msg.error.browser.cannotStart", uri), e);
            }
        } else {
            String errorMessage = "msg.error.browser.notFound";
            Exception exception = null;
            String[] envvars = {"MAPTOOL_BROWSER", "BROWSER"};
            String param = envvars[0];
            boolean apparentlyItWorked = false;
            for (String var : envvars) {
                String browser = System.getenv(var);
                if (browser != null) {
                    try {
                        param = var + "=\"" + browser + "\"";
                        Runtime.getRuntime().exec(new String[]{browser, url});
                        apparentlyItWorked = true;
                    } catch (Exception e) {
                        exception = e;
                    }
                }
            }
            if (!apparentlyItWorked) {
                errorMessage = "msg.error.browser.cannotStart";
//        // MapTool.showError(I18N.getText(errorMessage, param), exception);
            }
        }
    }

    /**
     * returns the current locale code.
     */
    public static String getLanguage() {
        return Locale.getDefault(Locale.Category.DISPLAY).getLanguage();
    }

    public static ThumbnailManager getThumbnailManager() {
        if (thumbnailManager == null) {
            thumbnailManager = new ThumbnailManager(AppUtil.getAppHome("imageThumbs"), THUMBNAIL_SIZE);
        }

        return thumbnailManager;
    }

    private static void initJavaFX() {
        var eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
        SecondaryLoop secondaryLoop = eventQueue.createSecondaryLoop();
        Platform.startup(secondaryLoop::exit);
        secondaryLoop.enter();

        Platform.setImplicitExit(false); // necessary to use JavaFX later
    }

    private static void initialize() {
        String versionImplementation = version;
        String versionOverride = version;

        if (OsDetection.MAC_OS_X) {
            // On OSX the menu bar at the top of the screen can be enabled at any time, but the
            // title (ie. name of the application) has to be set before the GUI is initialized (by
            // creating a frame, loading a splash screen, etc). So we do it here.
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            String appName = "MapTool";
            System.setProperty("apple.awt.application.name", appName);
            System.setProperty("apple.awt.application.appearance", "system");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "About MapTool...");
        }
        if (MapTool.class.getPackage().getImplementationVersion() != null) {
            versionImplementation = MapTool.class.getPackage().getImplementationVersion().trim();
            log.info("getting MapTool version from manifest: " + versionImplementation);
        }

        if (MapTool.class.getPackage().getImplementationVendor() != null) {
            vendor = MapTool.class.getPackage().getImplementationVendor().trim();
            log.info("getting MapTool vendor from manifest:  " + vendor);
        }
        // System properties
        System.setProperty("swing.aatext", "true");
//        initJavaFX();
        try {
            ThemeSupport.loadTheme();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Protocol handlers
        // cp:// is registered by the RPTURLStreamHandlerFactory constructor (why?)
        RPTURLStreamHandlerFactory factory = new RPTURLStreamHandlerFactory();
        factory.registerProtocol("asset", new AssetURLStreamHandler());
        factory.registerProtocol("lib", new LibraryURLStreamHandler());

        URL.setURLStreamHandlerFactory(factory);

        LibraryManager.init();
        ImageIO.setUseCache(false);

        assetTransferManager = new AssetTransferManager();
        assetTransferManager.addConsumerListener(new AssetTransferHandler());
        log.info("Initialised.");
    }


    public static void main(String[] args) {
        log.info("********************************************************************************");
        log.info("**                                                                            **");
        log.info("**                              MapTool Started!                              **");
        log.info("**                                                                            **");
        log.info("********************************************************************************");
        log.info("Initialising...");
        initialize();
    }
}
