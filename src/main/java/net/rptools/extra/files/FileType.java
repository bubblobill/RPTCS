package net.rptools.extra.files;

import com.google.common.net.MediaType;
import net.rpTools.aoTool.app.Constants;
import net.rpTools.aoTool.asset.Asset.Type;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FileType {
    private static final Logger log = LogManager.getLogger(FileType.class);
    public static final MediaType UNKNOWN = MediaType.create(MediaType.ANY_APPLICATION_TYPE.type(), "unknown");
    public static final MimeLookup MIME_LOOKUP = new MimeLookup();
    public static final Map<MediaType, Type> TYPE_EQUIVALENTS = new HashMap<>() {{
        put(MediaType.ANY_IMAGE_TYPE, Type.IMAGE);
        put(MediaType.ANY_AUDIO_TYPE, Type.AUDIO);
        put(MediaType.CSS_UTF_8, Type.CSS);
        put(MediaType.HTML_UTF_8, Type.HTML);
        put(MediaType.JSON_UTF_8, Type.JSON);
        put(MediaType.JAVASCRIPT_UTF_8, Type.JAVASCRIPT);
        put(MediaType.ZIP, Type.MTLIB);
        put(MediaType.MD_UTF_8, Type.MARKDOWN);
        put(MediaType.PLAIN_TEXT_UTF_8, Type.TEXT);
        put(MediaType.APPLICATION_XML_UTF_8, Type.XML);
        put(UNKNOWN, Type.INVALID);
    }};

    /**
     * Lazy method that uses the file extension with no verification.
     *
     * @param extension File extension
     * @return MediaType [Defaults to {@link #UNKNOWN}]
     */
    public static MediaType getMediaTypeFromFileExtension(String extension) {
        try {
            return MediaType.parse(MIME_LOOKUP.getContentTypeFor("." + extension));
        } catch (NullPointerException | IllegalArgumentException ignored) {
            return UNKNOWN;
        }
    }

    public static MediaType getMediaType(String filename, InputStream is) throws IOException {
        MediaType mediaType = MIME_LOOKUP.getMimeTypeFor(filename);
        /* Workaround for Javascript files as Matlab scripts */
        if ("text/x-matlab".equals(mediaType.toString())) {
            String ext = FilenameUtils.getExtension(filename);
            if ("js".equals(ext) || "javascript".equals(ext))
                mediaType = MediaType.create("text", "javascript");
        }
        if (mediaType.equals(UNKNOWN)) {
            try {
                mediaType = MediaType.parse(URLConnection.guessContentTypeFromStream(is));
            } catch (IOException ioe) {
                throw new IOException("Failed to get media type - IO Error: " + ioe.getLocalizedMessage(), ioe);
            }
        }
        return mediaType;
    }

    /**
     * Detects and returns the {@link MediaType} for a byte array.
     *
     * @param filename the name of the file that the bytes were read from.
     * @param bytes    the bytes to determine the {@link MediaType} of.
     * @return the detected {@link MediaType}.
     * @throws IOException when an error occurs.
     */
    public static MediaType getMediaType(String filename, byte[] bytes) throws IOException {
        return getMediaType(filename, new ByteArrayInputStream(bytes));
    }

    /**
     * Detects and returns the {@link MediaType} for a {@link URL}.
     *
     * @param url the {@link URL} to determine the {@link MediaType} of.
     * @return the detected {@link MediaType}.
     * @throws IOException when an error occurs.
     */
    public static MediaType getMediaType(URL url) throws IOException {
        return getMediaType(url.getFile(), url.openStream());
    }

    public static MediaType assetTypeToMediaType(Type type) {
        if (TYPE_EQUIVALENTS.containsValue(type)) {
            for (Map.Entry<MediaType, Type> e : TYPE_EQUIVALENTS.entrySet()) {
                if (e.getValue().equals(type)) {
                    return e.getKey();
                }
            }
        }
        return UNKNOWN;
    }

    public static Type mediaTypeToAssetType(MediaType type) {
        return TYPE_EQUIVALENTS.getOrDefault(type, Type.INVALID);
    }

    /**
     * Mostly just uses system method but intercepts RPTools types to return "application/zip"
     */
    public static class MimeLookup implements FileNameMap {
        private static final FileNameMap MIME_TABLE = URLConnection.getFileNameMap();
        static final Properties RPT_Types = new Properties();

        static {
            for (String ext : Constants.MT_FILE_TYPES) {
                RPT_Types.setProperty(ext, MediaType.ZIP.toString());
            }
        }

        public MediaType getMimeTypeFor(String fileName) {
            try {
                return MediaType.parse(getContentTypeFor(fileName));
            } catch (IllegalArgumentException | NullPointerException e) {
                log.info("Failed to get media type - IO Error: {}", e.getLocalizedMessage(), e);
                return UNKNOWN;
            }
        }

        @Override
        public String getContentTypeFor(String fileName) {
            String extension = Util.getFileExtension(fileName);
            String type = RPT_Types.getProperty(extension);
            if (type == null) {
                type = MIME_TABLE.getContentTypeFor(fileName);
                if (type.equals(MediaType.OCTET_STREAM.toString())) {
                    try {
                        type = URLConnection.guessContentTypeFromStream(new FileInputStream(fileName));
                    } catch (IOException ioException) {
                        log.info(ioException.getLocalizedMessage(), ioException);
                        type = UNKNOWN.toString();
                    }
                }
            }
            return type;
        }
    }
}
