package net.rptools.extra.files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

//https://medium.com/@AlexanderObregon/javas-files-walkfiletree-method-explained-6660bebfa626
public class DirectoryWalker{
    private static final Logger log = LogManager.getLogger(DirectoryWalker.class);
    private static final FileSystemView FSV = FileSystemView.getFileSystemView();
    private final FileSystem fileSystem = FileSystems.getDefault();
    public static DirectoryWalker getInstance(FileSystem fileSystem){
        return new DirectoryWalker(fileSystem);
    }
    public static DirectoryWalker getInstance(Path p){
        return new DirectoryWalker(p);
    }
    public static DirectoryWalker getInstance(Path p, boolean followLinks, int depth){
        return new DirectoryWalker(p, followLinks, depth);
    }
    public static DirectoryWalker getInstance(LazyNode rootNode, Path p, boolean followLinks, int depth){
        return new DirectoryWalker(rootNode, p, followLinks, depth);
    }
    public static DirectoryWalker getInstance(LazyNode rootNode, boolean followLinks, int depth){
        return new DirectoryWalker(rootNode, followLinks, depth);
    }

    private final Path startPath;
    private LazyNode root;
    private final int depth;
    private final Set<FileVisitOption> followLinks;
    public DirectoryWalker(FileSystem fileSystem){
        startPath = fileSystem.getRootDirectories().iterator().next();
        this.depth = 8;
        this.followLinks = Set.of(FileVisitOption.FOLLOW_LINKS);
    }
    public DirectoryWalker(LazyNode root, Path startPath, boolean followLinks, int depth){
        this.startPath = startPath;
        this.root = root;
        this.depth = depth;
        if(followLinks){
            this.followLinks = Set.of(FileVisitOption.FOLLOW_LINKS);
        } else {
            this.followLinks = Collections.emptySet();
        }
    }
    public DirectoryWalker(LazyNode startNode, boolean followLinks, int depth){
        this(startNode, Util.getNodeFilePath(startNode), followLinks, depth);
    }
    public DirectoryWalker(Path startPath, boolean followLinks, int depth){
        this(new LazyNode(startPath.toFile(), true), startPath, followLinks, depth);
    }
    public DirectoryWalker(Path startPath){
        this(startPath, true, 8);
    }
    public DirectoryWalker walk(){
        List<LazyNode> nodeList = new LinkedList<>();

        try {
            Files.walkFileTree(startPath, followLinks, depth, new SimpleFileVisitor<>(){
                @Override
                public FileVisitResult postVisitDirectory(@Nonnull Path path, @Nullable IOException exc) throws IOException {
                    if(nodeList.size() > 1){
                        LazyNode node = nodeList.removeLast();
                        nodeList.getLast().add(node);
                    }
                    return super.postVisitDirectory(path, exc);
                }

                @Override
                public FileVisitResult preVisitDirectory(@Nonnull Path path, @Nonnull BasicFileAttributes attrs) throws IOException {
                    LazyNode node = new LazyNode(path.toFile(), true);
                    nodeList.add(node);
                    return super.preVisitDirectory(path, attrs);
                }

                @Override
                public FileVisitResult visitFile(@Nonnull Path path, @Nonnull BasicFileAttributes attrs) throws IOException {
                    File file = path.toFile();
                    if (!FSV.isHiddenFile(file)) {
                        return super.visitFile(path, attrs);
                    }
                    LazyNode node;
                    if (attrs.isSymbolicLink() && followLinks.isEmpty()) {
                        node = new LazyNode(FSV.getLinkLocation(file));
                    } else {
                        node = new LazyNode(file);
                    }
                    nodeList.getLast().add(node);
                    return super.visitFile(path, attrs);
                }

                @Override
                public FileVisitResult visitFileFailed(@Nonnull Path path, @Nullable IOException exc) throws IOException {
                    if (exc instanceof FileSystemLoopException) {
                        log.info("cycle detected: {}", path);
                    } else if (exc instanceof AccessDeniedException) {
                        log.info("access denied: {}, owner: {}", path, Files.getOwner(path));
                    } else {
                        log.info(exc.getLocalizedMessage(), exc);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            for(LazyNode node: nodeList){
                if(root.isNodeDescendant(node)){
                    root.add(node);
                } else {
                    node.add(root);
                    DefaultTreeModel treeModel = new DefaultTreeModel(node);
                    root = (LazyNode) treeModel.getRoot();
                }
            }
            root.setKidsLoading(false);
            root.setKidsLoaded(true);
        } catch (IOException e) {
            log.info("Error traversing directory: {}", e.getMessage());
        }
        return this;
    }
    public LazyNode getTreeNode(){
        return root;
    }
}