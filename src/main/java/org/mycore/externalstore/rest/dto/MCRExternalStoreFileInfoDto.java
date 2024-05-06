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
import java.util.Set;

import org.mycore.externalstore.model.MCRExternalStoreFileInfo;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo.FileFlag;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Dto for {@link MCRExternalStoreFileInfo}.
 *
 * @param name name
 * @param parentPath parent path
 * @param isDirectory is directory
 * @param size size
 * @param checksum checksum
 * @param lastModified last modified
 * @param flags list over file flag elements
 * @param capabilities list over file capability elements
 */
public record MCRExternalStoreFileInfoDto(@JsonProperty("name") String name,
    @JsonProperty("parentPath") String parentPath, @JsonProperty("isDirectory") boolean isDirectory,
    @JsonProperty("size") Long size, @JsonProperty("checksum") String checksum,
    @JsonProperty("lastModified") Date lastModified, @JsonProperty("flags") Set<FileFlag> flags,
    @JsonProperty("capabilities") Set<MCRExternalStoreFileInfoDto.MCRFileCapability> capabilities) {

    /**
     * Describing the technical limits of a File or Folder
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public enum MCRFileCapability {

        /**
         * Download capabilty.
         */
        DOWNLOAD("Download");

        private String name;

        /**
         * Constructs file capabilty with name.
         *
         * @param name name
         */
        MCRFileCapability(String name) {
            this.name = name;
        }

        /**
         * Returns name
         *
         * @return name
         */
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
