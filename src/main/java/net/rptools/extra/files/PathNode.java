package net.rptools.extra.files;

import net.rpTools.aoTool.asset.MD5Key;
import org.apache.logging.log4j.util.Strings;
import org.pushingpixels.radiance.theming.api.renderer.RadianceDefaultTreeCellRenderer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.List;

//@formatter:off
public class PathNode extends DefaultMutableTreeNode {
    public static class PathNodeRenderer extends RadianceDefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel c = (JLabel) super.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus);
//            c.setOpaque(false);
            if(value instanceof PathNode node){
                Path p = node.getFilePath();
                Icon i = node.getFileIcon();
                boolean isVirtualNode = node.isVirtualNode();
                if(i != null){
                    if(isVirtualNode){
                        c.setIcon(UIManager.getLookAndFeel().getDisabledIcon(c, i));
                    } else {
                        c.setIcon(i);
                    }
                }
                if(p != null){
                    if(p.getFileName() != null){
                        c.setText((isVirtualNode ? "+ " : "") + p.getFileName());
                    }
                } else if(node.getUserObject() instanceof String string){
                    c.setText(string);
                }
            }
            return c;
        }
    }
    Path filePath = null;
    String filePathString = null;
    MD5Key assetKey = null;
    BasicFileAttributes fileAttributes = null;
    Icon fileIcon = null;
    boolean virtualNode = false;
    public PathNode() { this(""); }
    public PathNode(String string){ this(Path.of(string)); }
    public PathNode(String string, boolean allowsChildren){ this(Path.of(string), allowsChildren); }
    public PathNode(Path p) { this(p, Files.isDirectory(p)); }
    public PathNode(Path p, boolean allowsChildren) {
        super(null, allowsChildren);
        filePath = p; //!= null ? f : new File(SystemProps.get(SystemProps.TEMP_DIR), new GUID() + ".tmp");
    }
    public PathNode(String p, boolean allowsChildren, Icon icon){
        super(null, allowsChildren);
        filePath = Path.of(p);
        fileIcon = icon;
        virtualNode = true;
    }
    public PathNode(Path p, BasicFileAttributes attributes, Icon icon){
        this(p, attributes.isDirectory());
        fileAttributes = attributes;
        fileIcon = icon;
    }
    public PathNode(PathNode pathNode) {
        super();
        this.filePath = pathNode.getFilePath();
        this.filePathString = pathNode.getFilePathString();
        this.fileAttributes = pathNode.getFileAttributes();
        this.fileIcon = pathNode.getFileIcon();
        this.virtualNode = pathNode.isVirtualNode();
        this.children = pathNode.children;
    }
    public Path getPathToRoot() {
        return Path.of(getPathToRootString());
    }
    public String getPathToRootString() {
        List<PathNode> parents = new ArrayList<>();
        for(TreeNode tn: super.getPathToRoot(this, this.getDepth())){
            parents.add((PathNode) tn);
        }
        return Strings.join(parents, '/');
    }
    @Override
    public void add(MutableTreeNode newChild) {
        super.add(newChild);
        children.sort((o1, o2) -> {
            Path p1 = ((PathNode) o1).getFilePath();
            Path p2 = ((PathNode) o2).getFilePath();
            return p1.compareTo(p2);
        });
        children.sort((o1, o2) -> {
            boolean leaf1 = o1.isLeaf();
            boolean leaf2 = o2.isLeaf();
            return Boolean.compare(leaf1, leaf2) ;
        });
    }
    public void setFilePath(String filePath) {
        try{
            this.filePath = Path.of(filePath);
        } catch (Exception ignored){
            this.filePathString = filePath;
        }
    }
    public void setFilePath(Path filePath) { this.filePath = filePath; }
    public Icon getFileIcon(){ return fileIcon; }
    public void setFileIcon(Icon icon){ fileIcon = icon; }
    public BasicFileAttributes getFileAttributes(){ return (BasicFileAttributes)fileAttributes; }
    public Path getFilePath(){ return filePath; }
    public String getFilePathString(){ return filePathString; }
    public boolean isVirtualNode(){ return virtualNode; }
    public void setVirtualNode(boolean isVirtualNode){ this.virtualNode = isVirtualNode; }
    public MD5Key getAssetKey(){ return assetKey; }
    public void setAssetKey(MD5Key key){ this.assetKey = key; }
    @Override public Object getUserObject() { return filePath; }
    @Override public void setUserObject(Object path) { filePath = (Path) path; }
    public List<PathNode> getChildNodesAsList(){
        return Arrays.stream(children.toArray(TreeNode[]::new))
                .sequential()
                .map(treeNode -> (PathNode) treeNode)
                .toList();
    }
    public Enumeration<PathNode> getPathNodeChildren() { return Collections.enumeration(getChildNodesAsList()); }
    //@formatter:on

    public PathNode getDescendantByFilePath(final String fileNameOrPath) {
        Iterator<TreeNode> it = this.breadthFirstEnumeration().asIterator();
        while (it.hasNext()) {
            PathNode node = (PathNode) it.next();
            String filePath = node.getPathToRoot().toString();
            if (node.getFilePath() != null) {
                if (node.getFilePath().toString().toLowerCase().equalsIgnoreCase(fileNameOrPath) ||
                        node.getFilePath().toString().toLowerCase().endsWith(fileNameOrPath)) {
                    return node;
                }
            }
            if(node.getFilePathString() != null) {
                if (node.getFilePathString().toLowerCase().equalsIgnoreCase(fileNameOrPath) ||
                        node.getFilePathString().toLowerCase().endsWith(fileNameOrPath)) {
                    return node;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "PathNode{" +
//                "fileAttributes=" + fileAttributes +
                ", filePath=" + filePath +
//                ", fileIcon=" + fileIcon +
//                ", virtualNode=" + virtualNode +
//                ", allowsChildren=" + allowsChildren +
                '}';
    }
}
