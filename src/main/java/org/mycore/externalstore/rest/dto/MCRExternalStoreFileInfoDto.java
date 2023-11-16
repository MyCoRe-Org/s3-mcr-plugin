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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mycore.externalstore.model.MCRExternalStoreFileInfo.FileFlag;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MCRExternalStoreFileInfoDto {

    @JsonProperty("name")
    private String name;

    @JsonProperty("parentPath")
    private String parentPath;

    @JsonProperty("isDirectory")
    private boolean isDirectory;

    @JsonProperty("size")
    private Long size;

    @JsonProperty("checksum")
    private String checksum;

    @JsonProperty("lastModified")
    private Date lastModified;

    @JsonProperty("flags")
    private List<FileFlag> flags = new ArrayList<FileFlag>();

    @JsonProperty("capabilities")
    private List<MCRFileCapability> capabilities = new ArrayList<MCRFileCapability>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public List<FileFlag> getFlags() {
        return flags;
    }

    public void setFlags(List<FileFlag> flags) {
        this.flags = flags;
    }

    public List<MCRFileCapability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<MCRFileCapability> capabilities) {
        this.capabilities = capabilities;
    }

}
