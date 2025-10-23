package net.rptools.extra.files;

import net.rpTools.aoTool.language.I18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

public class Prompt {
    private static final Logger log = LogManager.getLogger(Prompt.class);
    public static boolean save(){
        return 0 == JOptionPane.showInternalOptionDialog(
                null,
                I18n.getText("prompt.save"),
                null,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                JOptionPane.YES_OPTION
                );
    }

    public static void showError(String text, Exception e) {
        JOptionPane.showInternalMessageDialog(null, text);
        log.info(e.getLocalizedMessage(), e);
    }
}
