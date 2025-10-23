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
package net.rptools.extra.asset;

import net.rpTools.aoTool.app.cache.ACache;
import net.rpTools.aoTool.files.Util;
import net.rpTools.aoTool.threading.ThreadPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

public class AssetLoader {
  private static final Logger log = LogManager.getLogger(AssetLoader.class);

  public enum RepoState {
    ACTIVE,
    BAD_URL,
    INDX_BAD_FORMAT,
    UNAVAILABLE
  }

  /** Length of time, in ms, before a repo index file is deemed out-of-date. */
  private static final long INDEX_LIFESPAN = TimeUnit.DAYS.toMillis(1);

  private final ExecutorService retrievalThreadPool = ThreadPool.getThreadPool();
  private final Set<MD5Key> requestedIdSet = new HashSet<>();


  /**
   * Check whether there is currently a pending request for the given asset ID.
   *
   * <p>NOTE: This may be immediately incorrect by the time this function returns so decisions on
   * whether to request an asset must not depend on the result.
   *
   * @return true if there is an incomplete request.
   */
  public synchronized boolean isIdRequested(MD5Key id) {
    return requestedIdSet.contains(id);
  }

  protected List<String> decode(byte[] indexData) throws IOException {
    BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(new GZIPInputStream(new ByteArrayInputStream(indexData))));

    List<String> list = new ArrayList<>();

    String line = null;
    while ((line = reader.readLine()) != null) {
      list.add(line);
    }
    return list;
  }

  protected Map<String, String> parseIndex(List<String> index) {
    Map<String, String> idxMap = new HashMap<>();

    for (String line : index) {
      if (line == null) {
        continue;
      }
      line = line.trim();
      if (line.isEmpty()) {
        continue;
      }

      String id = line.substring(0, 32);
      String ref = line.substring(33).trim();

      idxMap.put(id, ref);
    }
    return idxMap;
  }

  /**
   * Submit a new request to retrieve the asset with the given ID if needed.
   *
   * @param id The ID of the asset to request.
   * @return false if there was already a request, true if a new request was submitted.
   */
  public synchronized boolean requestAsset(MD5Key id) {
    if (requestedIdSet.contains(id)) {
      return false;
    }

    retrievalThreadPool.submit(new ImageRetrievalRequest(id));
    requestedIdSet.add(id);
    return true;
  }
  public synchronized void completeRequest(MD5Key id) {
    requestedIdSet.remove(id);
  }
  private class ImageRetrievalRequest implements Runnable {
    Object id;

    public ImageRetrievalRequest(Object key) {
      this.id = id;
    }

    public void run() {
      try {
        Image img = ACache.getImage(id);
        if(img != null) {
          completeRequest((MD5Key) id);
        } else {
          File f = Path.of(id.toString()).toFile();
          byte[] data = Util.getBytes(Path.of(id.toString()).toUri().toURL());
          if(data.length > 0){
            MD5Key k = new MD5Key(data);
            ACache.put(k, data);
          }
        }
      } catch (IOException e) {
          throw new RuntimeException(e);
      }
    }
  }
}
