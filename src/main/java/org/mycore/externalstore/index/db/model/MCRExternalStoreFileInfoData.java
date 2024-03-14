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

package org.mycore.externalstore.index.db.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.mycore.externalstore.model.MCRExternalStoreFileInfo.FileFlag;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * A {@link MCRExternalStoreFileInfoData} describes a info of a file.
 * Also, a file can be a directory.
 */
@Entity
@Table(name = "MCRExternalStoreFileInfo")
@NamedQueries({
    @NamedQuery(name = "MCRExternalStoreFileInfo.findAll",
        query = "SELECT i FROM MCRExternalStoreFileInfoData i"),
})
public class MCRExternalStoreFileInfoData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "file_info_id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "parent_path")
    private String parentPath;

    @Column(name = "is_directory", columnDefinition = "BOOLEAN default false")
    private boolean isDirectory;

    @Column(name = "size", columnDefinition = "BIGINT default 0")
    private Long size;

    @Column(name = "last_modified")
    private LocalDateTime lastModified;

    @Column(name = "checksum")
    private String checksum;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "MCRExternalStoreFile_Flag")
    @Column(name = "flag")
    private Set<String> flags = new HashSet<String>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "store_info_id")
    private MCRExternalStoreInfoData storeInfo;

    /**
     * Returns the internal id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the internal id.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the filename.
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
    public LocalDateTime getLastModified() {
        return lastModified;
    }

    /**
     * Sets the time of last modification.
     *
     * @param lastModified the time of last modification
     */
    public void setLastModified(LocalDateTime lastModified) {
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
     * Returns the associated {@link MCRExternalStoreInfoData}.
     *
     * @return the store info
     */
    public MCRExternalStoreInfoData getStoreInfo() {
        return storeInfo;
    }

    /**
     * Sets the associated {@link MCRExternalStoreInfoData}.
     *
     * @param storeInfo the store info
     */
    public void setStoreInfo(MCRExternalStoreInfoData storeInfo) {
        this.storeInfo = storeInfo;
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
    public Set<String> getFlags() {
        return flags;
    }

    /**
     * Sets the list of {@link FileFlag}.
     *
     * @param flags the flags
     */
    public void setFlags(Set<String> flags) {
        this.flags = flags;
    }

    @Transient
    public String getAbsolutePath() {
        if (!parentPath.isEmpty()) {
            return parentPath.concat("/").concat(name);
        }
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(checksum, flags, id, isDirectory, lastModified, name, parentPath, size, storeInfo);
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
        MCRExternalStoreFileInfoData other = (MCRExternalStoreFileInfoData) obj;
        return Objects.equals(checksum, other.checksum) && Objects.equals(flags, other.flags)
            && Objects.equals(id, other.id) && isDirectory == other.isDirectory
            && Objects.equals(lastModified, other.lastModified) && Objects.equals(name, other.name)
            && Objects.equals(parentPath, other.parentPath) && Objects.equals(size, other.size)
            && Objects.equals(storeInfo, other.storeInfo);
    }

}
