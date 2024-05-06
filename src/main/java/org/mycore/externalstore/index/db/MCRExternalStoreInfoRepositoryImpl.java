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

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

/**
 * {@link MCRExternalStoreInfoRepository} implementation.
 */
public class MCRExternalStoreInfoRepositoryImpl extends MCRExternalStoreInfoRepository {

    @Override
    public List<MCRExternalStoreInfoData> listAll() {
        return getEntityManager().createNamedQuery("MCRExternalStoreInfo.findAll", MCRExternalStoreInfoData.class)
            .getResultList();
    }

    @Override
    public Optional<MCRExternalStoreInfoData> findByDerivateId(MCRObjectID derivateId) {
        try {
            final TypedQuery<MCRExternalStoreInfoData> query = getEntityManager()
                .createNamedQuery("MCRExternalStoreInfo.findByDerivateId", MCRExternalStoreInfoData.class);
            query.setParameter("derivateId", derivateId);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<MCRExternalStoreInfoData> find(long id) {
        return Optional.ofNullable(getEntityManager().find(MCRExternalStoreInfoData.class, id));
    }

    @Override
    public void insert(MCRExternalStoreInfoData object) {
        getEntityManager().persist(object);
    }

    @Override
    public void save(MCRExternalStoreInfoData object) {
        getEntityManager().merge(object);
    }

    @Override
    public void delete(MCRExternalStoreInfoData object) {
        final EntityManager entityManager = getEntityManager();
        entityManager.remove(entityManager.contains(object) ? object : entityManager.merge(object));
    }

    @Override
    public void cleanByDerivateId(MCRObjectID derivateId) {
        findByDerivateId(derivateId).ifPresent(s -> delete(s));
    }

    @Override
    public Optional<MCRExternalStoreFileInfoData> findFileInfo(MCRObjectID derivateId, String name, String parentPath) {
        try {
            final TypedQuery<MCRExternalStoreFileInfoData> query = getEntityManager().createNamedQuery(
                "MCRExternalStoreInfo.findFileInfoByDerivateIdAndPath", MCRExternalStoreFileInfoData.class);
            query.setParameter("derivateId", derivateId);
            query.setParameter("name", name);
            query.setParameter("parentPath", parentPath);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<MCRExternalStoreFileInfoData> findFileInfos(MCRObjectID derivateId, String parentPath) {
        final TypedQuery<MCRExternalStoreFileInfoData> query = getEntityManager().createNamedQuery(
            "MCRExternalStoreInfo.findFileInfosByDerivateIdAndParentPath", MCRExternalStoreFileInfoData.class);
        query.setParameter("derivateId", derivateId);
        query.setParameter("parentPath", parentPath);
        return query.getResultList();
    }

}
