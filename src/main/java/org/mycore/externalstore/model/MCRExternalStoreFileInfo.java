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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * An {@link MCRExternalStoreFileInfo} describes a file.
 * Also, a file can be a directory.
 */
public record MCRExternalStoreFileInfo(String name, String parentPath, boolean isDirectory, Long size,
    Date lastModified, String checksum, Set<MCRExternalStoreFileInfo.FileFlag> flags) {

    private MCRExternalStoreFileInfo(Builder builder) {
        this(builder.name, builder.parentPath, builder.isDirectory, builder.size, builder.lastModified,
            builder.checksum, builder.flags);
    }

    public String getAbsolutePath() {
        if (parentPath != null && !parentPath.isEmpty()) {
            return parentPath + "/" + name;
        }
        return name;
    }

    /**
     * Builder for {@link MCRExternalStoreFileInfo}.
     */
    public static final class Builder {

        @SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
        private final String parentPath;

        @SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
        private final String name;

        @SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
        private boolean isDirectory = false;

        @SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
        private Long size;

        @SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
        private Date lastModified;

        @SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
        private String checksum;

        @SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
        private Set<FileFlag> flags = new HashSet<FileFlag>();

        public Builder(String name) {
            this(name, "");
        }

        /**
         * Creates a {@link Builder} with file name and parent path.
         *
         * @param name the file name
         * @param parentPath the parent path
         */
        public Builder(String name, String parentPath) {
            this.name = name;
            this.parentPath = parentPath;
        }

        /**
         * Sets if file is a directory.
         *
         * @param isDirectory true if file is a directory
         * @return builder instance
         */
        public Builder directory(boolean isDirectory) {
            this.isDirectory = isDirectory;
            return this;
        }

        /**
         * Sets file size.
         *
         * @param size the file size
         * @return builder instance
         */
        public Builder size(Long size) {
            this.size = size;
            return this;
        }

        /**
         * Sets last modified instant.
         *
         * @param lastModified last modified
         * @return builder instance
         */
        public Builder lastModified(Date lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        /**
         * Sets flags.
         *
         * @param flags list of flags
         * @return builder instance
         */
        public Builder flags(Set<FileFlag> flags) {
            this.flags = flags;
            return this;
        }

        /**
         * Sets the checksum.
         *
         * @param checksum the checksum
         * @return builder instance
         */
        public Builder checksum(String checksum) {
            this.checksum = checksum;
            return this;
        }

        /**
         * Builds {@link MCRExternalStoreFileInfo} instance.
         *
         * @return the file info
         */
        public MCRExternalStoreFileInfo build() {
            return new MCRExternalStoreFileInfo(this);
        }

    }

    /**
     * Defines optional flag for {@link MCRExternalStoreFileInfo}.
     */
    public enum FileFlag {
        ARCHIVE, ARCHIVE_ENTRY
    }

}
