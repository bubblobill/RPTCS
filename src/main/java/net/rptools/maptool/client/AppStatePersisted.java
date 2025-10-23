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
package net.rptools.maptool.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Keeps track of application state between runs.
 *
 * <p>This started as an offshoot of {@link net.rptools.maptool.client.AppPreferences}, hence why
 * the same node is used. But these keys do not represent preferences of the user, rather they are
 * application state such as MRU lists the need to be remembered between runs. This also makes it
 * different from {@link net.rptools.maptool.client.AppState} which is application state that is not
 * preserved between runs.
 */
public class AppStatePersisted {
  private static final Logger log = LogManager.getLogger(AppStatePersisted.class);
  private static final Preferences prefs =
      Preferences.userRoot().node(AppConstants.APP_NAME + "/prefs");

  /**
   * Defines the key constant for retrieving asset roots.
   *
   * <p>This constant is used to define the key for accessing asset roots in a configuration file or
   * a data source. Asset roots represent the directories or paths where application assets are
   * stored.
   *
   * <p>The asset roots can be used to locate and load files, images, or other resources required by
   * the application at runtime. By convention, the asset root directories are organized in a
   * structured manner to facilitate easy retrieval of assets.
   */
  private static final String KEY_ASSET_ROOTS = "assetRoots";

  /** Represents the key used to access the most recently used campaigns for the menu option. */
  private static final String KEY_MRU_CAMPAIGNS = "mruCampaigns";

  // When hill VBL was introduced, older versions of MapTool were unable to read the new topology
  // modes. So we use a different preference key than in the past so older versions would not
  // unexpectedly break.
  private static final String KEY_TOPOLOGY_TYPES = "topologyTypes";
  private static final String KEY_OLD_TOPOLOGY_DRAWING_MODE = "topologyDrawingMode";
  private static final String DEFAULT_TOPOLOGY_TYPE = "VBL";

  /** Represents the key used to save the paint textures to the preferences. */
  private static final String KEY_SAVED_PAINT_TEXTURES = "savedTextures";

  public static void clearAssetRoots() {
    prefs.put(KEY_ASSET_ROOTS, "");
  }

  public static void addAssetRoot(File root) {
    String list = prefs.get(KEY_ASSET_ROOTS, "");
    if (!list.isEmpty()) {
      // Add the new one and then remove all duplicates.
      list += ";" + root.getPath();
      String[] roots = list.split(";");
      var result = new StringBuilder(list.length() + root.getPath().length() + 10);
      var rootList = new HashSet<String>(roots.length);

      // This loop ensures that each path only appears once. If there are currently
      // duplicates in the list, only the first one is kept.
      for (String r : roots) {
        if (!rootList.contains(r)) {
          result.append(';').append(r);
          rootList.add(r);
        }
      }
      list = result.substring(1);
    } else {
      list += root.getPath();
    }
    prefs.put(KEY_ASSET_ROOTS, list);
  }

  public static Set<File> getAssetRoots() {
    String list = prefs.get(KEY_ASSET_ROOTS, "");
    String[] roots = list.split(";");

    var rootList = new HashSet<File>();
    for (String root : roots) {
      File file = new File(root);

      // LATER: Should this actually remove it from the pref list ?
      if (!file.exists()) {
        continue;
      }
      rootList.add(file);
    }
    return rootList;
  }

  public static void removeAssetRoot(File root) {
    String list = prefs.get(KEY_ASSET_ROOTS, "");
    if (!list.isEmpty()) {
      // Add the new one and then remove all duplicates.
      String[] roots = list.split(";");
      var result = new StringBuilder(list.length());
      var rootList = new HashSet<String>(roots.length);
      String rootPath = root.getPath();

      // This loop ensures that each path only appears once. If there are
      // duplicates in the list, only the first one is kept.
      for (String r : roots) {
        if (!r.equals(rootPath) && !rootList.contains(r)) {
          result.append(';').append(r);
          rootList.add(r);
        }
      }
      list = result.substring(result.isEmpty() ? 0 : 1);
      prefs.put(KEY_ASSET_ROOTS, list);
    }
  }

}
