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

package org.mycore.externalstore.index;

import java.util.List;
import java.util.Optional;

import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;

/**
 * Manages an index that refers to information about a store or file info.
 */
public interface MCRExternalStoreInfoIndex {

    /**
     * Adds list of {@link MCRExternalStoreFileInfo} elements to index by derivate id.
     *
     * @param derivateId the derivate id
     * @param fileInfos list of file info elements
     */
    void addFileInfos(MCRObjectID derivateId, List<MCRExternalStoreFileInfo> fileInfos);

    /**
     * Adds {@link MCRExternalStoreFileInfo} to index by derivate id.
     *
     * @param derivateId derivate id
     * @param fileInfo file info
     */
    void addFileInfo(MCRObjectID derivateId, MCRExternalStoreFileInfo fileInfo);

    /**
     * Updates {@link MCRExternalStoreFileInfo} in index by derivate id.
     *
     * @param derivateId derivate id
     * @param fileInfo file info
     */
    void updateFileInfo(MCRObjectID derivateId, MCRExternalStoreFileInfo fileInfo);

    /**
     * Returns an {@code Optional} with {@link MCRExternalStoreFileInfo} by path for derivate id.
     *
     * @param derivateId derivate id
     * @param path path
     * @return optional with file info
     */
    Optional<MCRExternalStoreFileInfo> findFileInfo(MCRObjectID derivateId, String path);

    /**
     * Returns a list of {@link MCRExternalStoreFileInfo} elements specified by the path and derivate id.
     *
     * @param derivateId the derivate id
     * @param path the path of the directory
     * @return all files
     */
    List<MCRExternalStoreFileInfo> listFileInfos(MCRObjectID derivateId, String path);

    /**
     * Clears all info specified by derivate id.
     *
     * @param derivateId derivate id
     */
    void deleteFileInfos(MCRObjectID derivateId);

}
