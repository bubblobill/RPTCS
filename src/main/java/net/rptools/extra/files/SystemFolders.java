package net.rptools.extra.files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class SystemFolders {
    /*
    https://stackoverflow.com/questions/33640908/how-to-navigate-to-a-network-host-in-jfilechooser/34620383#34620383
     */
    private static final Logger log = LogManager.getLogger(SystemFolders.class);
    public static final int COMPUTER = 0;
    public static final int DESKTOP = 1;
    public static final int DOCUMENTS = 2;
    public static final int DOWNLOADS = 3;
    public static final int FAVORITES = 4;
    public static final int LIBRARIES = 5;
    public static final int LINKS = 6;
    public static final int MUSIC = 7;
    public static final int NETWORK = 8;
    public static final int ONEDRIVE = 9;
    public static final int PICTURES = 10;
    public static final int VIDEOS = 11;

    private static final ConcurrentHashMap<Integer, File> FILE_MAP = new ConcurrentHashMap<>(11);
    private static final AtomicReference<LazyNode> rootNode = new AtomicReference<>(new LazyNode(new File("ROOT"), true));
    private static final LazyTreeModel treeModel = new LazyTreeModel(rootNode.get(), true);
    private static final JTree tree = new JTree(treeModel);

    static {
        tree.setRootVisible(false);
        ConcurrentLinkedQueue<CompletableFuture<LazyNode>> futures = new ConcurrentLinkedQueue<>();
        final JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jfc.setSelectedFile(Path.of(System.getProperty("user.home")).toFile());
        final FileSystemView fsv = jfc.getFileSystemView();
        final File root = fsv.getRoots()[0];
        for (File file : fsv.getFiles(jfc.getSelectedFile(), false)) {
            switch (file.getName()) {
                case "Desktop" -> {
                    FILE_MAP.put(DESKTOP, root);
                    FILE_MAP.put(LIBRARIES, fsv.getChild(root, "::{031E4825-7B94-4DC3-B131-E946B44C8DD5}"));
                    FILE_MAP.put(COMPUTER, fsv.getChild(root, "::{20D04FE0-3AEA-1069-A2D8-08002B30309D}"));
                    FILE_MAP.put(NETWORK, fsv.getChild(root, "::{F02C1A0D-BE21-4350-88B0-7367FC96EF3C}"));
                }
                case "Documents" -> FILE_MAP.put(DOCUMENTS, file);
                case "Downloads" -> FILE_MAP.put(DOWNLOADS, file);
                case "Favorites" -> FILE_MAP.put(FAVORITES, file);
                case "Links" -> FILE_MAP.put(LINKS, file);
                case "Music" -> FILE_MAP.put(MUSIC, file);
                case "OneDrive" -> FILE_MAP.put(ONEDRIVE, file);
                case "Pictures" -> FILE_MAP.put(PICTURES, file);
                case "Videos" -> FILE_MAP.put(VIDEOS, file);
            }
        }
        for (File file : FILE_MAP.values()) {
            if (file != null) {
                addRootNodeChild(new LazyNode(file));
            }
        }
        //@formatter:off
        getTreeModel().addTreeModelListener(new TreeModelListener() {
            @Override public void treeNodesChanged(TreeModelEvent e) { getTreeModel().reload(); }
            @Override public void treeNodesInserted(TreeModelEvent e) { getTreeModel().reload(); }
            @Override public void treeNodesRemoved(TreeModelEvent e) { getTreeModel().reload(); }
            @Override public void treeStructureChanged(TreeModelEvent e) { getTreeModel().reload(); }
        });
        //@formatter:on
    }

    public static ConcurrentHashMap<Integer, File> getFileMap() {
        return FILE_MAP;
    }

    public static synchronized JTree getTree() {
        return tree;
    }

    public static synchronized LazyTreeModel getTreeModel() {
        return treeModel;
    }

    public static synchronized LazyNode getRootNode() {
        return rootNode.get();
    }

    public static synchronized void setRootNode(LazyNode node) {
        rootNode.set(node);
    }

    public static synchronized void addRootNodeChild(LazyNode child) {
        LazyNode n = rootNode.get();
        n.add(child);
        rootNode.set(n);
    }

    public static class LazyTreeModel extends DefaultTreeModel {
        private final LazyNode lazyRoot;

        public LazyTreeModel(LazyNode lazyRoot, boolean asksAllowsChildren) {
            super(lazyRoot, asksAllowsChildren);
            this.lazyRoot = lazyRoot;
        }

        public LazyTreeModel(LazyNode lazyRoot) {
            super(lazyRoot);
            this.lazyRoot = lazyRoot;
        }

        public LazyNode getLazyRoot() {
            return lazyRoot;
        }

        @Override
        public Object getRoot() {
            return lazyRoot;
        }

        public LazyNode getChild(LazyNode parent, int index) {
            return (LazyNode) super.getChild(parent, index);
        }
    }
}