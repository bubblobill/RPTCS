package net.rptools.extra.app.preference;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;

import static net.rptools.extra.app.preference.Preferences.Type.*;

public class Preferences {
    private static final java.util.prefs.Preferences PREFERENCES = new FilePreferences(null, "");// Preferences.userNodeForPackage(FilePreferences.class);

    public enum Key {
        LAST_LOADED_ADDONS(STRING, ""),
        CLEAR_TEMP_ON_CLOSE(BOOL, true),
        FONT_SIZE(FLOAT, 13.5f),
        LOAD_LAST_ON_START(BOOL, true),
        LOOK_AND_FEEL(STRING, "McWin"),
        MRU_DIRECTORIES(LIST, Collections.emptyList()),
        MRU_ADD_ONS(LIST, Collections.emptyList()),
        SAVE_ON_CLOSE(BOOL, false),
        TREE_ICON_SIZE(INT, 24)
        ;
        private final Type type;
        private final Object def;
        Key(Type type, Object def){
            this.type = type;
            this.def = def;
        }
        private Object getDefault(){ return def;}
    }
    public enum Type{
        BOOL,
        DOUBLE,
        FLOAT,
        INT,
        LIST,
        MAP,
        STRING
    }
    static {
        try {
            List<String> existing = Arrays.stream(PREFERENCES.keys()).toList();
            Type type;
            for (Key key: Key.values()){
                if(!existing.contains(key.name())){
                    switch(key.type){
                        case BOOL -> PREFERENCES.putBoolean(key.name(), (boolean) key.getDefault());
                        case DOUBLE -> PREFERENCES.putDouble(key.name(), (double) key.getDefault());
                        case FLOAT -> PREFERENCES.putFloat(key.name(), (float) key.getDefault());
                        case INT -> PREFERENCES.putInt(key.name(), (int) key.getDefault());
                        case LIST -> ((FilePreferences) PREFERENCES).putList(key.name(), (List<?>) key.getDefault());
                        case MAP -> ((FilePreferences) PREFERENCES).putMap(key.name(), (Map<?, ?>) key.getDefault());
                        default -> PREFERENCES.put(key.name(), (String) key.getDefault());
                    }
                }
            }
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public static java.util.prefs.Preferences getPreferences(){ return PREFERENCES; }
    public static void set(Key key, Object value) throws ClassCastException{
        try {
            switch (key.type) {
                case BOOL -> PREFERENCES.putBoolean(key.name(), (Boolean) value);
                case DOUBLE -> PREFERENCES.putDouble(key.name(), (Double) value);
                case FLOAT -> PREFERENCES.putFloat(key.name(), (Float) value);
                case INT -> PREFERENCES.putInt(key.name(), (Integer) value);
                case LIST -> ((FilePreferences)PREFERENCES).putList(key.name(), (List<?>) value);
                case MAP -> ((FilePreferences)PREFERENCES).putMap(key.name(), (Map<?, ?>) value);
                case STRING -> PREFERENCES.put(key.name(), (String) value);
                default -> throw new IllegalStateException("Unexpected value: " + key.type);
            }
        } catch (ClassCastException cce){
            throw new RuntimeException(cce);
        }
    }
    public static boolean getBoolean(Key key){
        return PREFERENCES.getBoolean(key.name(), (boolean) key.getDefault());
    }
    public static double getDouble(Key key){
        return PREFERENCES.getDouble(key.name(), (double) key.getDefault());
    }
    public static float getFloat(Key key){
        return PREFERENCES.getFloat(key.name(), (float) key.getDefault());
    }
    public static int getInt(Key key){
        return PREFERENCES.getInt(key.name(), (int) key.getDefault());
    }
    @SuppressWarnings("unchecked")
    public static List<String> getList(Key key){
        return ((FilePreferences)PREFERENCES).getList(key.name(), (List<String>) key.getDefault());
    }
    @SuppressWarnings("unchecked")
    public static Map<String, String> getMap(Key key){
        return ((FilePreferences)PREFERENCES).getMap(key.name(), (Map<String, String>) key.getDefault());
    }

    public static String get(Key key){
        return PREFERENCES.get(key.name(), (String) key.getDefault());
    }
}