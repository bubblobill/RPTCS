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
package net.rptools.maptool.server;

import java.awt.geom.Area;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.gamedata.proto.DataStoreDto;
import net.rptools.maptool.model.gamedata.proto.GameDataDto;
import net.rptools.maptool.model.gamedata.proto.GameDataValueDto;
import net.rptools.maptool.model.library.addon.TransferableAddOnLibrary;
import net.rptools.maptool.model.player.Player;

public interface ServerCommand {
  void putAsset(Asset asset);
  void getAsset(MD5Key assetID);
  void removeAsset(MD5Key assetID);
  void addAddOnLibrary(List<TransferableAddOnLibrary> addOnLibraries);
  void removeAddOnLibrary(List<String> namespaces);
  void removeAllAddOnLibraries();
  void updateDataStore(DataStoreDto dataStore);
  void updateDataNamespace(GameDataDto gameData);
  void updateData(String type, String namespace, GameDataValueDto gameData);
  void removeDataStore();
  void removeDataNamespace(String type, String namespace);
  void removeData(String type, String namespace, String name);
}
