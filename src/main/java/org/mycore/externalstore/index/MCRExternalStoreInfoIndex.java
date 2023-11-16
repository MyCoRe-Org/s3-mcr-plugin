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
 * A {@link MCRExternalStoreInfoIndex} manages {@link MCRExternalStoreInfo}.
 */
public interface MCRExternalStoreInfoIndex {

    /**
     * Adds given {@link MCRExternalStoreFileInfo}.
     *
     * @param derivateId the derivate id
     * @param storeInfo the store info
     */
    void addFileInfos(MCRObjectID derivateId, List<MCRExternalStoreFileInfo> fileInfos);

    /**
     * Adds {@link MCRExternalStoreFileInfo}.
     *
     * @param derivateId the derivate id
     * @param fileInfo the file info
     */
    void addFileInfo(MCRObjectID derivateId, MCRExternalStoreFileInfo fileInfo);

    /**
     * Updates {@link MCRExternalStoreFileInfo}.
     *
     * @param derivateId the derivate id
     * @param fileInfo the file info
     */
    void updateFileInfo(MCRObjectID derivateId, MCRExternalStoreFileInfo fileInfo);

    /**
     * Finds {@link MCRExternalStoreFileInfo} by path for derivate.
     *
     * @param derivateId the derivate id
     * @param path the path
     * @return optional of file info
     */
    Optional<MCRExternalStoreFileInfo> findFileInfo(MCRObjectID derivateId, String path);

    /**
     * Lists all files of a derivate specified by the path.
     *
     * @param derivateId the derivate id
     * @param path the path of the directory
     * @return all files
     */
    List<MCRExternalStoreFileInfo> listFileInfos(MCRObjectID derivateId, String path);

    /**
     * Deletes {@link MCRExternalStoreInfo} by given derivate id.
     *
     * @param derivateId the derivate id
     */
    void deleteFileInfos(MCRObjectID derivateId);

}
