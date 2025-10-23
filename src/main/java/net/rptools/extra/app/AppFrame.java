package net.rptools.extra.app;

import net.rpTools.aoTool.addon.addon.AddOnLibraryImporter;
import net.rpTools.aoTool.addon.wrappers.WAddOnLibrary;
import net.rpTools.aoTool.addonui.EditorContainer;
import net.rpTools.aoTool.app.preference.Preferences;
import net.rpTools.aoTool.language.I18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pushingpixels.radiance.common.internal.font.DefaultKDEFontPolicy;
import org.pushingpixels.radiance.theming.api.RadianceThemingCortex;
import org.pushingpixels.radiance.theming.api.icon.RadianceIconPack;
import org.pushingpixels.radiance.theming.api.skin.SkinInfo;
import org.pushingpixels.radiance.theming.extras.api.RadianceThemingExtrasSkinPlugin;
import org.pushingpixels.radiance.theming.internal.utils.RadianceSizeUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

public class AppFrame extends JFrame {
    private static final Logger log = LogManager.getLogger(AppFrame.class);
    private static EditorContainer editorContainer;
    public EditorContainer getEditorContainer(){return editorContainer; }

    public AppFrame() {
        this(Collections.emptyList());
    }

    public AppFrame(Path addOnToLoad) throws IOException {
        this(addOnToLoad == null ? Collections.emptyList() : new ArrayList<>(List.of(Objects.requireNonNull(AddOnLibraryImporter.importFromFile(addOnToLoad.toFile())))));
    }
    public AppFrame(List<WAddOnLibrary> addOnsToLoad) {
        super();
        setLayout(new BorderLayout());
        Dimension maxSize = Toolkit.getDefaultToolkit().getScreenSize();
        setMinimumSize(new Dimension(maxSize.width * 3 / 4, maxSize.height * 3 / 4));





        RadianceIconPack iconPack = RadianceThemingCortex.GlobalScope.getIconPack();
        RadianceThemingCortex.GlobalScope.setTabCloseButtonsVisible(true);
        RadianceThemingCortex.GlobalScope.setFontPolicy(new DefaultKDEFontPolicy());
//        RadianceSizeUtils.setTreeIconSize();
//        RadianceSizeUtils.setTreeCellRendererInsets();
        RadianceSizeUtils.setControlFontSize(1);

        editorContainer = new EditorContainer(addOnsToLoad);
        add(editorContainer, BorderLayout.CENTER);

        setLocationByPlatform(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initMenuBar();

    }

    private final ActionListener menuListener = (l -> {
        switch (l.getActionCommand()){
            case "exit" -> dispose();
            case "close" -> close();
            case "create" -> create();
            case "import" -> importAddOn();
            case "open" -> open(null);
            default -> {
                try {
                    List<Path> mru = new ArrayList<>(Preferences.getList(Preferences.Key.MRU_DIRECTORIES).stream().map(Paths::get).toList());
                    mru.addAll(Preferences.getList(Preferences.Key.MRU_ADD_ONS).stream().map(Paths::get).toList());
                    mru = mru.stream().filter(path -> path.getFileName().endsWith(l.getActionCommand())).toList();
                    if(!mru.isEmpty()){
                        open(mru.getFirst());
                    }
                } catch (Exception ignored){
                }
            }
        }
    });
    private void close(){ editorContainer.removeAddOn(editorContainer.getCurrentAddOn()); }
    private void create(){
        try {
            editorContainer.addAddOn(AddOnLibraryImporter.importFromClassPath(Constants.RESOURCE_SAMPLES_PATH + "emptyAddOn.mtlib") );
        } catch (IOException e) {
            log.info(e.getLocalizedMessage(), e);
        }
    }
    private void importAddOn(){}
    private void open(Path path){}


    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        menu.setMnemonic(KeyEvent.VK_F);

        JMenuItem new_ = new JMenuItem(I18n.getText("verb.new"));
        new_.setActionCommand("create");
        new_.addActionListener(menuListener);
        menu.add(new_);

        JMenuItem open = new JMenuItem(I18n.getText("verb.open"));
        open.setActionCommand("open");
        open.addActionListener(menuListener);
        menu.add(open);

        JMenuItem import_ = new JMenuItem(I18n.getText("verb.import"));
        import_.setActionCommand("import");
        import_.addActionListener(menuListener);
        menu.add(import_);

        JMenuItem close = new JMenuItem(I18n.getText("verb.close"));
        close.setActionCommand("close");
        close.addActionListener(menuListener);
        menu.add(close);

        menu.addSeparator();

        JMenu mruAddon = new JMenu(MessageFormat.format("{0} ({1})", I18n.getText("noun.recent"), I18n.getText("noun.add-on")));
        mruAddon.setMnemonic(KeyEvent.VK_A);
        List<Path> mrud = Preferences.getList(Preferences.Key.MRU_ADD_ONS).stream().map(Paths::get).toList();
        mrud.stream().distinct().limit(14).forEach(path -> {
                    String name = path.getFileName().toString();
                    if (name.isEmpty()) {
                        name = path.getName(path.getNameCount() - 1).toString();
                    }
                    JMenuItem entry = new JMenuItem(name);
                    entry.addActionListener(menuListener);
                    mruAddon.add(entry);
                }
        );
        menu.add(mruAddon);

        JMenu mruDirectories = new JMenu(MessageFormat.format("{0} ({1})", I18n.getText("noun.recent"), I18n.getText("noun.folder")));
        mrud = Preferences.getList(Preferences.Key.MRU_DIRECTORIES).stream().map(Paths::get).toList();
        mrud.stream().distinct().limit(14).forEach(path -> {
                    String name = path.getFileName().toString();
                    if (name.isEmpty()) {
                        name = path.getName(path.getNameCount() - 1).toString();
                    }
                    JMenuItem entry = new JMenuItem(name);
                    entry.addActionListener(menuListener);
                    mruDirectories.add(entry);

                }
        );
        menu.add(mruDirectories);

        menu.addSeparator();
        JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem("Load most recent on start");
        cbMenuItem.setSelected(Preferences.getBoolean(Preferences.Key.LOAD_LAST_ON_START));
        cbMenuItem.addActionListener(e -> {
            if (((JCheckBoxMenuItem) e.getSource()).getState()) {
                Preferences.set(Preferences.Key.LOAD_LAST_ON_START, true);
            } else {
                Preferences.set(Preferences.Key.LOAD_LAST_ON_START, false);
            }
        });
        cbMenuItem.setMnemonic(KeyEvent.VK_V);
        menu.add(cbMenuItem);

        menu.addSeparator();
        cbMenuItem = new JCheckBoxMenuItem("Save on close");
        cbMenuItem.setSelected(Preferences.getBoolean(Preferences.Key.SAVE_ON_CLOSE));
        cbMenuItem.addActionListener(e -> {
            if (((JCheckBoxMenuItem) e.getSource()).getState()) {
                Preferences.set(Preferences.Key.SAVE_ON_CLOSE, true);
            } else {
                Preferences.set(Preferences.Key.SAVE_ON_CLOSE, false);
            }
        });
        cbMenuItem.setMnemonic(KeyEvent.VK_V);
        menu.add(cbMenuItem);

        cbMenuItem = new JCheckBoxMenuItem("Delete temp files on exit");
        cbMenuItem.setSelected(Preferences.getBoolean(Preferences.Key.CLEAR_TEMP_ON_CLOSE));
        cbMenuItem.addActionListener(e -> {
            if (((JCheckBoxMenuItem) e.getSource()).getState()) {
                Preferences.set(Preferences.Key.CLEAR_TEMP_ON_CLOSE, true);
            } else {
                Preferences.set(Preferences.Key.CLEAR_TEMP_ON_CLOSE, false);
            }
        });
        cbMenuItem.setMnemonic(KeyEvent.VK_T);
        menu.add(cbMenuItem);

        menu.addSeparator();

        JMenuItem exit = new JMenuItem(I18n.getText("verb.exit"));
        exit.setActionCommand("exit");
        exit.addActionListener(menuListener);
        menu.add(exit);

        JMenu themeMenu = new JMenu(I18n.getText("noun.theme"));
        Map<String, SkinInfo> skins = RadianceThemingCortex.GlobalScope.getAllSkins();
        Set<SkinInfo> extras = new RadianceThemingExtrasSkinPlugin().getSkins();
        extras.forEach(skinInfo -> skins.put(skinInfo.getDisplayName(), skinInfo));
        List<String> names = skins.keySet().stream().toList().stream().sorted().toList();
        String currentStringClassName = RadianceThemingCortex.GlobalScope.getCurrentSkin().getClass().getName();

        JMenu themesSubMenu = new JMenu(I18n.getText("noun.plural.skins"));
        ButtonGroup themeGroup = new ButtonGroup();
        for(String skin: names){
            SkinInfo skinInfo = skins.get(skin);
            JRadioButtonMenuItem skinItem = new JRadioButtonMenuItem(skinInfo.getDisplayName());
            if(skinInfo.getLookAndFeelClassName().equalsIgnoreCase(currentStringClassName)){
                skinItem.setSelected(true);
            }
            themeGroup.add(skinItem);
            final String lafClassName = skinInfo.getLookAndFeelClassName();
            skinItem.addActionListener(a -> {
                try {
                    UIManager.setLookAndFeel(lafClassName);
                    Preferences.set(Preferences.Key.LOOK_AND_FEEL, lafClassName);
                    AppFrame.this.repaint();
                    log.info("skin");
                } catch (ClassNotFoundException|InstantiationException|IllegalAccessException|UnsupportedLookAndFeelException e) {
                    log.info(e.getLocalizedMessage(), e);
                }
            });
            themesSubMenu.add(skinItem);
        }
        themeMenu.add(themesSubMenu);
        menuBar.add(themeMenu);

        setJMenuBar(menuBar);
    }

    @Override
    public void dispose() {
        App.close();
        super.dispose();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            pack();
        }
        super.setVisible(visible);
    }
}
