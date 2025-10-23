package net.rptools;

import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        AppUtil.initLogging();
        MapTool.main(args);
        SwingUtilities.invokeLater(()->{
            JFrame frame = new JFrame();
            frame.setMinimumSize(new Dimension(300,600));
            frame.add(new JButton(RessourceManager.getBigIcon(Icons.MAPTOOL)));
            frame.pack();
            frame.setVisible(true);
        });
    }
}
