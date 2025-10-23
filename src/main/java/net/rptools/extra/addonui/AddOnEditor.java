package net.rptools.extra.addonui;

import net.miginfocom.swing.MigLayout;
import net.rpTools.aoTool.addon.addon.StatSheet;
import net.rpTools.aoTool.addon.wrappers.WAddOnLibrary;
import net.rpTools.aoTool.app.cache.ACache;
import net.rpTools.aoTool.app.event.NavigatorOpenEvent;
import net.rpTools.aoTool.app.event.NavigatorOpenEventListener;
import net.rpTools.aoTool.asset.Asset;
import net.rpTools.aoTool.asset.AssetManager;
import net.rpTools.aoTool.files.FileUtils;
import net.rpTools.aoTool.files.PathNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class AddOnEditor extends JComponent implements NavigatorOpenEventListener {
    private static final Logger log = LogManager.getLogger(AddOnEditor.class);
    private JRootPane rootPane;
    private JPanel contentPane;
    private JTabbedPane fileTabs;
    private JSplitPane splitPane;
    private JPanel splitLeft;
    private JPanel splitRight;
    private JPanel structurePanel;
    private JPanel configTab;

    private Navigator navigator;
    private final ConfigForm configForm = new ConfigForm();
    private final WAddOnLibrary wAddOnLibrary;

//    PropertyChangeListener tabChangeListener = (l) -> {
//        if (l.getPropertyName().equalsIgnoreCase("indexForTabComponent")) {
//            int index = (int) l.getNewValue();
//            getFileTabs().setTabComponentAt(index, new JTabLabel(getFileTabs()));
//        }
//    };

    public AddOnEditor(WAddOnLibrary wAddOnLibrary) {
        super();
        setLayout(new BorderLayout());
        add(contentPane, BorderLayout.CENTER);
        this.wAddOnLibrary = wAddOnLibrary;

//        getFileTabs().addPropertyChangeListener(tabChangeListener);
        initComponents();
        log.info("new {}", getClass().getSimpleName());
    }

    private void initComponents() {
        navigator = new Navigator(wAddOnLibrary);
        navigator.addNavigatorOpenEventListener(this);

        structurePanel.setLayout(new BorderLayout());
        structurePanel.add(navigator, BorderLayout.CENTER);
        configTab.setLayout(new BorderLayout());
        configTab.add(configForm.getMain(), BorderLayout.CENTER);
    }

    //@formatter:off
    private void createUIComponents() {}
    public JPanel getConfigTab() { return configTab; }
    public JPanel getMain() { return contentPane; }
    public JSplitPane getSplitPane() { return splitPane; }
    public JPanel getSplitLeft() { return splitLeft; }
    public JPanel getSplitRight() { return splitRight; }
    public JPanel getStructureTab() { return structurePanel; }
    public JTabbedPane getFileTabs() { return fileTabs; }
    //@formatter:on

    @Override
    public void listen(NavigatorOpenEvent noe) {
        PathNode pathNode = noe.pathNode;
        if(pathNode.isVirtualNode()){
           File f = FileUtils.createFile(pathNode.getFilePath());
           try {
               Asset asset = AssetManager.createAsset(f);
               pathNode.setAssetKey(asset.getMD5Key());
               ACache.put(asset.getMD5Key(), asset);
               pathNode.setVirtualNode(false);
           } catch (IOException e) {
               log.info(e.getLocalizedMessage(), e);
           }
        } else {
            Asset asset = (Asset) ACache.getIfPresent(pathNode.getAssetKey());
            if (asset != null) {
                switchToRightTab(pathNode);
            }
        }
    }

    private void switchToRightTab(PathNode pathNode) {
        String fileName = pathNode.getFilePath().getFileName().toString();
        JTabbedPane tp = getFileTabs();
        int index = tp.indexOfTab(fileName);
        if (index > -1) {
            tp.setSelectedIndex(index);
            return;
        }
        switch (fileName) {
            case "events.json" -> System.out.println(0);
            case "library.json" -> System.out.println(1);
            case "mts_properties.json" -> System.out.println(2);
            case "stat_sheets.json" -> addSheetsForm(pathNode);
            default -> System.out.println(((Asset) ACache.getIfPresent(pathNode.getAssetKey())).getType().toString());
//                Editor editor = new Editor();
//                editor.getEditorPane().setContentType(asset.getMediaTypeFromFileExtension().toString());
//
//                editor.getEditorPane().setText(asset.getDataAsString());
//                getTabbedRight().addTab(pathNode.getFilePath().getFileName().toString(), editor.getMain());
//                getTabbedRight().setSelectedComponent(editor.getMain());
        }
    }

    private void addSheetsForm(PathNode pathNode) {
        JPanel p = new JPanel(new MigLayout("flowy, wrap 5"));
        JScrollPane s = new JScrollPane(p);
        JPanel p2 = new JPanel(new BorderLayout());
        p2.add(s, BorderLayout.CENTER);
        getFileTabs().addTab("stat_sheets.json", p2);
        for (StatSheet ss : wAddOnLibrary.getStatSheets()) {
            p.add(new SheetForm(ss).getMain());
        }
        getFileTabs().setSelectedComponent(p2);
    }

    public WAddOnLibrary getAddOn() {
        return wAddOnLibrary;
    }

    public ConfigForm getConfigForm() {
        return configForm;
    }

    public Navigator getNavigator() {
        return navigator;
    }
}
