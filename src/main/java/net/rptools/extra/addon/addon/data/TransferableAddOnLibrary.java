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
package net.rptools.extra.addon.addon.data;

import net.rpTools.aoTool.addon.wrappers.WAddOnLibrary;
import net.rpTools.aoTool.asset.MD5Key;

import java.util.concurrent.ExecutionException;

public class TransferableAddOnLibrary {
  private final String namespace;
  private final String version;
  private final MD5Key assetKey;

  private TransferableAddOnLibrary(String namespace, String version, String assetKey) {
    this.namespace = namespace;
    this.version = version;
    this.assetKey = new MD5Key(assetKey);
  }

  public TransferableAddOnLibrary(WAddOnLibrary WAddOnLibrary)
      throws ExecutionException, InterruptedException {
    namespace = WAddOnLibrary.getNamespace();
    version = WAddOnLibrary.getVersion().get();
    assetKey = WAddOnLibrary.getAssetKey();
  }

//  public static TransferableAddOnLibrary fromDto(TransferableAddOnLibraryDto dto) {
//    return new TransferableAddOnLibrary(dto.getNamespace(), dto.getVersion(), dto.getAssetKey());
//  }

  public String getNamespace() {
    return namespace;
  }

  public String getVersion() {
    return version;
  }

  public MD5Key getAssetKey() {
    return assetKey;
  }

//  public TransferableAddOnLibraryDto toDto() {
//    var dto = TransferableAddOnLibraryDto.newBuilder();
//    return dto.setNamespace(namespace).setVersion(version).setAssetKey(assetKey.toString()).build();
//  }
}
