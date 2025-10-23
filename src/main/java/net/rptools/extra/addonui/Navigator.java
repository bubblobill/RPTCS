package net.rptools.extra.addonui;

import com.google.gson.Gson;
import net.rpTools.aoTool.addon.wrappers.*;
import net.rpTools.aoTool.app.Constants;
import net.rpTools.aoTool.app.cache.ACache;
import net.rpTools.aoTool.app.event.NavigatorOpenEvent;
import net.rpTools.aoTool.app.event.NavigatorOpenEventListener;
import net.rpTools.aoTool.app.preference.Preferences;
import net.rpTools.aoTool.asset.GUID;
import net.rpTools.aoTool.files.AddOnChooser;
import net.rpTools.aoTool.files.PackedFile;
import net.rpTools.aoTool.files.PathNode;
import net.rpTools.aoTool.files.TreeUtils;
import net.rpTools.aoTool.system.SystemProps;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class Navigator extends JComponent {
    private static final Logger log = LogManager.getLogger(Navigator.class);
    private JTree tree;
    private JPanel contentPane;
    private JTextField addonPathField;
    private JButton openFileButton;
    private JTextField folderPathField;
    private JButton openFolderButton;
    private JScrollPane scrollPane;
    private static final FileSystemView fsv = FileSystemView.getFileSystemView();
    private WAddOnLibrary wAddOnLibrary;
    private final List<NavigatorOpenEventListener> openEventListeners = new ArrayList<>();

    public void addNavigatorOpenEventListener(NavigatorOpenEventListener noel) {
        openEventListeners.add(noel);
    }

    public void removeNavigatorOpenEventListener(NavigatorOpenEventListener noel) {
        openEventListeners.remove(noel);
    }

    public void fireNOE(NavigatorOpenEvent noe) {
        // Notify everybody that may be interested.
        for (NavigatorOpenEventListener noel : openEventListeners) {
            noel.listen(noe);
        }
        reloadTree();
    }

    MouseListener doubleClickListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                TreePath treePath = tree.getClosestPathForLocation(e.getX(), e.getY());
                if (treePath != null) {
                    PathNode pathNode = (PathNode) treePath.getLastPathComponent();
                    editNodeFile(pathNode);
                }
            }
        }
    };

    public Navigator() {
        super();
        setLayout(new BorderLayout());
        add(contentPane, BorderLayout.CENTER);

    }

    public Navigator(WAddOnLibrary WAddOnLibrary) {
        super();
        setLayout(new BorderLayout());
        add(contentPane, BorderLayout.CENTER);

        this.wAddOnLibrary = WAddOnLibrary;
        initComponents();
        log.info("new {}", getClass().getSimpleName());
    }

    private void createUIComponents() {
    }

    private void initComponents() {
        Path filePath = wAddOnLibrary.getArchiveLocation();
        Path folderPath = wAddOnLibrary.getLinkedDirectory();
        if (filePath != null) {
            addonPathField.setText(filePath.toString());
            addonPathField.setEnabled(false);
            openFileButton.setEnabled(false);
        }
        if (folderPath != null) {
            folderPathField.setText(folderPath.toString());
            folderPathField.setEnabled(false);
            openFolderButton.setEnabled(false);
        }

        tree.setCellRenderer(new PathNode.PathNodeRenderer());
        tree.setFont(tree.getFont().deriveFont(Preferences.getFloat(Preferences.Key.FONT_SIZE)));
        tree.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

        tree.setModel(new DefaultTreeModel(wAddOnLibrary.getPathNode(), true));
//        if(filePath != null){
//            setTreeModel(filePath);
//        } else if(folderPath != null) {
//            setTreeModel(folderPath);
//        } else {
//            tree.setModel(new DefaultTreeModel(Data.getModelTreeRootNode()));
//        }
        tree.expandRow(0);
        tree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_O) {
                    PathNode pathNode = (PathNode) tree.getSelectionModel().getLeadSelectionPath().getLastPathComponent();
                    editNodeFile(pathNode);
                }
            }
        });


        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tree.addMouseListener(doubleClickListener);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tree.removeMouseListener(doubleClickListener);
            }
        });

        openFileButton.addActionListener(e -> {
            JFileChooser jfc = new AddOnChooser(AddOnChooser.ADD_ON);
            if (jfc.showDialog(contentPane, "Open") == 0) {
                Path p = Paths.get(jfc.getSelectedFile().toURI());
                wAddOnLibrary.setArchiveLocation(p);
                addonPathField.setText(p.toAbsolutePath().toString());
                setTreeModel(p);
            }
        });
        openFolderButton.addActionListener(e -> {
            JFileChooser jfc = new AddOnChooser(AddOnChooser.ADD_ON_DIRECTORY);
            if (jfc.showDialog(contentPane, "Select") == 0) {
                Path p = Paths.get(jfc.getSelectedFile().toURI());
                wAddOnLibrary.setLinkedDirectory(p);
                folderPathField.setText(p.toAbsolutePath().toString());
            }
        });
//        } catch (Exception e){
//            log.info(e.getMessage(), e);
////            System.out.println(Arrays.deepToString(e.getStackTrace()));
//        }
    }
    public void reloadTree(){
        SwingUtilities.invokeLater(() -> ((DefaultTreeModel) tree.getModel()).reload());
    }
    public void setAddonArchivePath(Path path){
        addonPathField.setText(path.toString());
        if(!wAddOnLibrary.getArchiveLocation().equals(path)){
            wAddOnLibrary.setArchiveLocation(path);
            reloadTree();
        }
    }
    public void setAddOnFolderPath(Path path){
        folderPathField.setText(path.toString());
        if(!wAddOnLibrary.getLinkedDirectory().equals(path)){
            wAddOnLibrary.setLinkedDirectory(path);
            reloadTree();
        }
    }
    private void editNodeFile(PathNode pathNode) {
        if (pathNode.getAllowsChildren()) { // is just tree expansion event
            return;
        }
        if (pathNode.getAssetKey() == null) {
            pathNode = createFileIfNotExists(pathNode);
        }
        fireNOE(new NavigatorOpenEvent(this, pathNode));

    }

    private PathNode createFileIfNotExists(PathNode pathNode) {
        Path path = pathNode.getFilePath();
        File f = null;
        String fileString = null;
        for (String configFileName : Constants.CONFIG_FILES) {
            if (pathNode.getFilePath().endsWith(configFileName)) {
                Gson gson = new Gson();
                fileString = switch (configFileName) {
                    case "events.json" -> gson.toJson(new WEvents());
                    case "library.json" -> gson.toJson(new WInfo());
                    case "mts_properties.json" -> gson.toJson(new WMTScripts());
                    case "stat_sheets.json" -> gson.toJson(new WStatSheets());
                    default -> "";
                };
            }
        }
        if (!path.isAbsolute()) {
            Path p;
            if (((PathNode) pathNode.getRoot()).getFilePath().isAbsolute()) {
                p = pathNode.getPathToRoot();
            } else {
                String tmp = MessageFormat.format("{0}/{1}{2}",
                        System.getProperty(SystemProps.TEMP_DIR),
                        new GUID().toString(),
                        pathNode.getFilePath().getFileName().toString()
                );
                p = Path.of(tmp);
                try {
                    if (fileString != null) {
                        IOUtils.write(fileString, new FileWriter(p.toFile()));
                    } else {
                        Files.createDirectory(p);
                    }
                } catch (IOException e) {
                    log.info(e.getLocalizedMessage(), e);
                }
            }
            f = p.toFile();
        }

            boolean isArchive = false;
            try {
                Files.exists(path);
            } catch (ClosedFileSystemException cfse) {
                isArchive = true;
                PackedFile packedFile = new PackedFile(path.getFileSystem().getRootDirectories().iterator().next().toFile());
                try {
                    f = (File) ACache.getIfPresent(path);
                    System.out.println(f.getName());
                } catch (Exception e) {
                    log.info(e.getMessage(), e);
                }
            }
//            if (Files.isDirectory(path)) {
//                f = FileUtils.createFolder(path);
//            } else {
//                boolean notConfig = true;
//                for (String configFileName : Constants.CONFIG_FILES) {
//                    if (pathNode.getFilePath().endsWith(configFileName)) {
//                        notConfig = false;
//                        f = FileUtils.copyConfigFile(configFileName, path);
//
//                        // something special
//                    }
//                }
//                if (notConfig) {
//                    f = FileUtils.createFile(pathNode.getFilePath());
//                }
//            }
            if (f != null) {
                pathNode = new PathNode(Paths.get(f.toURI()));
                pathNode.setVirtualNode(false);
            }
            return pathNode;
        }

        public void setTreeModel (Path path){
            tree.setModel(new DefaultTreeModel(TreeUtils.createTreeRoot(path)));
            reloadTree();
        }
    }
