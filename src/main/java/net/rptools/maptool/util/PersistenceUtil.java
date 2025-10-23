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
package net.rptools.maptool.util;

import com.google.protobuf.util.JsonFormat;
import com.thoughtworks.xstream.XStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import net.rptools.lib.MD5Key;
import net.rptools.lib.ModelVersionManager;

import net.rptools.lib.io.PackedFile;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.Asset.Type;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.model.library.addon.AddOnLibrary;
import net.rptools.maptool.model.library.addon.AddOnLibraryImporter;
import net.rptools.maptool.model.library.proto.AddOnLibraryListDto;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** This class provides utility methods for persistence operations in the application. */
public class PersistenceUtil {
  /**
   * The log variable is an instance of the Logger class provided by the
   * LogManager.getLogger(PersistenceUtil.class) method. The Logger class is used for logging
   * purposes and can be used to log debug, info, warning, and error messages.
   */
  private static final Logger log = LogManager.getLogger(PersistenceUtil.class);

  /** The version property for a persistencd file. */
  public static final String PROP_VERSION = "version"; // $NON-NLS-1$

  /** The campaign version property for a persisted campaign. */
  public static final String PROP_CAMPAIGN_VERSION = "campaignVersion"; // $NON-NLS-1$

  /**
   * The ASSET_DIR variable represents the directory path where assets are stored within a persisted
   * file.
   */
  private static final String ASSET_DIR = "assets/"; // $NON-NLS-1$;

  /**
   * The HERO_LAB variable represents the directory path where HeroLab assets are stored within a
   * persisted file.
   */
  public static final String HERO_LAB = "herolab"; // $NON-NLS-1$

  /**
   * This variable represents the directory where drop-in library files are stored within a
   * persisted file.
   */
  private static final String DROP_IN_LIBRARY_DIR = "libraries/";

  /** The file path for the list of drop-in libraries stored within a persisted campaign file. */
  private static final String DROP_IN_LIBRARY_LIST_FILE = DROP_IN_LIBRARY_DIR + "libraries.json";

  /** The directory path for the drop-in library assets stored within a persisted campaign file. */
  private static final String DROP_IN_LIBRARY_ASSET_DIR = DROP_IN_LIBRARY_DIR + ASSET_DIR;

  /** The directory where game data is stored within a persisted campaign file. */
  private static final String GAME_DATA_DIR = "data/";

  /** Represents the file path of the game data content file within a persisted campaign file. */
  private static final String GAME_DATA_FILE = GAME_DATA_DIR + "game-data.json";

  /**
   * The version number of the campaign.
   *
   * <p>The {@code CAMPAIGN_VERSION} variable represents the version number of the campaign. It is a
   * string value that follows the standard format of major.minor.patch. Each part of the version
   * number represents a different level of changes and updates in the campaign.
   *
   * @since 1.3.70 ownerOnly added to model.Light (not backward compatible)
   * @since 1.3.75 model.Token.visibleOnlyToOwner (actually added to b74 but I didn't catch it
   *     before release)
   * @since 1.3.83 ExposedAreaData added to tokens in b78 but again not caught until b82 :(
   * @since 1.3.85 Added CampaignProperties.hasUsedFogToolbar
   * @since 1.4.0 Added lumens to LightSource class, old versions will not load unless saved as b89
   *     compatible
   * @since 1.11.0 Added add-on libraries, if loaded and saved with an older version then add-on
   *     libraries will be removed.
   * @since 1.15.0 Labels now have background color and font, will default old lables to a similar
   *     background color to what they had before.
   */
  private static final String CAMPAIGN_VERSION = "1.15.0";

  /**
   * Manager of the versioning of campaign models.
   *
   * @see ModelVersionManager
   */

  static {
      PackedFile.init(AppUtil.getAppHome("tmp")); // $NON-NLS-1$
  }


  /**
   * Return an XStream which allows net.rptools.**, java.awt.**, sun.awt.** May be too permissive,
   * but it Works For Me(tm)
   *
   * @return a configured XStream
   */
  public static XStream getConfiguredXStream() {
    XStream xStream = new XStream();
    XStream.setupDefaultSecurity(xStream);
    xStream.allowTypesByWildcard(new String[] {"net.rptools.**", "java.awt.**", "sun.awt.**"});

    return xStream;
  }

  /**
   * Loads assets from a packed file and adds them to the asset manager.
   *
   * @param assetIds The collection of MD5 keys representing the assets to be loaded
   * @param pakFile The packed file containing the assets
   * @throws IOException If an I/O error occurs while loading the assets
   */
  private static void loadAssets(Collection<MD5Key> assetIds, PackedFile pakFile)
      throws IOException {
    // Special handling of assets: XML file to describe the Asset, but binary file for the image
    // data
    pakFile.getXStream().processAnnotations(Asset.class);

    String campaignVersion = (String) pakFile.getProperty(PROP_CAMPAIGN_VERSION);
    String progVersion = (String) pakFile.getProperty(PROP_VERSION);
    List<Asset> addToServer = new ArrayList<Asset>(assetIds.size());

    // FJE: Ugly fix for a bug I introduced in b64. :(
    boolean fixRequired = "1.3.b64".equals(progVersion);

    for (MD5Key key : assetIds) {
      if (key == null) continue;

      if (!AssetManager.hasAsset(key)) {
        String pathname = ASSET_DIR + key;
        Asset asset = null;
        if (fixRequired) {
          try (InputStream is = pakFile.getFileAsInputStream(pathname)) {
            asset =
                Asset.createAssetDetectType(
                    key.toString(), IOUtils.toByteArray(is)); // Ugly bug fix :(
          } catch (FileNotFoundException fnf) {
            // Doesn't need to be reported, since that's handled below.
          } catch (Exception e) {
            log.error("Could not load asset from 1.3.b64 file in compatibility mode", e);
          }
        } else {
          try {
            asset = pakFile.getAsset(pathname);
          } catch (Exception e) {
            // Do nothing. The asset will be 'null' and it'll be handled below.
            log.info("Exception while handling asset '" + pathname + "'", e);
          }
        }
        if (asset == null) { // Referenced asset not included in PackedFile??
          log.error("Referenced asset '" + pathname + "' not found while loading?!");
          continue;
        }
        // If the asset was marked as "broken" then ignore it completely. The end
        // result is that MT will attempt to load it from a repository again, as normal.
        if ("broken".equals(asset.getName())) {
          log.warn("Reference to 'broken' asset '" + pathname + "' not restored.");
          ImageManager.flushImage(asset);
          continue;
        }
        // pre 1.3b52 campaign files stored the image data directly in the asset serialization.
        // New XStreamConverter creates empty byte[] for image.
        if (asset.getData() == null || asset.getData().length < 4) {
          String ext = asset.getExtension();
          pathname = pathname + "." + (ext.isEmpty() ? "dat" : ext);
//          pathname = assetnameVersionManager.transform(pathname, campaignVersion);
          try (InputStream is = pakFile.getFileAsInputStream(pathname)) {
            asset = asset.setData(IOUtils.toByteArray(is), false);
          } catch (FileNotFoundException fnf) {
            log.error("Image data for '" + pathname + "' not found?!", fnf);
            continue;
          } catch (Exception e) {
            log.error("While reading image data for '" + pathname + "'", e);
            continue;
          }
        }
        AssetManager.putAsset(asset);
        addToServer.add(asset);
      }
    }
    if (!addToServer.isEmpty()) {
      }
      addToServer.clear();
    }


  /**
   * Loads the add-on libraries from the campaign file.
   *
   * @param packedFile the file to load from.
   * @throws IOException if there is a problem reading the add-o library information.
   */
  private static void loadAddOnLibraries(PackedFile packedFile) throws IOException {
    var libraryManager = new LibraryManager();
    if (!packedFile.hasFile(DROP_IN_LIBRARY_LIST_FILE)) {
      return; // No Libraries to import
    }
    var builder = AddOnLibraryListDto.newBuilder();
    JsonFormat.parser()
        .merge(
            new InputStreamReader(packedFile.getFileAsInputStream(DROP_IN_LIBRARY_LIST_FILE)),
            builder);
    var listDto = builder.build();

    for (var library : listDto.getLibrariesList()) {
      String libraryData = DROP_IN_LIBRARY_ASSET_DIR + library.getMd5Hash();
      byte[] bytes = packedFile.getFileAsInputStream(libraryData).readAllBytes();
      String libraryNamespace = library.getDetails().getNamespace();
      Asset asset = Type.MTLIB.getFactory().apply(libraryNamespace, bytes);
      if (!AssetManager.hasAsset(asset)) {
        AssetManager.putAsset(asset);
      }
      AddOnLibrary addOnLibrary = new AddOnLibraryImporter().importFromAsset(asset);
      libraryManager.registerAddOnLibrary(addOnLibrary);
    }
  }

  /**
   * Saves the add-on libraries to the provided packed file.
   *
   * @param packedFile The packed file to save the add-on libraries to.
   * @throws IOException If an error occurs while saving the add-on libraries.
   */
  private static void saveAddOnLibraries(PackedFile packedFile) throws IOException {
    // remove all drop-in libraries from the packed file first.
    for (String path : packedFile.getPaths()) {
      if (path.startsWith(DROP_IN_LIBRARY_ASSET_DIR) && !path.equals(DROP_IN_LIBRARY_ASSET_DIR)) {
        packedFile.removeFile(path);
      }
    }

    AddOnLibraryListDto dto = null;
    try {
      dto = new LibraryManager().addOnLibrariesToDto().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new IOException(e);
    }
    packedFile.putFile(
        DROP_IN_LIBRARY_LIST_FILE,
        JsonFormat.printer().print(dto).getBytes(StandardCharsets.UTF_8));

    for (var ldto : dto.getLibrariesList()) {
      Asset asset = AssetManager.getAsset(new MD5Key(ldto.getMd5Hash()));
      packedFile.putFile(DROP_IN_LIBRARY_ASSET_DIR + asset.getMD5Key().toString(), asset.getData());
    }
  }

  /**
   * Saves a collection of assets to a specified packed file.
   *
   * @param assetIds A collection of MD5Key objects representing the asset IDs to be saved.
   * @param pakFile The PackedFile object representing the file to save the assets to.
   * @throws IOException If there is an error writing the assets to the file.
   */
  private static void saveAssets(Collection<MD5Key> assetIds, PackedFile pakFile)
      throws IOException {
    // Special handling of assets: XML file to describe the Asset, but binary file for the image
    // data
    pakFile.getXStream().processAnnotations(Asset.class);

    for (MD5Key assetId : assetIds) {
      if (assetId == null) continue;

      // And store the asset elsewhere
      // As of 1.3.b64, assets are written in binary to allow them to be readable
      // when a campaign file is unpacked.
      Asset asset = AssetManager.getAsset(assetId);
      if (asset == null) {
        log.error("AssetId " + assetId + " not found while saving?!");
        continue;
      }

      String extension = asset.getExtension();
      byte[] assetData = asset.getData();
      pakFile.putFile(ASSET_DIR + assetId + "." + extension, assetData);
      pakFile.putFile(ASSET_DIR + assetId + "", asset); // Does not write the image
    }
  }

  /**
   * Clears all assets in the given packed file that are located in the asset directory.
   *
   * @param pakFile the packed file from which assets will be cleared
   * @throws IOException if an I/O error occurs
   */
  private static void clearAssets(PackedFile pakFile) throws IOException {
    for (String path : pakFile.getPaths()) {
      if (path.startsWith(ASSET_DIR) && !path.equals(ASSET_DIR)) {
        pakFile.removeFile(path);
      }
    }
  }

}
