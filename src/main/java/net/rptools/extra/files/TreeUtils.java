package net.rptools.extra.files;

import net.rpTools.aoTool.app.Constants;
import net.rpTools.aoTool.app.Data;
import net.rpTools.aoTool.app.cache.ACache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TreeUtils {
    private static final Logger log = LogManager.getLogger(TreeUtils.class);
    private static final PathNode modelNode = Data.getModelTreeRootNode();

    public static PathNode createTreeRoot(Path path) {
        PathNode addOnRoot;
        if (path.toString().toLowerCase().contains(".zip") || path.toString().toLowerCase().contains(".mtlib")) {
            addOnRoot = rootNodeFromAddOnArchive(path);
        } else if(Files.isDirectory(path)){
            addOnRoot = rootNodeFromDirectoryTree(path);
        } else {
            addOnRoot = null;
        }
        if(addOnRoot == null){
            return modelNode;
        }
        return addOnRoot;
    }
    public static PathNode rootNodeFromDirectoryTree(Path path){
        return PathWalker.getInstance(path, true, 12).walk().getTreeRootNode();
    }
    public static PathNode rootNodeFromAddOnArchive(Path path) {
        if (path == null) {
            return null;
        }

        try (PackedFile packedFile = new PackedFile(path.toFile())) {
            packedFile.getPaths().forEach(string -> ACache.put(string, packedFile.getExplodedFile(string)));
        }

        try (FileSystem fs = FileSystems.newFileSystem(path, Constants.FILESYSTEM_ENV)) {
            PathNode rootNode = new PathNode("/", true);
            List<PathNode> rootList = new ArrayList<>();
            for (Path rootDir : fs.getRootDirectories()) {
                rootList.add(PathWalker.getInstance(rootDir, true, 12).walk().getTreeRootNode());
            }
            if (rootList.size() == 1) {
                rootNode = (PathNode)  rootList.getFirst().getChildAt(0);
            } else {
                for (PathNode node : rootList) {
                    rootNode.add((PathNode) node.getChildAt(0));
                }
            }
            return rootNode;
        } catch (IOException ioe) {
            log.info(ioe.getLocalizedMessage(), ioe);
        }
        return null;
    }
}
