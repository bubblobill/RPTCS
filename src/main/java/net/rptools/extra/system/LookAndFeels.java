package net.rptools.extra.system;

import net.rpTools.aoTool.app.preference.Preferences;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class LookAndFeels {
    private static final Map<String, UIManager.LookAndFeelInfo> LOOK_AND_FEELS = new HashMap<>();
    static {
        UIManager.installLookAndFeel("Acryl", "com.jtattoo.plaf.acryl.AcrylLookAndFeel");
        UIManager.installLookAndFeel("Aero", "com.jtattoo.plaf.aero.AeroLookAndFeel");
        UIManager.installLookAndFeel("Aluminium", "com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
        UIManager.installLookAndFeel("Bernstein", "com.jtattoo.plaf.bernstein.BernsteinLookAndFeel");
        UIManager.installLookAndFeel("Graphite", "com.jtattoo.plaf.graphite.GraphiteLookAndFeel");
        UIManager.installLookAndFeel("HiFi", "com.jtattoo.plaf.hifi.HiFiLookAndFeel");
        UIManager.installLookAndFeel("Luna", "com.jtattoo.plaf.luna.LunaLookAndFeel");
        UIManager.installLookAndFeel("McWin", "com.jtattoo.plaf.mcwin.McWinLookAndFeel");
        UIManager.installLookAndFeel("Mint", "com.jtattoo.plaf.mint.MintLookAndFeel");
        UIManager.installLookAndFeel("Noire", "com.jtattoo.plaf.noire.NoireLookAndFeel");
        UIManager.installLookAndFeel("Smart", "com.jtattoo.plaf.smart.SmartLookAndFeel");
        UIManager.installLookAndFeel("Texture", "com.jtattoo.plaf.texture.TextureLookAndFeel");

        for(UIManager.LookAndFeelInfo info: UIManager.getInstalledLookAndFeels()){
            LOOK_AND_FEELS.put(info.getName(), info);
        }
    }
    public LookAndFeels(){
        setLookAndFeel(Preferences.get(Preferences.Key.LOOK_AND_FEEL));
    }

    public Map<String, UIManager.LookAndFeelInfo> getLookAndFeels() {
        return LOOK_AND_FEELS;
    }

    public UIManager.LookAndFeelInfo getLookAndFeelInfo(String lafName){
        return LOOK_AND_FEELS.get(lafName);
    }

    public UIManager.LookAndFeelInfo getCurrentLookAndFeelInfo(){
        return LOOK_AND_FEELS.get(UIManager.getLookAndFeel().getName());
    }
    public static void setLookAndFeel(String lafName){
        if (LOOK_AND_FEELS.containsKey(lafName)) {
            try {
                UIManager.setLookAndFeel(LOOK_AND_FEELS.get(lafName).getClassName());
            } catch (UnsupportedLookAndFeelException | ClassNotFoundException |
                     InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
