/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mycore.externalstore.rest.dto;

import java.util.Date;
import java.util.List;

import org.mycore.externalstore.model.MCRExternalStoreFileInfo;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo.FileFlag;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Dto for {@link MCRExternalStoreFileInfo}.
 */
public record MCRExternalStoreFileInfoDto(@JsonProperty("name") String name,
    @JsonProperty("parentPath") String parentPath, @JsonProperty("isDirectory") boolean isDirectory,
    @JsonProperty("size") Long size, @JsonProperty("checksum") String checksum,
    @JsonProperty("lastModified") Date lastModified, @JsonProperty("flags") List<FileFlag> flags,
    @JsonProperty("capabilities") List<MCRFileCapability> capabilities) {

}
