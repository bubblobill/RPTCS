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
package net.rptools.extra.addon.addon;

import net.rpTools.aoTool.addon.addon.proto.AddOnLibraryDto;

import java.util.Arrays;

/** Record that contains the information about a library. */
public record LibraryInfo(
    String name,
    String namespace,
    String version,
    String website,
    String gitUrl,
    String[] authors,
    String license,
    String description,
    String shortDescription,
    boolean allowsUrlAccess,
    String readMeFile,
    String licenseFile) {
    public LibraryInfo fromDto(AddOnLibraryDto dto){
        return  new LibraryInfo(
                dto.getName(),
                dto.getNamespace(),
                dto.getVersion(),
                dto.getWebsite(),
                dto.getGitUrl(),
                dto.getAuthorsList().toArray(String[]::new),
                dto.getLicense(),
                dto.getDescription(),
                dto.getShortDescription(),
                dto.getAllowsUriAccess(),
                dto.getReadMeFile(),
                dto.getLicenseFile()
        );
    }
    public AddOnLibraryDto toDto(){
        return AddOnLibraryDto.newBuilder()
                .setName(name())
                .setNamespace(namespace())
                .setVersion(version())
                .setWebsite(website())
                .setGitUrl(gitUrl())
                .addAllAuthors(Arrays.asList(authors()))
                .setLicense(license())
                .setDescription(description())
                .setShortDescription(shortDescription())
                .setAllowsUriAccess(allowsUrlAccess())
                .setReadMeFile(readMeFile())
                .setLicenseFile(licenseFile())
                .build();
    }
}
