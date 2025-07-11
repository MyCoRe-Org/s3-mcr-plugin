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

package org.mycore.externalstore.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.mycore.access.MCRAccessManager;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.externalstore.MCRExternalStore;
import org.mycore.externalstore.MCRExternalStoreConstants;
import org.mycore.externalstore.MCRExternalStoreService;
import org.mycore.externalstore.MCRExternalStoreServiceUtils;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;
import org.mycore.externalstore.rest.dto.MCRDerivateInfoDto;
import org.mycore.externalstore.rest.dto.MCRDerivateTitleDto;
import org.mycore.externalstore.rest.dto.MCRExternalStoreFileInfoDto;
import org.mycore.externalstore.util.MCRExternalStoreUtils;
import org.mycore.services.i18n.MCRTranslation;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

/**
 * Provides helper methods for external store resource.
 */
public class MCRExternalStoreResourceHelper {

    /**
     * Returns an input stream response with file specified by path.
     *
     * @param derivateId derivate id
     * @param path path
     * @return response
     */
    protected static Response getFile(MCRObjectID derivateId, String path) {
        final String fileName = MCRExternalStoreUtils.getFileName(path);
        return Response.ok(new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                try (InputStream inputStream = MCRExternalStoreService.getInstance().getStore(derivateId)
                    .newInputStream(path)) {
                    inputStream.transferTo(outputStream);
                }
            }
        })
            .header("Content-Disposition", "attachment; filename=" + fileName).build();
    }

    /**
     * Returns {@link MCRExternalStoreFileInfoDto} from file info.
     *
     * @param fileInfo file info
     * @param downloadable if file is downloadable
     * @return file info dto
     */
    protected static MCRExternalStoreFileInfoDto toDto(MCRExternalStoreFileInfo fileInfo, boolean downloadable) {
        final Set<MCRExternalStoreFileInfoDto.MCRFileCapability> capabilities
            = EnumSet.noneOf(MCRExternalStoreFileInfoDto.MCRFileCapability.class);
        if (downloadable && !fileInfo.isDirectory() && fileInfo.size() != null
            && fileInfo.size() <= MCRExternalStoreConstants.MAX_DOWNLOAD_SIZE) {
            capabilities.add(MCRExternalStoreFileInfoDto.MCRFileCapability.DOWNLOAD);
        }
        return new MCRExternalStoreFileInfoDto(fileInfo.name(), fileInfo.parentPath(),
            fileInfo.isDirectory(), fileInfo.size(), fileInfo.checksum(), fileInfo.lastModified(), fileInfo.flags(),
            capabilities);
    }

    /**
     * Returns a list of {@link MCRDerivateInfoDto} elements by object id.
     *
     * @param objectId object id
     * @return list of derivate info dto elements
     */
    protected static List<MCRDerivateInfoDto> listDerivateInformations(MCRObjectID objectId) {
        return listStoreDerivates(objectId).stream()
            .map(MCRObjectID::getInstance)
            .map(MCRMetadataManager::retrieveMCRDerivate)
            .map(der -> {
                final boolean canView = MCRAccessManager.checkPermission(der.getId(), MCRAccessManager.PERMISSION_VIEW)
                    || MCRAccessManager.checkPermission(der.getId(), MCRAccessManager.PERMISSION_READ);
                final boolean canDelete = MCRAccessManager.checkPermission(der.getId(),
                    MCRAccessManager.PERMISSION_DELETE);
                final boolean canEdit = MCRAccessManager.checkPermission(der.getId(),
                    MCRAccessManager.PERMISSION_WRITE);

                final List<MCRDerivateTitleDto> titles = der.getDerivate().getTitles().stream()
                    .sorted((t1, t2) -> getLanguageValue(t2) - getLanguageValue(t1))
                    .map(title -> new MCRDerivateTitleDto(title.getText(),
                        title.getLang(),
                        title.getForm()))
                    .collect(Collectors.toList());

                final MCRExternalStore store = MCRExternalStoreService.getInstance().getStore(der.getId());
                final Map<String, String> metadataMap = canEdit ? store.getStoreSettings() : Collections.emptyMap();
                return new MCRDerivateInfoDto(der.getId().toString(), titles, metadataMap, canView, canDelete, canEdit);
            })
            .collect(Collectors.toList());
    }

    /**
     * Returns a list of objects derivate id elements which are store classified.
     *
     * @param objectId the object id
     * @return list of derivate id elements
     */
    protected static List<String> listStoreDerivates(MCRObjectID objectId) {
        final MCRObject object = MCRMetadataManager.retrieveMCRObject(objectId);
        return MCRExternalStoreServiceUtils.listExternalStoreDerivateIds(object);
    }

    private static int getLanguageValue(MCRMetaLangText t1) {
        return Objects.equals(MCRTranslation.getCurrentLocale().getLanguage(), t1.getLang()) ? 1 : 0;
    }
}
