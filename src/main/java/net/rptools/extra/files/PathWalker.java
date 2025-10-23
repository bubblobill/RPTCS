package net.rptools.extra.files;

import net.rpTools.aoTool.app.cache.ACache;
import net.rpTools.aoTool.app.preference.Preferences;
import net.rpTools.aoTool.asset.Asset;
import net.rpTools.aoTool.asset.MD5Key;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

//https://medium.com/@AlexanderObregon/javas-files-walkfiletree-method-explained-6660bebfa626
public class PathWalker {
    private static final Logger log = LogManager.getLogger(PathWalker.class);
    private static final FileSystemView FSV = FileSystemView.getFileSystemView();
    private static final int ICON_SIZE = Preferences.getInt(Preferences.Key.TREE_ICON_SIZE);

    public static PathWalker getInstance(FileSystem fileSystem) {
        return new PathWalker(fileSystem);
    }

    public static PathWalker getInstance(Path p) {
        return new PathWalker(p);
    }

    public static PathWalker getInstance(Path p, boolean followLinks, int depth) {
        return new PathWalker(p, followLinks, depth);
    }

    public static PathWalker getInstance(PathNode rootNode, Path p, boolean followLinks, int depth) {
        return new PathWalker(rootNode, p, followLinks, depth);
    }

    private final Path startPath;
    private final PathNode root;
    private final int depth;
    private final Set<FileVisitOption> followLinks;

    public PathWalker(FileSystem fileSystem) {
        startPath = fileSystem.getRootDirectories().iterator().next();
        root = new PathNode(startPath, true);
        this.depth = 8;
        this.followLinks = Set.of(FileVisitOption.FOLLOW_LINKS);
    }

    public PathWalker(PathNode root, Path startPath, boolean followLinks, int depth) {
        this.startPath = startPath;
        if (startPath.toUri().getScheme().equalsIgnoreCase("jar")
                || startPath.toUri().getScheme().equalsIgnoreCase("zip")) {
            root.setFilePath(getZipPath(startPath));
        }

        this.root = root;
        this.depth = depth > -1 ? depth : Integer.MAX_VALUE;
        if (followLinks) {
            this.followLinks = Set.of(FileVisitOption.FOLLOW_LINKS);
        } else {
            this.followLinks = Collections.emptySet();
        }
    }

    public PathWalker(Path startPath, boolean followLinks, int depth) {
        this(new PathNode(startPath, true), startPath, followLinks, depth);
    }

    public PathWalker(Path startPath) {
        this(startPath, true, 8);
    }

    public PathWalker walk() {
        List<PathNode> nodeList = new LinkedList<>();
        nodeList.add(root);
        try {
            Files.walkFileTree(startPath, followLinks, depth, new SimpleFileVisitor<>() {
                @Override
                @Nonnull
                public FileVisitResult postVisitDirectory(@Nonnull Path path, @Nullable IOException exc) throws IOException {
                    if (nodeList.size() > 1) {
                        PathNode node = nodeList.removeLast(); // graft to parent
                        nodeList.getLast().add(node);
                    }
                    return super.postVisitDirectory(path, exc);
                }

                @Override
                @Nonnull
                public FileVisitResult preVisitDirectory(@Nonnull Path path, @Nonnull BasicFileAttributes attrs) throws IOException {
                    PathNode node = new PathNode(path, attrs, FSV.getSystemIcon(new File(path.toString()), ICON_SIZE, ICON_SIZE));
                    if (path.toString().equalsIgnoreCase("/")) {
                        try {
                            node.setFilePath(getZipPath(startPath));
                        } catch (Exception ignored) {
                        }
                    }
                    nodeList.add(node);
                    return super.preVisitDirectory(path, attrs);
                }

                @Override
                @Nonnull
                public FileVisitResult visitFile(@Nonnull Path path, @Nonnull BasicFileAttributes attrs) throws IOException {
                    File file = new File(path.toString());
                    if (FSV.isHiddenFile(file)) {
                        return super.visitFile(path, attrs);
                    }
                    PathNode node;
                    if (attrs.isSymbolicLink() && followLinks.isEmpty()) {
                        node = new PathNode(Paths.get(FSV.getLinkLocation(file).toURI()), attrs, FSV.getSystemIcon(file, ICON_SIZE, ICON_SIZE));
                    } else {
                        node = new PathNode(path, attrs, FSV.getSystemIcon(file));
                    }
                    if (attrs.isRegularFile()) {
                        byte[] targetArray = IOUtils.toByteArray(path.toUri());
                        Asset asset = Asset.createAssetDetectType(path.getFileName().toString(), targetArray, file);
                        MD5Key key = asset.getMD5Key();
                        ACache.put(key, asset);
                        node.setAssetKey(key);
                    }

                    nodeList.getLast().add(node);
                    return super.visitFile(path, attrs);
                }

                @Override
                @Nonnull
                public FileVisitResult visitFileFailed(@Nonnull Path path, @Nullable IOException exc) throws IOException {
                    if (exc instanceof FileSystemLoopException) {
                        log.info("cycle detected: {}", path);
                    } else if (exc instanceof AccessDeniedException) {
                        log.info("access denied: {}, owner: {}", path, Files.getOwner(path));
                    } else if (exc != null) {
                        log.info(exc.getLocalizedMessage(), exc);
                    }
                    return super.visitFileFailed(path, new IOException(exc));
                }
            });
        } catch (IOException e) {
            log.info("Error traversing directory: {}", e.getMessage());
        }
        return this;
    }

    public PathNode getTreeRootNode() {
        return root;
    }

    private Path getZipPath(Path path) {
        String s = path.toUri().getRawSchemeSpecificPart();
        return Paths.get(URI.create(s.substring(0, s.lastIndexOf('!'))));
    }
}