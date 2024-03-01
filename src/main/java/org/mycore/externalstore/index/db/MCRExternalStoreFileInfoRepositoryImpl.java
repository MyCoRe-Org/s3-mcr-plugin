package org.mycore.externalstore.index.db;

import java.util.List;
import java.util.Optional;

import org.mycore.externalstore.index.db.model.MCRExternalStoreFileInfoData;

import jakarta.persistence.EntityManager;

/**
 * {@link MCRExternalStoreInfoRepository} implementation.
 */
public class MCRExternalStoreFileInfoRepositoryImpl extends MCRExternalStoreFileInfoRepository {

    @Override
    public Optional<MCRExternalStoreFileInfoData> find(long id) {
        return Optional.ofNullable(getEntityManager().find(MCRExternalStoreFileInfoData.class, id));
    }

    @Override
    public List<MCRExternalStoreFileInfoData> listAll() {
        return getEntityManager()
            .createNamedQuery("MCRExternalFileStoreInfo.findAll", MCRExternalStoreFileInfoData.class).getResultList();
    }

    @Override
    public void insert(MCRExternalStoreFileInfoData object) {
        getEntityManager().persist(object);
    }

    @Override
    public void save(MCRExternalStoreFileInfoData object) {
        getEntityManager().merge(object);
    }

    @Override
    public void delete(MCRExternalStoreFileInfoData object) {
        final EntityManager entityManager = getEntityManager();
        entityManager.remove(entityManager.contains(object) ? object : entityManager.merge(object));
    }

}
