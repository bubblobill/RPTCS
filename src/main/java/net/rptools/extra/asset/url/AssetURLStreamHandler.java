/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.extra.asset.url;


import net.rpTools.aoTool.app.cache.ACache;
import net.rpTools.aoTool.asset.AssetManager;
import net.rpTools.aoTool.asset.MD5Key;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Support "asset://" in Swing components
 *
 * @author Azhrei
 */
public class AssetURLStreamHandler extends URLStreamHandler {
  private static final Logger log = LogManager.getLogger(AssetURLStreamHandler.class);
  @Override
  protected URLConnection openConnection(URL u) {
    return new AssetURLConnection(u);
  }

  public static class AssetURLConnection extends URLConnection {

    public AssetURLConnection(URL url) {
      super(url);
    }

    @Override
    public void connect() {
      // Nothing to do
    }

    @Override
    public InputStream getInputStream() throws IOException {

      if (url.getQuery() != null) {
        if (Arrays.stream(url.getQuery().split("&"))
            .anyMatch(q -> q.equalsIgnoreCase("raw=true"))) {
          var asset = AssetManager.getAssetAndWait(new MD5Key(url.getHost()));
          return new ByteArrayInputStream(asset.getData());
        }
      }

      BufferedImage img = getImageFromUrl(url);
      return new ByteArrayInputStream(imageToBytes(img, "png"));
    }
  }
  /**
   * Convert a BufferedImage to byte[] in an given format.
   *
   * @param image the buffered image.
   * @param format a String containing the informal name of the format.
   * @return the byte[] of the image.
   * @throws IOException if the image cannot be written to the output stream.
   */
  public static byte[] imageToBytes(BufferedImage image, String format) throws IOException {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream(10000);
    ImageIO.write(image, format, outStream);

    return outStream.toByteArray();
  }
  public static BufferedImage getImageFromUrl(URL url) {
    if (url == null || !url.getProtocol().equals("asset")) {
      return null;
    }

    String id = url.getHost();
    String query = url.getQuery();
    BufferedImage image;
    int imageW, imageH, scaleW = -1, scaleH = -1, size = -1;

    // Get size parameter
    int szIndex = id.indexOf('-');
    if (szIndex != -1) {
      String szStr = id.substring(szIndex + 1);
      id = id.substring(0, szIndex);
      try {
        size = Integer.parseInt(szStr);
      } catch (NumberFormatException nfe) {
        // Do nothing
      }
      if (size <= 0) {
        return null;
      }
    }

    // Get query parameters
    if (query != null && !query.isEmpty()) {
      HashMap<String, String> params = new HashMap<>();

      for (String param : query.split("&")) {
        if (param.isBlank()) continue;

        int eqIndex = param.indexOf("=");
        if (eqIndex != -1) {
          String k, v;
          k = param.substring(0, eqIndex).trim();
          v = param.substring(eqIndex + 1).trim();
          params.put(k, v);
        } else {
          params.put(param.trim(), "");
        }
      }

      if (params.containsKey("width")) {
        size = -1; // Don't use size param if width is present
        try {
          scaleW = Integer.parseInt(params.get("width"));
        } catch (NumberFormatException nfe) {
          // Do nothing
        }
        if (scaleW <= 0) {
          return null;
        }
      }

      if (params.containsKey("height")) {
        size = -1; // Don't use size param if height is present
        try {
          scaleH = Integer.parseInt(params.get("height"));
        } catch (NumberFormatException nfe) {
          // Do nothing
        }
        if (scaleH <= 0) {
          return null;
        }
      }
    }

    image = (BufferedImage) getImageAndWait(new MD5Key(id), null);
    imageW = image.getWidth();
    imageH = image.getHeight();

    // We only want to scale down, so if scaleW or ScaleH are too large just return the image
    if (scaleW > imageW || scaleH > imageH) {
      return image;
    }

    // Note: size will never be >0 if height or width parameters are present
    if (size > 0) {
      if (imageW > imageH) {
        scaleW = size;
      } else if (imageH > imageW) {
        scaleH = size;
      } else {
        scaleW = scaleH = size;
      }
    }

    if ((scaleW > 0 && imageW > scaleW) || (scaleH > 0 && imageH > scaleH)) {
      // Maintain aspect ratio if one dimension isn't given
      if (scaleW <= 0) {
        scaleW = Math.max((int) ((double) scaleH / imageH * imageW), 1);
      } else if (scaleH <= 0) {
        scaleH = Math.max((int) ((double) scaleW / imageW * imageH), 1);
      }
      image = (BufferedImage) image.getScaledInstance(scaleW, scaleH, Image.SCALE_AREA_AVERAGING);
    }

    return image;
  }
  /**
   * Loads the asset's raw image data into a buffered image, and waits for the image to load.
   *
   * @param assetId Load image data from this asset
   * @param hintMap Hints used when loading the image
   * @return BufferedImage Return the loaded image
   */
  public static Image getImageAndWait(final MD5Key assetId, Map<String, Object> hintMap) {
    if (assetId == null) {
      return null;
    }
    BufferedImage image = null;
    final CountDownLatch loadLatch = new CountDownLatch(1);
    return ACache.getImage(assetId);
  }
  /**
   * Return the image corresponding to the assetId.
   *
   * @param assetId Load image data from this asset.
   * @param observers the observers to be notified when the image loads, if it hasn't already.
   * @return the image, or null if assetId null, or null if loading.
   */
  public static Image getImage(MD5Key assetId, ImageObserver... observers) {
    if (assetId == null) {
      return null;
    }
    return ACache.getImage(assetId);
  }
}
