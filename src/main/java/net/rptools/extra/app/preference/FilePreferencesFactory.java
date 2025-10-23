package net.rptools.extra.app.preference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

/**
 * PreferencesFactory implementation that stores the preferences in a user-defined file. To use it,
 * set the system property <tt>java.util.prefs.PreferencesFactory</tt> to
 * <tt>net.infotrek.util.prefs.FilePreferencesFactory</tt>
 * <p/>
 * The file defaults to [user.home]/.fileprefs, but may be overridden with the system property
 * <tt>net.infotrek.util.prefs.FilePreferencesFactory.file</tt>
 *
 * @author David Croft (<a href="http://www.davidc.net">www.davidc.net</a>)
 * @version $Id: FilePreferencesFactory.java 282 2009-06-18 17:05:18Z david $
 */
public class FilePreferencesFactory implements PreferencesFactory {
    private static final Logger log = LogManager.getLogger(FilePreferencesFactory.class);

    public static final String SYSTEM_PROPERTY_FILE = FilePreferencesFactory.class.getName() + ".file";
    static {
        System.setProperty("java.util.prefs.PreferencesFactory", FilePreferencesFactory.class.getName());
        System.setProperty(SYSTEM_PROPERTY_FILE, "addOnAppPrefs.txt");
    }

    Preferences rootPreferences;

    public Preferences systemRoot() {
        return userRoot();
    }

    public Preferences userRoot() {
        if (rootPreferences == null) {
            log.info("Instantiating root preferences");
            rootPreferences = new FilePreferences(null, "");
        }
        return rootPreferences;
    }

    private static File preferencesFile;

    public static File getPreferencesFile() {
        if (preferencesFile == null) {
            String prefsFile = System.getProperty(SYSTEM_PROPERTY_FILE);
            if (prefsFile == null || prefsFile.isEmpty()) {
                prefsFile = System.getProperty("user.home") + File.separator + ".fileprefs";
            }
            preferencesFile = new File(prefsFile).getAbsoluteFile();
            log.info("Preferences file is " + preferencesFile);
        }
        return preferencesFile;
    }

    public static void main(String[] args) throws BackingStoreException {


        Preferences p = Preferences.userNodeForPackage(FilePreferences.class);

        for (String s : p.keys()) {
            log.info("p[" + s + "]=" + p.get(s, null));
        }

        p.putBoolean("no", true);
        p.put("Number", String.valueOf(System.currentTimeMillis()));
    }
}
