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
import org.mycore.externalstore.exception.MCRExternalStoreFileNameCollisionException;
import org.mycore.externalstore.exception.MCRExternalStoreNoSuchFileException;
import org.mycore.externalstore.index.MCRExternalStoreInfoIndex;
import org.mycore.externalstore.index.db.model.MCRExternalStoreFileInfoData;
import org.mycore.externalstore.index.db.model.MCRExternalStoreInfoData;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;
import org.mycore.externalstore.util.MCRExternalStoreUtils;

/**
 * Implements {@link MCRExternalStoreInfoIndex} using db as backend.
 */
public class MCRExternalStoreDbStoreInfoIndex implements MCRExternalStoreInfoIndex {

    private final MCRExternalStoreInfoRepository storeInfoRepository = new MCRExternalStoreInfoRepositoryImpl();

    @Override
    public synchronized void addFileInfos(MCRObjectID derivateId, List<MCRExternalStoreFileInfo> fileInfos) {
        final MCRExternalStoreInfoData storeInfoData = getOrCreateStoreInfoData(derivateId);
        fileInfos.stream().map(MCRExternalStoreDbMapper::toData).forEach(storeInfoData::addFileInfo);
        storeInfoRepository.insert(storeInfoData);
    }

    @Override
    public synchronized List<MCRExternalStoreFileInfo> listFileInfos(MCRObjectID derivateId, String path) {
        return storeInfoRepository.findFileInfos(derivateId, path).stream().map(MCRExternalStoreDbMapper::toDomain)
            .toList();
    }

    @Override
    public synchronized void deleteFileInfos(MCRObjectID derivateId) {
        storeInfoRepository.cleanByDerivateId(derivateId);
    }

    @Override
    public synchronized Optional<MCRExternalStoreFileInfo> findFileInfo(MCRObjectID derivateId, String path) {
        final String name = MCRExternalStoreUtils.getFileName(path);
        final String parentPath = MCRExternalStoreUtils.getParentPath(path);
        return storeInfoRepository.findFileInfo(derivateId, name, parentPath).map(MCRExternalStoreDbMapper::toDomain);
    }

    @Override
    public synchronized void addFileInfo(MCRObjectID derivateId, MCRExternalStoreFileInfo fileInfo) {
        final MCRExternalStoreInfoData storeInfoData = getOrCreateStoreInfoData(derivateId);
        if (findFileInfo(derivateId, fileInfo.getAbsolutePath()).isPresent()) {
            throw new MCRExternalStoreFileNameCollisionException();
        }
        final MCRExternalStoreFileInfoData fileInfoData = MCRExternalStoreDbMapper.toData(fileInfo);
        storeInfoData.addFileInfo(fileInfoData);
    }

    @Override
    public synchronized void updateFileInfo(MCRObjectID derivateId, MCRExternalStoreFileInfo fileInfo) {
        final MCRExternalStoreFileInfoData fileInfoData = MCRExternalStoreDbMapper.toData(fileInfo);
        final Optional<MCRExternalStoreFileInfoData> currentFileInfoData
            = storeInfoRepository.findFileInfo(derivateId, fileInfo.getName(), fileInfo.getParentPath());
        if (currentFileInfoData.isEmpty()) {
            throw new MCRExternalStoreNoSuchFileException("There is no matching file info to update");
        }
        currentFileInfoData.get().setFlags(fileInfoData.getFlags());
    }

    private MCRExternalStoreInfoData getOrCreateStoreInfoData(MCRObjectID derivateId) {
        return storeInfoRepository.findByDerivateId(derivateId).orElseGet(() -> {
            final MCRExternalStoreInfoData storeInfoData = new MCRExternalStoreInfoData();
            storeInfoData.setDerivateId(derivateId);
            storeInfoRepository.insert(storeInfoData);
            return storeInfoData;
        });

    }
}
