package net.rptools.extra.files;

import net.rpTools.aoTool.app.Constants;
import net.rpTools.aoTool.app.preference.Preferences;
import net.rpTools.aoTool.system.SystemProps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class AddOnChooser extends JFileChooser {
    private static final Logger log = LogManager.getLogger(AddOnChooser.class);
    public static final int ADD_ON = 0;
    public static final int ADD_ON_DIRECTORY = 1;
    public static final int ADD_ON_FILES = 2;

    private int what;
    private Path lastPath = null;

    public AddOnChooser() { this(null, ADD_ON_FILES); }
    public AddOnChooser(int what) { this(null, what); }
    public AddOnChooser(Path path) { this(path, Files.isDirectory(path) ? ADD_ON_DIRECTORY : ADD_ON_FILES); }
    public AddOnChooser(Path path, int what) {
        super();
        this.what = what;
        Path path1 = path != null ? path : lastPath;
        init();
    }

    private void init() {
        setInheritsPopupMenu(true);
        setFileHidingEnabled(true);
        setDragEnabled(false);
        setMultiSelectionEnabled(false);
        addChoosableFileFilter(Constants.ADD_ON_FILES);
        addChoosableFileFilter(Constants.ADD_ON_CONTENT);
        addChoosableFileFilter(Constants.MT_FILTER);
        addChoosableFileFilter(Constants.IMAGE_FILES);
        setWhat(what);
    }
    public void setWhat(int what){
        this.what = what;
        Preferences.Key key = null;
        switch (what) {
            case ADD_ON -> {
                key = Preferences.Key.MRU_ADD_ONS;
                setFileSelectionMode(JFileChooser.FILES_ONLY);
                setFileFilter(Constants.ADD_ON_FILES);
            }
            case ADD_ON_DIRECTORY -> {
                key = Preferences.Key.MRU_DIRECTORIES;
                setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            }
            default -> {
                setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                setFileFilter(Constants.ADD_ON_CONTENT);
            }
        }
        if(key != null){
            List<String> mru = Preferences.getList(key);
            if(mru != null && !mru.isEmpty()){
                setSelectedFile(new File(Path.of(mru.getLast()).toUri()));
            }
        } else {
            if(lastPath != null){
                setSelectedFile(new File(lastPath.toUri()));
            } else {
                setSelectedFile(new File(System.getProperty(SystemProps.USER_DIR)));
            }
        }
    }
    @Override
    public void approveSelection() {
        super.approveSelection();
        File file = getSelectedFile();
        Path path = Paths.get(file.toURI());
        this.lastPath = path;
        Preferences.Key key;
        if (Files.isDirectory(path)) {
            key = Preferences.Key.MRU_DIRECTORIES;
        } else if (file.toString().toLowerCase().contains(".zip") || file.toString().toLowerCase().contains(".mtlib")){
            key = Preferences.Key.MRU_ADD_ONS;
        } else {
            return;
        }
        List<String> mru = Preferences.getList(key);
        if(mru == null || mru.isEmpty()){
            mru = List.of(path.toString());
        } else {
            mru.add(path.toString());
        }
        Preferences.set(key, mru);
    }
}
