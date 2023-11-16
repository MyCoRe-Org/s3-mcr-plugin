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

package org.mycore.externalstore.index.db;

import java.util.List;
import java.util.Optional;

import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.externalstore.index.db.model.MCRExternalStoreFileInfoData;
import org.mycore.externalstore.index.db.model.MCRExternalStoreInfoData;

/**
 * Class for {@link MCRExternalStoreInfoRepository}.
 */
public abstract class MCRExternalStoreInfoRepository extends MCRBaseRepository<MCRExternalStoreInfoData> {

    /**
     * Returns {@link Optional} of {@link MCRExternalStoreInfoData} for derivate id.
     *
     * @param derivateId the id
     * @return the optional
     */
    public abstract Optional<MCRExternalStoreInfoData> findByDerivateId(MCRObjectID derivateId);

    /**
     * Cleans all data for derivate id.
     *
     * @param derivateId the derivate id
     */
    public abstract void cleanByDerivateId(MCRObjectID derivateId);

    /**
     * Returns {@link MCRExternalStoreFileInfoData} for derivate id and path.
     *
     * @param derivateId the derivate id
     * @param name the name
     * @param parentPath the parent path
     * @return file info
     */
    public abstract Optional<MCRExternalStoreFileInfoData> findFileInfo(MCRObjectID derivateId, String name,
        String parentPath);

    /**
     * Returns {@link MCRExternalStoreFileInfoData} for derivate id and path.
     *
     * @param derivateId the derivate id
     * @param name the name
     * @param parentPath the parent path
     * @return file infos
     */
    public abstract List<MCRExternalStoreFileInfoData> findFileInfos(MCRObjectID derivateId, String parentPath);

}
