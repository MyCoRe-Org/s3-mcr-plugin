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

package org.mycore.externalstore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.externalstore.exception.MCRExternalStoreException;
import org.mycore.externalstore.index.MCRExternalStoreInfoIndex;
import org.mycore.externalstore.index.MCRExternalStoreInfoIndexManager;
import org.mycore.externalstore.model.MCRExternalStoreArchiveInfo;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;

/**
 * Event handlers which primary indexes remote store specific files.
 */
public class MCRExternalStoreEventHandler extends MCREventHandlerBase {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final MCRExternalStoreInfoIndex STORE_INFO_INDEX
        = MCRExternalStoreInfoIndexManager.getInfoIndex();

    @Override
    protected void handlePathCreated(MCREvent evt, Path path, BasicFileAttributes attrs) {
        handlePathCreatedUpdated(path);
    }

    @Override
    protected void handlePathUpdated(MCREvent evt, Path path, BasicFileAttributes attrs) {
        handlePathCreatedUpdated(path);
    }

    @Override
    protected void handlePathDeleted(MCREvent evt, Path path, BasicFileAttributes attrs) {
        if (!MCRExternalStoreServiceUtils.checkPathIsFileInfosFile(path)) {
            return;
        }
        final String derivateIdString = MCRPath.toMCRPath(path).getOwner();
        final MCRObjectID derivateId = MCRObjectID.getInstance(derivateIdString);
        final MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(derivateId);
        deleteFileInfosFromIndex(derivate);
    }

    @Override
    protected void handleDerivateDeleted(MCREvent evt, MCRDerivate der) {
        deleteFileInfosFromIndex(der);
    }

    private void addArchiveInfosToIndex(MCRObjectID derivateId, Path path) {
        LOGGER.debug("Adding archive infos to {}", derivateId);
        List<MCRExternalStoreArchiveInfo> archiveInfos;
        try {
            archiveInfos = MCRExternalStoreServiceHelper.getArchiveInfos(Files.newInputStream(path));
        } catch (IOException | IllegalArgumentException e) {
            throw new MCRExternalStoreException("Error while reading info", e);
        }
        archiveInfos.forEach(a -> {
            a.getFileInfos().forEach(f -> STORE_INFO_INDEX.addFileInfo(derivateId, f));
            final Optional<MCRExternalStoreFileInfo> entry = STORE_INFO_INDEX.findFileInfo(derivateId, a.getPath());
            if (entry.isEmpty()) {
                throw new MCRExternalStoreException("Found missmatch");
            }
            entry.get().getFlags().add(MCRExternalStoreFileInfo.FileFlag.ARCHIVE);
            STORE_INFO_INDEX.updateFileInfo(derivateId, entry.get());
        });
    }

    private void addFileInfosToIndex(MCRObjectID derivateId, Path path) {
        LOGGER.debug("File infos to {}", derivateId);
        List<MCRExternalStoreFileInfo> fileInfos;
        try {
            fileInfos = MCRExternalStoreServiceHelper.getFileInfos(Files.newInputStream(path));
        } catch (IOException | IllegalArgumentException e) {
            throw new MCRExternalStoreException("Error while reading info", e);
        }
        STORE_INFO_INDEX.deleteFileInfos(derivateId);
        STORE_INFO_INDEX.addFileInfos(derivateId, fileInfos);
    }

    private void handlePathCreatedUpdated(Path path) {
        final String derivateIdString = MCRPath.toMCRPath(path).getOwner();
        final MCRObjectID derivateId = MCRObjectID.getInstance(derivateIdString);
        final MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(derivateId);
        if (!checkDerivateClassification(derivate)) {
            LOGGER.debug("Path: {} is no located in external store classified derivate", path);
            return;
        }
        String type;
        try {
            type = MCRExternalStoreServiceUtils.getStoreType(derivate);
            LOGGER.debug("detected type: {}", type);
        } catch (MCRExternalStoreException e) {
            LOGGER.error(e);
            return;
        }
        LOGGER.debug("Found store specific file");
        if (MCRExternalStoreServiceUtils.checkPathIsFileInfosFile(path)) {
            addFileInfosToIndex(derivateId, path);
        } else if (MCRExternalStoreServiceUtils.checkPathIsArchiveInfoFile(path)) {
            addArchiveInfosToIndex(derivateId, path);
        }
    }

    private void deleteFileInfosFromIndex(MCRDerivate derivate) {
        final MCRObjectID derivateId = derivate.getId();
        if (!checkDerivateClassification(derivate)) {
            LOGGER.debug("Derivate {} is not external store classified. Skipping", derivateId);
            return;
        }
        STORE_INFO_INDEX.deleteFileInfos(derivateId);
    }

    private static boolean checkDerivateClassification(MCRDerivate derivate) {
        return derivate.getDerivate().getClassifications().stream()
            .anyMatch(c -> MCRExternalStoreServiceUtils.checkExternalStoreClassification(c));
    }

}
