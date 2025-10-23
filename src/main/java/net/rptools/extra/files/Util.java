package net.rptools.extra.files;

import net.rpTools.aoTool.app.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static javax.swing.JOptionPane.ERROR_MESSAGE;

public class Util {
    private static final Logger log = LogManager.getLogger(Util.class);

    public static byte[] loadResource(String resource) throws IOException {
        try (InputStream is = Util.class.getClassLoader().getResourceAsStream(resource)) {
            if (is == null) {
                throw new IOException("Resource \"" + resource + "\" cannot be opened as stream.");
            }
            return IOUtils.toByteArray(
                    new InputStreamReader(is, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        }
    }
    public static InputStream getResourceAsStream(String res) {
        return Util.class.getClassLoader().getResourceAsStream(res);
    }

    public static void saveResource(String resource, File destDir) throws IOException {
        int index = resource.lastIndexOf('/');
        String filename = index >= 0 ? resource.substring(index + 1) : resource;
        saveResource(resource, destDir, filename);
    }

    public static void saveResource(String resource, File destDir, String filename)
            throws IOException {
        File outFilename = new File(destDir, filename);
        try (InputStream inStream = Util.class.getResourceAsStream(resource);
             OutputStream outStream = new BufferedOutputStream(new FileOutputStream(outFilename))) {
            IOUtils.copy(inStream, outStream);
        }
    }

    public static byte[] getBytes(URL url) throws IOException {
        try (InputStream is = url.openStream()) {
            return IOUtils.toByteArray(is);
        }
    }
    public static String getFileExtension(String fileName){
        if(fileName.contains(".")) {
            return fileName.substring(Math.min(
                    fileName.lastIndexOf(".") + 1,
                    fileName.length())).toLowerCase();
        }
        return "";
    }
    /**
     * Returns the file with its name modified to add the extension if it doesn't have already.
     *
     * @param file the file that might need the extension
     * @param extension the extension to add, if it is missing
     * @return the file with the correct extension
     */
    public static File getFileWithExtension(File file, String extension) {
        if (file.getName().endsWith(extension)) {
            return file;
        } else {
            return new File(file.getAbsolutePath() + extension);
        }
    }
    public static String getNameWithoutExtension(File file) {
        return getNameWithoutExtension(file.getName());
    }

    public static String getNameWithoutExtension(String filename) {
        if (filename == null) {
            return null;
        }
        Matcher matcher = Constants.TRIM_EXTENSION_PATTERN.matcher(filename);
        if (!matcher.matches()) {
            return filename;
        }
        return matcher.group(1);
    }

    public static String getNameWithoutExtension(URL url) {
        String file = url.getFile();
        try {
            file = url.toURI().getPath();
        } catch (URISyntaxException e) {
            // If the conversion doesn't work, ignore it and use the original file name.
        }
        return getNameWithoutExtension(new File(file));
    }
    public static List<String> getLines(File file) throws IOException {
        try (FileReader fr = new FileReader(file)) {
            return IOUtils.readLines(fr);
        }
    }

    /**
     * Given an InputStream this method tries to figure out what the content type might be.
     *
     * @param in the InputStream to check
     * @return a <code>String</code> representing the content type name
     */
    public static String getContentType(InputStream in) {
        String type = "";
        try {
            type = URLConnection.guessContentTypeFromStream(in);
            log.debug("result from guessContentTypeFromStream() is {}", type);
        } catch (IOException e) {
        }
        return type;
    }

    /**
     * Given a URL this method tries to figure out what the content type might be based only on the
     * filename extension.
     *
     * @param url the URL to check
     * @return a <code>String</code> representing the content type name
     */
    public static String getContentType(URL url) {
        String type = "";
        type = URLConnection.guessContentTypeFromName(url.getPath());
        log.debug("result from guessContentTypeFromName({}) is {}", url.getPath(), type);
        return type;
    }

    /**
     * Given a <code>File</code> this method tries to figure out what the content type might be based
     * only on the filename extension.
     *
     * @param file the File to check
     * @return a <code>String</code> representing the content type name
     */
    public static String getContentType(File file) {
        if(Constants.MT_FILTER.accept(file)){
            return "zip";
        }
        try {
            return getContentType(file.toURI().toURL());
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * Returns a {@link BufferedReader} from the given {@code File} object. The contents of the file
     * are expected to be UTF-8.
     *
     * @param file the input data source
     * @return a String representing the data
     * @throws IOException in case of an I/O error
     */
    public static BufferedReader getFileAsReader(File file) throws IOException {
        return new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
    }

    public static InputStream getFileAsInputStream(File file) throws IOException {
        return getURLAsInputStream(file.toURI().toURL());
    }

    /**
     * Given a URL this method determines the content type of the URL (if possible) and then returns
     * an InputStream.
     *
     * @param url the source of the data stream
     * @return InputStream representing the data
     * @throws IOException in case of an I/O error
     */
    public static InputStream getURLAsInputStream(URL url) throws IOException {
        InputStream is = null;
        URLConnection conn = null;
        // We're assuming character here, but it could be bytes. Perhaps we should
        // check the MIME type returned by the network server?
        conn = url.openConnection();
        if (log.isDebugEnabled()) {
            String type = URLConnection.guessContentTypeFromName(url.getPath());
            log.debug("result from guessContentTypeFromName(" + url.getPath() + ") is " + type);
            type = getContentType(conn.getInputStream());
            log.debug("result from getContentType(" + url.getPath() + ") is " + type);
        }
        is = conn.getInputStream();
        return is;
    }

    /**
     * Copies <code>sourceFile</code> to <code>destFile</code> overwriting as required, and <b>not</b>
     * preserving the source file's last modified time. The destination directory is created if it
     * does not exist, and if the destination file exists, it is overwritten.
     *
     * @param sourceFile the source file
     * @param destFile the destination file
     * @throws IOException in case of an I/O error
     */
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        FileUtils.copyFile(sourceFile, destFile, false);
    }

    /**
     * Unzips the indicated file from the <code>classpathFile</code> location into the indicated
     * <code>destDir</code>.
     *
     * @param classpathFile The resource name
     * @param destDir the destination directory
     * @throws IOException in case of an I/O error
     */
    public static void unzip(String classpathFile, File destDir) throws IOException {
        try {
            unzip(Util.class.getClassLoader().getResource(classpathFile), destDir);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Loads the given {@link URL}, and unzips the URL's contents into the given <code>destDir</code>.
     *
     * @param url the url to load
     * @param destDir the destination directory to save the files in
     * @throws IOException in case of an I/O error
     */
    public static void unzip(URL url, File destDir) throws IOException {
        if (url == null) throw new IOException("URL cannot be null");

        InputStream is = url.openStream();
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is))) {
            unzip(zis, destDir);
        }
    }

    public static void unzip(ZipInputStream in, File destDir) throws IOException {
        if (in == null) throw new IOException("input stream cannot be null");

        // Prepare destination
        destDir.mkdirs();
        File absDestDir = destDir.getAbsoluteFile();

        // Pull out the files
        ZipEntry entry = null;
        while ((entry = in.getNextEntry()) != null) {
            if (entry.isDirectory()) continue;

            // Prepare file destination
            File entryFile = new File(absDestDir, entry.getName());
            entryFile.getParentFile().mkdirs();

            try (OutputStream out = new FileOutputStream(entryFile)) {
                IOUtils.copy(in, out);
            }
            in.closeEntry();
        }
    }

    public static void unzipFile(File sourceFile, File destDir) throws IOException {
        if (!sourceFile.exists()) throw new IOException("source file does not exist: " + sourceFile);

        // Canonicalize destination directory path
        File canonicalDestDir = destDir.getCanonicalFile();

        try (ZipFile zipFile = new ZipFile(sourceFile)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;

                // Create file object and validate path
                File file = new File(canonicalDestDir, entry.getName());
                File canonicalFile = file.getCanonicalFile();

                // Prevent zip slip - check if file path is within destination directory
                if (!canonicalFile.toPath().startsWith(canonicalDestDir.toPath())) {
                    throw new IOException("Entry is outside of the target directory: " + entry.getName());
                }

                // Create parent directories
                file.getParentFile().mkdirs();

                // Extract file content
                try (InputStream is = zipFile.getInputStream(entry);
                     OutputStream os = new BufferedOutputStream(new FileOutputStream(canonicalFile))) {
                    IOUtils.copy(is, os);
                }
            }
        }
    }

    /**
     * Copies all bytes from InputStream to OutputStream without closing either stream.
     *
     * @deprecated not in use. Use {@link IOUtils#copy(InputStream, OutputStream)} instead.
     * @param is the InputStream to read from
     * @param os the OutputStream to write to
     * @throws IOException in case of an I/O error
     */
    @Deprecated
    public static void copyWithoutClose(InputStream is, OutputStream os) throws IOException {
        IOUtils.copy(is, os);
    }

    /**
     * Copies all bytes from InputStream to OutputStream, and close both streams before returning.
     *
     * @deprecated not in use. Use {@link IOUtils#copy(InputStream, OutputStream)} instead and
     *     try-with-resources
     * @param is input stream to read data from.
     * @param os output stream to write data to.
     * @throws IOException in case of an I/O error
     */
    @Deprecated
    public static void copyWithClose(InputStream is, OutputStream os) throws IOException {
        try {
            IOUtils.copy(is, os);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    /**
     * Recursively deletes all files and/or directories that have been modified longer than <code>
     * daysOld</code> days ago. Note that this will recursively examine a directory, and only deletes
     * those items that are too old.
     *
     * @param file the file or directory to recursively check and possibly delete
     * @param daysOld number of days old a file or directory can be before it is considered for
     *     deletion
     */
    public static void delete(File file, int daysOld) {
        Calendar olderThan = new GregorianCalendar();
        olderThan.add(Calendar.DATE, -daysOld);

        boolean shouldDelete = new Date(file.lastModified()).before(olderThan.getTime());

        if (file.isDirectory()) {
            // Wipe the contents first
            for (File currfile : file.listFiles()) {
                if (".".equals(currfile.getName()) || "..".equals(currfile.getName())) continue;
                delete(currfile, daysOld);
            }
        }
        if (shouldDelete) file.delete();
    }

    /**
     * Recursively deletes the given file or directory
     *
     * @see FileUtils#deleteQuietly(File)
     * @param file to recursively delete
     */
    public static void delete(File file) {
        FileUtils.deleteQuietly(file);
    }

    /**
     * Replace invalid File name characters, useful for token Save function to replace the : in Lib
     * tokens.
     *
     * @author Jamz
     * @since 1.4.0.2
     * @param fileName the name of the file
     * @return the fileName with invalid characters replaced by _a
     */
    public static String stripInvalidCharacters(String fileName) {
        return fileName = fileName.replaceAll("[^\\w\\s.,-]", "_");
    }
    public static Path getNodeFilePath(DefaultMutableTreeNode node){
        try {
            Path path;
            switch (node.getUserObject()) {
                case File file -> path = file.toPath();
                case Path p -> path = p;
                case String s -> path = Path.of(s);
                case FileSystem fs -> path = fs.getRootDirectories().iterator().next();
                default -> {
                    path = null;
                    log.info("Unexpected user object: {}", node.getUserObject().getClass().getSimpleName());
                }
            }
            if(path != null) {
                return path.toRealPath();
            }
            return null;
        } catch (IOException ioe) {
            log.info("Unable to resolve path: ", ioe);
            return null;
        }
    }
    public static String getCleanTreePathString(TreePath treePath) {
        /*
         * The above treePath returns path as [Computer, C:\, Home, User, test.txt] when converted via toString()
         * for folder names which end with ], throw a runtime error as ]] is not expected in the end of the string in replaceFirst() regex
         * Hence below the path string is checked for ] as the last character
         * if yes, it is removed via substring
         */
        String s = treePath.toString();
        if (s.charAt(s.length() - 1) == ']') {
            s = s.substring(0, s.length() - 1);
        }

        //below replaceFirst(), replaces any character in the set (open square bracket, close square bracket) with the empty string. The \\ is to escape the square brackets within the set
        s = s.replaceFirst("[\\[\\]]", "");
        s = s.replace(", ", File.separator); //', ' is replaced with '\'
        s = s.replace("Computer" + File.separator, ""); //substring starts at 9 to skip the 'Computer\' at start of the string, as Computer was defined by us and is not a valid path.
        return s;
    }
    public static int loc2IndexFileList(JList<?> list, Point point) {
        int index = list.locationToIndex(point);
        if (index != -1) {
            Object bySize = list.getClientProperty("List.isFileList");
            if (bySize instanceof Boolean && (Boolean) bySize &&
                    !pointIsInActualBounds(list, index, point)) {
                index = -1;
            }
        }
        return index;
    }
    private static <T> boolean pointIsInActualBounds(JList<T> list, int index,
                                                     Point point) {
        ListCellRenderer<? super T> renderer = list.getCellRenderer();
        T value = list.getModel().getElementAt(index);
        Component item = renderer.getListCellRendererComponent(list,
                value, index, false, false);
        Dimension itemSize = item.getPreferredSize();
        Rectangle cellBounds = list.getCellBounds(index, index);
        if (!item.getComponentOrientation().isLeftToRight()) {
            cellBounds.x += (cellBounds.width - itemSize.width);
        }
        cellBounds.width = itemSize.width;

        return cellBounds.contains(point);
    }
    public static void alert(String message, Exception e){
        JOptionPane optionPane = new JOptionPane(message, ERROR_MESSAGE);
        optionPane.getRootPane().getContentPane().add(new JLabel(e.getLocalizedMessage()));
        optionPane.setVisible(true);
    }
}
