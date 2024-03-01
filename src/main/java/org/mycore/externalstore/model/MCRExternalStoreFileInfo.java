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

package org.mycore.externalstore.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An {@link MCRExternalStoreFileInfo} describes a file.
 * Also, a file can be a directory.
 */
public class MCRExternalStoreFileInfo {

    private String name;

    private String parentPath;

    private boolean isDirectory;

    private Long size;

    private Date lastModified;

    private String checksum;

    private List<FileFlag> flags = new ArrayList<FileFlag>();

    /**
     * Constructs new instance. Necessary for {@link ObjectMapper}.
     */
    @SuppressWarnings("PMD.UnnecessaryConstructor")
    public MCRExternalStoreFileInfo() {

    }

    /**
     * Returns the file name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the filename.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns whether file is a directory.
     *
     * @return true is file is a directory
     */
    public boolean isDirectory() {
        return isDirectory;
    }

    /**
     * Returns whether file is a directory.
     *
     * @param isDirectory true if file is a directory
     */
    public void setDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

    /**
     * Returns the file size.
     *
     * @return the size
     */
    public Long getSize() {
        return size;
    }

    /**
     * Sets the file size.
     *
     * @param size the size
     */
    public void setSize(Long size) {
        this.size = size;
    }

    /**
     * Returns the time of the last modification.
     *
     * @return time of last modification
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Sets the time of last modification.
     *
     * @param lastModfied the time of last modification
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Returns the checksum.
     *
     * @return the checksum
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * Sets the checksum.
     *
     * @param checksum the checksum
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    /**
     * Returns the parent path.
     *
     * @return the parent path which can be null
     */
    public String getParentPath() {
        return parentPath;
    }

    /**
     * Sets the parent path.
     *
     * @param parentPath the parent path
     */
    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    /**
     * Returns the list of {@link FileFlag}.
     *
     * @return the flags
     */
    public List<FileFlag> getFlags() {
        return flags;
    }

    /**
     * Sets the list of {@link FileFlag}.
     *
     * @param flags the flags
     */
    public void setFlags(List<FileFlag> flags) {
        this.flags = flags;
    }

    /**
     * Returns absolute path.
     *
     * @return the absolute path
     */
    public String getAbsolutePath() {
        if (parentPath != null && !parentPath.isEmpty()) {
            return parentPath + "/" + name;
        }
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(checksum, isDirectory, lastModified, name, parentPath, flags, size);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MCRExternalStoreFileInfo other = (MCRExternalStoreFileInfo) obj;
        return Objects.equals(checksum, other.checksum) && isDirectory == other.isDirectory
            && Objects.equals(lastModified, other.lastModified) && Objects.equals(name, other.name)
            && Objects.equals(parentPath, other.parentPath) && Objects.equals(size, other.size)
            && Objects.equals(flags, other.flags);
    }

    /**
     * Builder for {@link MCRExternalStoreFileInfo}.
     */
    public static class MCRExternalStoreFileInfoBuilder {

        private String parentPath;

        private String name;

        private boolean isDirectory = false;

        private Long size;

        private Date lastModified;

        private String checksum;

        private List<FileFlag> flags = new ArrayList<FileFlag>();

        /**
         * Creates a {@link MCRExternalStoreFileInfoBuilder} with file name and parent path.
         *
         * @param name the file name
         * @param parentPath the parent path
         */
        public MCRExternalStoreFileInfoBuilder(String name, String parentPath) {
            this.name = name;
            this.parentPath = parentPath;
        }

        /**
         * Sets if file is a directory.
         *
         * @param isDirectory true if file is a directory
         * @return builder instance
         */
        public MCRExternalStoreFileInfoBuilder directory(boolean isDirectory) {
            this.isDirectory = isDirectory;
            return this;
        }

        /**
         * Sets file size.
         *
         * @param size the file size
         * @return builder instance
         */
        public MCRExternalStoreFileInfoBuilder size(Long size) {
            this.size = size;
            return this;
        }

        /**
         * Sets last modified instant.
         *
         * @param lastModified last modified
         * @return builder instance
         */
        public MCRExternalStoreFileInfoBuilder lastModified(Date lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        /**
         * Sets flags.
         *
         * @param flags list of flags
         * @return builder instance
         */
        public MCRExternalStoreFileInfoBuilder flags(List<FileFlag> flags) {
            this.flags = flags;
            return this;
        }

        /**
         * Sets the checksum.
         *
         * @param checksum the checksum
         * @return builder instance
         */
        public MCRExternalStoreFileInfoBuilder checksum(String checksum) {
            this.checksum = checksum;
            return this;
        }

        /**
         * Builds {@link MCRExternalStoreFileInfo} instance.
         *
         * @return the file info
         */
        public MCRExternalStoreFileInfo build() {
            final MCRExternalStoreFileInfo file = new MCRExternalStoreFileInfo();
            file.setName(name);
            file.setParentPath(parentPath);
            file.setDirectory(isDirectory);
            file.setSize(size);
            file.setLastModified(lastModified);
            file.setChecksum(checksum);
            file.setFlags(flags);
            return file;
        }

    }

    /**
     * Defines optional flag for {@link MCRExternalStoreFileInfo}.
     */
    public enum FileFlag {
        ARCHIVE, ARCHIVE_ENTRY
    }

}
