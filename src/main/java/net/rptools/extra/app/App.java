package net.rptools.extra.app;

import net.rpTools.aoTool.app.cache.ACache;
import net.rpTools.aoTool.app.preference.FilePreferencesFactory;
import net.rpTools.aoTool.app.preference.Preferences;
import net.rpTools.aoTool.asset.url.AssetURLStreamHandler;
import net.rpTools.aoTool.asset.url.LibraryURLStreamHandler;
import net.rpTools.aoTool.asset.url.RPTURLStreamHandlerFactory;
import net.rpTools.aoTool.files.FileUtils;
import net.rpTools.aoTool.files.PackedFile;
import net.rpTools.aoTool.threading.ThreadPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pushingpixels.ephemeral.chroma.dynamiccolor.ContainerConfiguration;
import org.pushingpixels.radiance.theming.api.ContainerColorTokensBundle;
import org.pushingpixels.radiance.theming.api.RadianceSkin;
import org.pushingpixels.radiance.theming.api.RadianceThemingCortex;
import org.pushingpixels.radiance.theming.api.skin.SkinChangeListener;
import org.pushingpixels.radiance.theming.internal.utils.RadianceCoreUtilities;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;

public class App {

    private static final Logger log = LogManager.getLogger(App.class);

    public void initApp(){
        RPTURLStreamHandlerFactory factory = new RPTURLStreamHandlerFactory();
        factory.registerProtocol("asset", new AssetURLStreamHandler());
        factory.registerProtocol("lib", new LibraryURLStreamHandler());
        URL.setURLStreamHandlerFactory(factory);
        // use preferences file
        System.setProperty("java.util.prefs.PreferencesFactory", FilePreferencesFactory.class.getName());
        System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, "addOnAppPrefs.txt");
        // set Look and Feel
//        LookAndFeels.setLookAndFeel(Preferences.get(Preferences.Key.LOOK_AND_FEEL));

        SkinChangeListener skinChangeListener = new SkinChangeListener() {
            @Override
            public void skinChanged() {
                RadianceSkin skin = RadianceThemingCortex.GlobalScope.getCurrentSkin();
                RadianceCoreUtilities.updateActiveUi();
log.info("skin change");
                ContainerColorTokensBundle colorTokensBundle;
                ContainerConfiguration.defaultLight();
            }
        };
//        Reflective.getResourcePaths();
        try {
//            UIManager.setLookAndFeel("org.pushingpixels.radiance.theming.api.skin.RadianceBusinessLookAndFeel");
            UIManager.setLookAndFeel("org.pushingpixels.radiance.theming.api.skin.RadianceMistSilverLookAndFeel");
//            UIManager.setLookAndFeel("org.pushingpixels.radiance.theming.api.skin.RadianceCeruleanLookAndFeel");
//            UIManager.setLookAndFe    el("org.pushingpixels.radiance.theming.api.skin.RadianceOfficeSilver2007LookAndFeel");
//            UIManager.setLookAndFeel("org.pushingpixels.radiance.theming.api.skin.RadianceMistAquaLookAndFeel");
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException |
                 InstantiationException e) {
            throw new RuntimeException(e);
        }
        PackedFile.init(Constants.TEMP_FOLDER);

        if(Preferences.getBoolean(Preferences.Key.LOAD_LAST_ON_START)){
            Data.restore();
            if(Data.LOADED_LIBRARIES.isEmpty()) {
                try {
                    Data.AppFrame = new AppFrame(Data.mruAddOn.get());
                } catch (IOException ignored) {
                    Data.AppFrame = new AppFrame();
                }
            } else {
                Data.AppFrame = new AppFrame(Data.LOADED_LIBRARIES);
            }
        } else {
            Data.AppFrame = new AppFrame();
        }
    }

    public static void close() {
        Data.backup();
        ThreadPool.closePool();
        ACache.cleanUp();
        if (Preferences.getBoolean(Preferences.Key.CLEAR_TEMP_ON_CLOSE)) {
            FileUtils.deleteTempFiles();
        }
    }
}
