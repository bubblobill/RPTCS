package net.rptools.extra.app.preference;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/**
 * Preferences implementation that stores to a user-defined file. See FilePreferencesFactory.
 *
 * @author David Croft (<a href="http://www.davidc.net">www.davidc.net</a>)
 * @version $Id: FilePreferences.java 283 2009-06-18 17:06:58Z david $
 */
public class FilePreferences extends AbstractPreferences
{
    private static final Logger log = LogManager.getLogger(FilePreferences.class);

    private final Map<String, String> root;
    private final Map<String, FilePreferences> children;
    private boolean isRemoved = false;

    public FilePreferences(AbstractPreferences parent, String name) {
        super(parent, name);

        log.info("Instantiating node {}", name);

        root = new TreeMap<>();
        children = new TreeMap<>();

        try {
            sync();
        }
        catch (BackingStoreException e) {
            log.error("Unable to sync on creation of node {}", name, e);
        }
    }

    protected void putSpi(String key, String value) {
        root.put(key, value);
        try {
            flush();
        }
        catch (BackingStoreException e) {
            log.error("Unable to flush after putting {}", key, e);
        }
    }

    protected String getSpi(String key) {
        return root.get(key);
    }

    protected void removeSpi(String key) {
        root.remove(key);
        try {
            flush();
        }
        catch (BackingStoreException e) {
            log.error("Unable to flush after removing {}", key, e);
        }
    }

    protected void removeNodeSpi() throws BackingStoreException {
        isRemoved = true;
        flush();
    }

    protected String[] keysSpi() throws BackingStoreException {
        return root.keySet().toArray(new String[root.keySet().size()]);
    }

    protected String[] childrenNamesSpi() throws BackingStoreException {
        return children.keySet().toArray(new String[children.keySet().size()]);
    }

    protected FilePreferences childSpi(String name) {
        FilePreferences child = children.get(name);
        if (child == null || child.isRemoved()) {
            child = new FilePreferences(this, name);
            children.put(name, child);
        }
        return child;
    }


    protected void syncSpi() throws BackingStoreException {
        if (isRemoved()) return;

        final File file = FilePreferencesFactory.getPreferencesFile();

        if (!file.exists()) return;

        synchronized (file) {
            Properties p = new Properties();
            try {
                p.load(new FileInputStream(file));

                StringBuilder sb = new StringBuilder();
                getPath(sb);
                String path = sb.toString();

                final Enumeration<?> pnen = p.propertyNames();
                while (pnen.hasMoreElements()) {
                    String propKey = (String) pnen.nextElement();
                    if (propKey.startsWith(path)) {
                        String subKey = propKey.substring(path.length());
                        // Only load immediate descendants
                        if (subKey.indexOf('.') == -1) {
                            root.put(subKey, p.getProperty(propKey));
                        }
                    }
                }
            }
            catch (IOException e) {
                throw new BackingStoreException(e);
            }
        }
    }

    private void getPath(StringBuilder sb) {
        final FilePreferences parent = (FilePreferences) parent();
        if (parent == null) return;

        parent.getPath(sb);
        sb.append(name()).append('.');
    }

    protected void flushSpi() throws BackingStoreException {
        final File file = FilePreferencesFactory.getPreferencesFile();

        synchronized (file) {
            Properties p = new Properties();
            try {

                StringBuilder sb = new StringBuilder();
                getPath(sb);
                String path = sb.toString();

                if (file.exists()) {
                    p.load(new FileInputStream(file));

                    List<String> toRemove = new ArrayList<>();

                    // Make a list of all direct children of this node to be removed
                    final Enumeration<?> pnen = p.propertyNames();
                    while (pnen.hasMoreElements()) {
                        String propKey = (String) pnen.nextElement();
                        if (propKey.startsWith(path)) {
                            String subKey = propKey.substring(path.length());
                            // Only do immediate descendants
                            if (subKey.indexOf('.') == -1) {
                                toRemove.add(propKey);
                            }
                        }
                    }

                    // Remove them now that the enumeration is done with
                    for (String propKey : toRemove) {
                        p.remove(propKey);
                    }
                }

                // If this node hasn't been removed, add back in any values
                if (!isRemoved) {
                    for (String s : root.keySet()) {
                        p.setProperty(path + s, root.get(s));
                    }
                }

                p.store(new FileOutputStream(file), "FilePreferences");
            }
            catch (IOException e) {
                throw new BackingStoreException(e);
            }
        }
    }
    private final Gson gson = new Gson();

    public void putList(String key, List<?> list){
        if(list != null) {
            List<String> stringList = list.stream().map(String::valueOf).toList();
            put(key, gson.toJson(stringList, List.class));
        } else {
            put(key, null);
        }
    }
    public List<String> getList(String key){
        return getList(key, new ArrayList<>());
    }
    public List<String> getList(String key, List<String> dflt){
        String rawString = super.get(key, null);
        if(rawString == null || rawString.isEmpty()){
            return dflt;
        }
        List<?> list = gson.fromJson(rawString, List.class);
        List<String> listOut = new ArrayList<>();
        Collections.addAll(listOut, list.stream().map(String::valueOf).toArray(String[]::new));
        return listOut;
    }

    public void putMap(String key, Map<?,?> map){
        Map<String, String> stringMap = new HashMap<>();
        if(map != null && !map.isEmpty()) {
            map.forEach((key1, value) -> stringMap.put(String.valueOf(key1), String.valueOf(value)));
            put(key, gson.toJson(stringMap, Map.class));
        } else {
            put(key, null);
        }
    }
    public Map<String,String> getMap(String key){
        return getMap(key, new HashMap<>());
    }
    public Map<String, String> getMap(String key, Map<String,String> dflt){
        String rawString = super.get(key, null);
        if(rawString == null || rawString.isEmpty()){
            return dflt;
        } else {
            Map<?, ?> map = gson.fromJson(rawString, Map.class);
            Map<String, String> stringMap = new HashMap<>();
            if(map != null && !map.isEmpty()) {
                map.forEach((key1, value) -> stringMap.put(String.valueOf(key1), String.valueOf(value)));
            }
            return stringMap;
        }
    }
}