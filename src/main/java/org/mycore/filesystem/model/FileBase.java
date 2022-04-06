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

package org.mycore.filesystem.model;

import org.mycore.filesystem.capability.FileCapability;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

public class FileBase {

    public FileBase(FileType type) {
        this.type = type;
    }

    protected String path;

    protected String iconURI;

    protected String name;

    protected String etag;

    protected String storedEtag;

    protected List<FileCapability> capabilities = new ArrayList<>();

    protected Date lastModified;

    protected FileType type;

    int size;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconURI() {
        return iconURI;
    }

    public void setIconURI(String iconURI) {
        this.iconURI = iconURI;
    }

    public List<FileCapability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<FileCapability> capabilities) {
        this.capabilities = capabilities;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public FileType getType() {
        return type;
    }

    public String getStoredEtag() {
        return storedEtag;
    }

    public void setStoredEtag(String storedEtag) {
        this.storedEtag = storedEtag;
    }
}
