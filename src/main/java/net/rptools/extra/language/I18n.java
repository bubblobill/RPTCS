package net.rptools.extra.language;

import net.rpTools.aoTool.app.Constants;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class I18n {
    private static ResourceBundle bundle;
    static {
        try {
            bundle = ResourceBundle.getBundle(Constants.RESOURCE_I18N_PATH);
        } catch (Exception e) {
            bundle = null;
        }
    }
    public static String getText(String key){
        if(bundle == null){
            return key;
        }
        return bundle.getString(key);
    }

    public static String getText(String key, Object... values) {
        if(bundle == null){
            return key;
        }
        return MessageFormat.format(bundle.getString(key), values);
    }
}
