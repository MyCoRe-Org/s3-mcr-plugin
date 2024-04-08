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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.mycore.access.MCRAccessManager;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.externalstore.MCRExternalStore;
import org.mycore.externalstore.MCRExternalStoreConstants;
import org.mycore.externalstore.MCRExternalStoreService;
import org.mycore.externalstore.exception.MCRExternalStoreException;
import org.mycore.externalstore.index.MCRExternalStoreInfoIndex;
import org.mycore.externalstore.index.MCRExternalStoreInfoIndexManager;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo.FileFlag;
import org.mycore.externalstore.rest.dto.MCRDerivateInfoDto;
import org.mycore.externalstore.rest.dto.MCRDerivateInfosDto;
import org.mycore.externalstore.rest.dto.MCRExternalStoreFileInfoDto;
import org.mycore.restapi.annotations.MCRRequireTransaction;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Provides external store rest resource.
 */
@Path("es/")
public class MCRExternalStoreResource {

    private static final String PARAM_OBJ_ID = "object_id";

    private static final String PARAM_DER_ID = "derivate_id";

    private static final String PARAM_STORE_TYPE = "store_type";

    private static final String PARAM_PATH = "path";

    private static final String CREATE_DERIVATE_PERMISSION = "create-derivate";

    private static final Optional<String> DOWNLOD_PROXY_URL
        = MCRConfiguration2.getString(MCRExternalStoreConstants.PROPERTY_PREFIX + "ProxyServlet.Url");

    private static final MCRExternalStoreInfoIndex INDEX = MCRExternalStoreInfoIndexManager.getInfoIndex();

    /**
     * Returns derivate info for object.
     *
     * @param objectId object id
     * @return derivate infos dto
     */
    @GET
    @Path("{" + PARAM_OBJ_ID + "}/info")
    @Produces(MediaType.APPLICATION_JSON)
    public MCRDerivateInfosDto listInfo(@PathParam(PARAM_OBJ_ID) MCRObjectID objectId) {
        ensureObjectExists(objectId);
        if (!MCRAccessManager.checkPermission(objectId, MCRAccessManager.PERMISSION_READ)) {
            throw new ForbiddenException();
        }
        final List<MCRDerivateInfoDto> derivateInfos = MCRExternalStoreResourceHelper
            .listDerivateInformations(objectId);
        final boolean canCreateStore = checkCreateStorePermission(objectId.toString());
        return new MCRDerivateInfosDto(derivateInfos, canCreateStore);
    }

    /**
     * Creates external store for object with store settings.
     *
     * @param objectId object id
     * @param storeType store type
     * @param storeProviderSettings map over store settings
     * @return response
     */
    @POST
    @Path("{" + PARAM_OBJ_ID + "}/add/{" + PARAM_STORE_TYPE + "}/")
    @Consumes(MediaType.APPLICATION_JSON)
    @MCRRequireTransaction
    public Response createStore(@PathParam(PARAM_OBJ_ID) MCRObjectID objectId,
        @PathParam(PARAM_STORE_TYPE) String storeType, Map<String, String> storeProviderSettings) {
        ensureObjectExists(objectId);
        if (!checkCreateStorePermission(objectId.toString())) {
            throw new ForbiddenException();
        }
        try {
            MCRExternalStoreService.createStore(objectId, storeType, storeProviderSettings);
        } catch (MCRExternalStoreException e) {
            throw new BadRequestException(e);
        }
        return Response.ok().build();
    }

    /**
     * Returns file infos for external store.
     *
     * @param objectId object id
     * @param base64DerivateId derivate id
     * @param offset offset
     * @param limit limit
     * @return response with list over file infos dtos
     */
    @GET
    @Path("{" + PARAM_OBJ_ID + "}/list/{" + PARAM_DER_ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFileInfos(@PathParam(PARAM_OBJ_ID) MCRObjectID objectId,
        @PathParam(PARAM_DER_ID) String base64DerivateId, @DefaultValue("0") @QueryParam("offset") int offset,
        @DefaultValue("" + Integer.MAX_VALUE) @QueryParam("limit") int limit) {
        return listFileInfos(objectId, base64DerivateId, "", offset, limit);
    }

    /**
     * Returns file infos for external store by path.
     *
     * @param objectId object id
     * @param base64DerivateId derivate id
     * @param base64Path path
     * @param offset offset
     * @param limit limit
     * @return response with list over file infos dtos
     */
    @GET
    @Path("{" + PARAM_OBJ_ID + "}/list/{" + PARAM_DER_ID + "}/{" + PARAM_PATH + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFileInfos(@PathParam(PARAM_OBJ_ID) MCRObjectID objectId,
        @PathParam(PARAM_DER_ID) String base64DerivateId, @PathParam(PARAM_PATH) String base64Path,
        @DefaultValue("0") @QueryParam("offset") int offset,
        @DefaultValue("2147483647") @QueryParam("limit") int limit) {
        ensureObjectExists(objectId);
        final String derivateIdStr = decodeBase64(base64DerivateId);
        ensureObjectHasChild(objectId, derivateIdStr);
        ensureDerivateReadPermission(derivateIdStr);
        final String path = decodeBase64(base64Path);
        final MCRObjectID derivateId = MCRObjectID.getInstance(derivateIdStr);
        final List<MCRExternalStoreFileInfoDto> fileInfos = listFileInfos(derivateId, path);
        final List<MCRExternalStoreFileInfoDto> result = fileInfos.stream().skip(offset).limit(limit).toList();
        return Response.ok(result).header("X-Total-Count", fileInfos.size()).build();
    }

    private List<MCRExternalStoreFileInfoDto> listFileInfos(MCRObjectID derivateId, String path) {
        final MCRExternalStoreFileInfo fileInfo = path.isEmpty()
            ? new MCRExternalStoreFileInfo.Builder("", "").directory(true).build()
            : INDEX.findFileInfo(derivateId, path).orElseThrow(() -> new BadRequestException("Path does not exist"));

        if (fileInfo.isDirectory() && !fileInfo.flags().contains(FileFlag.ARCHIVE_ENTRY)) {
            return INDEX.listFileInfos(derivateId, path).stream()
                .map(i -> MCRExternalStoreResourceHelper.toDto(i, true)).toList();
        }
        if (fileInfo.flags().contains(FileFlag.ARCHIVE)
            || (fileInfo.isDirectory() && fileInfo.flags().contains(FileFlag.ARCHIVE_ENTRY))) {
            return INDEX.listFileInfos(derivateId, path).stream()
                .map(i -> MCRExternalStoreResourceHelper.toDto(i, false)).toList();
        }
        throw new BadRequestException("Path is not a directory or archive");
    }

    /**
     * Creates download token for file.
     *
     * @param objectId object id
     * @param base64DerivateId derivate id
     * @param base64Path path
     * @return response with download token
     */
    @GET
    @Path("{" + PARAM_OBJ_ID + "}/download/{" + PARAM_DER_ID + "}/{" + PARAM_PATH + "}")
    @Produces({ "text/plain", MediaType.APPLICATION_JSON })
    public String getDownloadUrl(@PathParam(PARAM_OBJ_ID) MCRObjectID objectId,
        @PathParam(PARAM_DER_ID) String base64DerivateId, @PathParam(PARAM_PATH) String base64Path) {
        ensureObjectExists(objectId);
        final String derivateIdStr = decodeBase64(base64DerivateId);
        ensureObjectHasChild(objectId, derivateIdStr);
        ensureDerivateReadPermission(derivateIdStr);
        final MCRObjectID derivateId = MCRObjectID.getInstance(derivateIdStr);
        final String path = decodeBase64(base64Path);
        final MCRExternalStoreFileInfo fileInfo = INDEX.findFileInfo(derivateId, path)
            .orElseThrow(() -> new BadRequestException("File does not exist"));
        ensureFileIsDownloadable(fileInfo);
        ensureAllowedFileSize(fileInfo);
        ensureFileIntegrity(derivateId, fileInfo);
        return getDownloadUrl(derivateId, path);
    }

    private void ensureFileIsDownloadable(MCRExternalStoreFileInfo fileInfo) {
        if (fileInfo.isDirectory()) {
            throw new BadRequestException("File is a directory");
        }
        if (fileInfo.flags().contains(MCRExternalStoreFileInfo.FileFlag.ARCHIVE_ENTRY)) {
            throw new BadRequestException("File is part of an archive.");
        }
    }

    private void ensureFileIntegrity(MCRObjectID derivateId, MCRExternalStoreFileInfo fileInfo) {
        String storeArchiveChecksum;
        try {
            storeArchiveChecksum = MCRExternalStoreService.getInstance().getStore(derivateId)
                .getFileInfo(fileInfo.getAbsolutePath()).checksum();
        } catch (IOException e) {
            throw new InternalServerErrorException("Detected integrity violation");
        }
        if (!Objects.equals(fileInfo.checksum(), storeArchiveChecksum)) {
            throw new InternalServerErrorException("Detected integrity violation");
        }
    }

    final String getDownloadUrl(MCRObjectID derivateId, String path) {
        final MCRExternalStore store = MCRExternalStoreService.getInstance().getStore(derivateId);
        final URL downloadUrl = store.getStoreProvider().getDownloadUrl(path);
        if (store.useDownloadProxy()) {
            final String downloadProxyUrl = store.getCustomDownloadProxyUrl();
            if (downloadProxyUrl != null) {
                return createProxyDownloadUrl(downloadProxyUrl, downloadUrl);
            } else if (!DOWNLOD_PROXY_URL.isEmpty()) {
                return createProxyDownloadUrl(DOWNLOD_PROXY_URL.get() + "/" + derivateId, downloadUrl);
            }
            throw new InternalServerErrorException("Internal proxy url is not set");
        }
        return downloadUrl.toString();
    }

    private String createProxyDownloadUrl(String proxy, URL downloadUrl) {
        return String.format("%s%s?%s", proxy, downloadUrl.getPath(), downloadUrl.getQuery());
    }

    private void ensureAllowedFileSize(MCRExternalStoreFileInfo fileInfo) {
        if (fileInfo.size() > MCRExternalStoreConstants.MAX_DOWNLOAD_SIZE) {
            throw new BadRequestException("File size is not allowed to download");
        }
    }

    private void ensureObjectExists(MCRObjectID objectId) {
        if (!MCRMetadataManager.exists(objectId)) {
            throw new BadRequestException(objectId + " does not exist");
        }
    }

    private void ensureObjectHasChild(MCRObjectID objectId, String derivateIdStr) {
        if (!MCRExternalStoreResourceHelper.listStoreDerivates(objectId).contains(derivateIdStr)) {
            throw new BadRequestException(derivateIdStr + " is not a store derivate of " + objectId);
        }
    }

    // TODO may replace with MCRMetadataManager#checkCreatePrivilege
    private boolean checkCreateStorePermission(String objectIdStr) {
        return MCRAccessManager.checkPermission(CREATE_DERIVATE_PERMISSION)
            && MCRAccessManager.checkPermission(objectIdStr, MCRAccessManager.PERMISSION_WRITE);
    }

    private void ensureDerivateReadPermission(String derivateIdStr) {
        if (!MCRAccessManager.checkPermission(derivateIdStr, MCRAccessManager.PERMISSION_READ)
            && !MCRAccessManager.checkPermission(derivateIdStr, MCRAccessManager.PERMISSION_VIEW)) {
            throw new ForbiddenException();
        }
    }

    private static String decodeBase64(String base64) {
        return new String(Base64.getUrlDecoder().decode(base64), StandardCharsets.UTF_8);
    }
}
