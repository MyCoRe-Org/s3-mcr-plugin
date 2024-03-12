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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.mycore.backend.jpa.MCRObjectIDConverter;
import org.mycore.datamodel.metadata.MCRObjectID;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * A {@link MCRExternalStoreInfoData} contains general info about a remote store
 * like type and linked object id. Also, it includes the a list of {@link MCRExternalStoreFileInfoData}.
 */
@Entity
@Table(name = "MCRExternalStoreInfo")
@NamedQueries({
    @NamedQuery(name = "MCRExternalStoreInfo.findAll",
        query = "SELECT i FROM MCRExternalStoreInfoData i"),
    @NamedQuery(name = "MCRExternalStoreInfo.findByDerivateId",
        query = "SELECT i FROM MCRExternalStoreInfoData i WHERE i.derivateId = :derivateId"),
    @NamedQuery(name = "MCRExternalStoreInfo.findFileInfosByDerivateIdAndParenthPath",
        query = "SELECT i FROM MCRExternalStoreFileInfoData i, MCRExternalStoreInfoData s "
            + "where i.storeInfo = s and s.derivateId = :derivateId and i.parentPath = :parentPath"),
    @NamedQuery(name = "MCRExternalStoreInfo.findFileInfoByDerivateIdAndPath",
        query = "SELECT i FROM MCRExternalStoreFileInfoData i, MCRExternalStoreInfoData s "
            + "where i.storeInfo = s and s.derivateId = :derivateId and i.name = :name and i.parentPath = :parentPath"),
})
public class MCRExternalStoreInfoData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "store_info_id")
    private Long id;

    @Column(name = "derivate_id", length = MCRObjectID.MAX_LENGTH, nullable = false)
    @Convert(converter = MCRObjectIDConverter.class)
    private MCRObjectID derivateId;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "storeInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MCRExternalStoreFileInfoData> fileInfos = new ArrayList<>();

    /**
     * Constructs new instance. Necessary for {@link ObjectMapper}.
     */
    @SuppressWarnings("PMD.UnnecessaryConstructor")
    public MCRExternalStoreInfoData() {

    }

    /**
     * Returns the internal id.
     *
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the internal id.
     *
     * @param id id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the derivate id.
     *
     * @return derivate id
     */
    public MCRObjectID getDerivateId() {
        return derivateId;
    }

    /**
     * Sets the derivate id.
     *
     * @param derivateId derivate id
     */
    public void setDerivateId(MCRObjectID derivateId) {
        this.derivateId = derivateId;
    }

    /**
     * Returns list of {@link MCRExternalStoreFileInfoData} elements.
     *
     * @return list of file info data elements
     */
    public List<MCRExternalStoreFileInfoData> getFileInfos() {
        return fileInfos;
    }

    /**
     * Sets list of {@link MCRExternalStoreFileInfoData} elements.
     *
     * @param fileInfos list of files
     */
    public void setFileInfos(List<MCRExternalStoreFileInfoData> fileInfos) {
        this.fileInfos = fileInfos;
    }

    /**
     * Adds {@link MCRExternalStoreFileInfoData} to file info list.
     *
     * @param fileInfo fileInfo
     */
    public void addFileInfo(MCRExternalStoreFileInfoData fileInfo) {
        fileInfos.add(fileInfo);
        fileInfo.setStoreInfo(this);
    }

    /**
     * Removes {@link MCRExternalStoreFileInfoData} element from file info list.
     *
     * @param fileInfo fileInfo
     */
    public void removeFileInfo(MCRExternalStoreFileInfoData fileInfo) {
        fileInfos.remove(fileInfo);
        fileInfo.setParentPath(null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(derivateId, fileInfos, id);
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
        MCRExternalStoreInfoData other = (MCRExternalStoreInfoData) obj;
        return Objects.equals(derivateId, other.derivateId) && Objects.equals(fileInfos, other.fileInfos)
            && Objects.equals(id, other.id);
    }

}
