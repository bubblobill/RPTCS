package net.rptools.extra.files;

import net.rpTools.aoTool.threading.ThreadPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;


public class LazyNode extends DefaultMutableTreeNode implements LazyNodeListener {
    private static final FileSystemView FSV = FileSystemView.getFileSystemView();
    private final ConcurrentLinkedQueue<LazyNodeListener> listeners = new ConcurrentLinkedQueue<>(new ArrayList<>());
    public void addListener(LazyNodeListener listener) { listeners.add(listener); }
    public void removeListener(LazyNodeListener listener) { listeners.remove(listener); }
    public synchronized void event(LazyNode.LazyNodeEvent e) {
        for (LazyNodeListener l : listeners){
            SwingUtilities.invokeLater(()-> l.event(e));
        }
    }
    private static final Logger log = LogManager.getLogger(LazyNode.class);
    private final AtomicBoolean kidsLoaded = new AtomicBoolean(false);
    private final AtomicBoolean kidsLoading = new AtomicBoolean(false);

    public LazyNode(File file, boolean allowsChildren) {
        super(file, allowsChildren);
        if(file == null){
            log.info("Null file object");
        } else if(FSV.isLink(file)){
//            isComputerNode
//            File.FS.fromURIPath(file.path)
            try {
                File f = FSV.getLinkLocation(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
//            log.info(FSV.getSystemTypeDescription(file));
//            .equalsIgnoreCase("System Folder")); {
//                FSV.getLinkLocation(file);
//                log.info(FSV.getDefaultDirectory());
//                log.info(FSV.getHomeDirectory());
//                log.info(FSV.getParentDirectory(file));
//                log.info(FSV.getRoots());
        }
    }

    public LazyNode(File file) {
        this(file, file.isDirectory());
    }

    public LazyNode(DefaultMutableTreeNode treeNode) {
        this((File) treeNode.getUserObject());
        LazyNode source = (LazyNode) treeNode;
        allowsChildren = source.getAllowsChildren();
        if (allowsChildren) {
            source.children().asIterator().forEachRemaining(child -> ((LazyNode) child).setParent(this));
        }
        kidsLoaded.set(source.kidsLoaded.get());
        kidsLoading.set(source.kidsLoading.get());
    }

    public synchronized File getFile() {
        if (getUserObject() instanceof File file) {
            return file;
        }
        return null;
    }

    public synchronized Path getFilePath() {
        if (getUserObject() instanceof File file) {
            try {
                if(FSV.isComputerNode(file)) {
                    return ((LazyNode) children.getFirst()).getFilePath();
                }
                return file.getAbsoluteFile().toPath().toRealPath();
            } catch (Exception e) {
                log.info("file.toPath().toRealPath() issue: {}", file.getName());
                try {
                    return FSV.getLinkLocation(file).toPath();
                } catch (NullPointerException | FileNotFoundException ex) {
                    try {
                        return FileSystems.getDefault().getPath(file.getAbsolutePath());
                    } catch (Exception exc) {
                        throw new RuntimeException(exc);
                    }
//                    throw new RuntimeException(ex);
                }
            }
        }
        return null;
    }

    public synchronized void loadChildren() {
        loadChildren(1);
    }
    public synchronized void loadChildren(final int depth) {
        if (loading() || loaded()) {
            return;
        }
        ThreadPool.execute(() -> {
            try {
                setKidsLoading(true);
                event(new LazyNodeEvent(this, LazyNodeEventType.LOADING, null, null));
//                DirectoryWalker.getInstance(this, true, depth).walk().getTreeNode()
//                        .children().asIterator()
//                        .forEachRemaining(child -> ((LazyNode)child).setParent(this));
//                setKidsLoading(false);
//                setKidsLoaded(true);
//                event(new LazyNodeEvent(this, LazyNodeEventType.LOADED, null, null));
//            } catch (InvalidPathException ipe){
//                try {
                    if(depth > 0) {
                        File f = getFile();
                        File[] kids = FileSystemView.getFileSystemView().getFiles(f, true);
                        if (kids != null) {
                            for (File kid : kids) {
                                LazyNode child = new LazyNode(kid);
                                add(child);
                                child.loadChildren(depth - 1);
                            }
                        }
                    }
                    setKidsLoading(false);
                    setKidsLoaded(true);
                    event(new LazyNodeEvent(this, LazyNodeEventType.LOADED, null, null));
                } catch (Exception e){
                    setKidsLoading(false);
                    event(new LazyNodeEvent(this, LazyNodeEventType.ERROR, "Error loading children", e));
                }
        });
    }
    public synchronized void setKidsLoading(boolean value){
        this.kidsLoading.set(value);
    }
    public synchronized void setKidsLoaded(boolean value){
        this.kidsLoaded.set(value);
    }
    public synchronized boolean loading() {
        return kidsLoading.get();
    }
    public synchronized boolean loaded() {
        return kidsLoaded.get();
    }

    @Override
    public void insert(MutableTreeNode newChild, int childIndex) {
        super.insert(newChild, childIndex);
        event(new LazyNodeEvent(this, LazyNodeEventType.CHANGED, "insert", null));
    }

    @Override
    public void setAllowsChildren(boolean allows) {
        super.setAllowsChildren(allows);
        event(new LazyNodeEvent(this, LazyNodeEventType.CHANGED, "allowsChildren", null));
    }

    @Override
    public void setParent(MutableTreeNode newParent) {
        super.setParent(newParent);
        if(newParent != null) {
            Collections.addAll(listeners, ((LazyNode) newParent).listeners.toArray(LazyNodeListener[]::new));
        }
        event(new LazyNodeEvent(this, LazyNodeEventType.CHANGED, "parent", null));
    }

    @Override
    public void setUserObject(Object userObject) {
        super.setUserObject(userObject);
        event(new LazyNodeEvent(this, LazyNodeEventType.CHANGED, "userObject", null));
    }


    public LazyNode getChildAt(int index) {
        return (LazyNode) super.getChildAt(index);
    }

    public List<LazyNode> lazyChildren() {
        List<LazyNode> list = new ArrayList<>(getChildCount());
        for (int i = 0; i < getChildCount(); i++) {
            list.add(getChildAt(i));
        }
        return list;
    }
}
