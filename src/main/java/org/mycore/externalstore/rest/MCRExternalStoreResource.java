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
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.mycore.access.MCRAccessManager;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.externalstore.MCRExternalStore;
import org.mycore.externalstore.MCRExternalStoreConstants;
import org.mycore.externalstore.MCRExternalStoreProviderFactory;
import org.mycore.externalstore.MCRExternalStoreService;
import org.mycore.externalstore.exception.MCRExternalStoreException;
import org.mycore.externalstore.exception.MCRExternalStoreNoAccessException;
import org.mycore.externalstore.index.MCRExternalStoreInfoIndex;
import org.mycore.externalstore.index.MCRExternalStoreInfoIndexManager;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo.FileFlag;
import org.mycore.externalstore.rest.dto.MCRCreateStoreDto;
import org.mycore.externalstore.rest.dto.MCRDerivateInfoDto;
import org.mycore.externalstore.rest.dto.MCRDerivateInfosDto;
import org.mycore.externalstore.rest.dto.MCRDownloadUrlDto;
import org.mycore.externalstore.rest.dto.MCRExternalStoreFileInfoDto;
import org.mycore.restapi.annotations.MCRRequireTransaction;
import org.mycore.restapi.v2.MCRErrorResponse;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

/**
 * Provides the external stores of an object as rest resource.
 * <p>
 * A store is represented by a derivate of the object.
 */
@Path("es/{" + MCRExternalStoreResource.PARAM_OBJ_ID + "}/stores")
public class MCRExternalStoreResource {

    static final String PARAM_OBJ_ID = "object_id";

    private static final String PARAM_DER_ID = "derivate_id";

    private static final String PARAM_PATH = "path";

    private static final String CREATE_DERIVATE_PERMISSION = "create-derivate";

    private static final String HEADER_TOTAL_COUNT = "X-Total-Count";

    private static final int STATUS_UNPROCESSABLE_ENTITY = 422;

    private static final Optional<String> DOWNLOD_PROXY_URL
        = MCRConfiguration2.getString(MCRExternalStoreConstants.PROPERTY_PREFIX + "ProxyServlet.Url");

    private static final MCRExternalStoreInfoIndex INDEX = MCRExternalStoreInfoIndexManager.getInfoIndex();

    @PathParam(PARAM_OBJ_ID)
    private MCRObjectID objectId;

    @Context
    private UriInfo uriInfo;

    /**
     * Returns the stores of the object.
     *
     * @return derivate infos dto
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public MCRDerivateInfosDto listStores() {
        ensureObjectExists();
        if (!MCRAccessManager.checkPermission(objectId, MCRAccessManager.PERMISSION_READ)) {
            throw error(Response.Status.FORBIDDEN.getStatusCode(), "No permission to read " + objectId);
        }
        final List<MCRDerivateInfoDto> derivateInfos = MCRExternalStoreResourceHelper
            .listDerivateInformations(objectId);
        final boolean canCreateStore = checkCreateStorePermission();
        return new MCRDerivateInfosDto(derivateInfos, canCreateStore);
    }

    /**
     * Creates a store for the object.
     *
     * @param createStore store type and store settings
     * @return response with status 201 and location of the created store
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @MCRRequireTransaction
    public Response createStore(MCRCreateStoreDto createStore) {
        ensureObjectExists();
        if (!checkCreateStorePermission()) {
            throw error(Response.Status.FORBIDDEN.getStatusCode(), "No permission to create store for " + objectId);
        }
        if (createStore == null || createStore.type() == null || createStore.settings() == null) {
            throw error(Response.Status.BAD_REQUEST.getStatusCode(), "Store type and settings are required");
        }
        if (!MCRExternalStoreProviderFactory.isSupported(createStore.type())) {
            throw error(Response.Status.BAD_REQUEST.getStatusCode(),
                "Unsupported store type: " + createStore.type());
        }
        final MCRObjectID derivateId;
        try {
            derivateId = MCRExternalStoreService.createStore(objectId, createStore.type(), createStore.settings());
        } catch (IllegalArgumentException e) {
            throw error(Response.Status.BAD_REQUEST.getStatusCode(), "Invalid store settings", e);
        } catch (MCRExternalStoreNoAccessException e) {
            throw error(STATUS_UNPROCESSABLE_ENTITY, "Cannot access store with given settings", e);
        } catch (MCRExternalStoreException e) {
            throw error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Error while creating store", e);
        }
        final URI location = uriInfo.getAbsolutePathBuilder().path(derivateId.toString()).build();
        return Response.created(location).build();
    }

    /**
     * Deletes a store of the object.
     *
     * @param derivateId derivate id of the store
     * @return response with status 204
     */
    @DELETE
    @Path("{" + PARAM_DER_ID + "}")
    @MCRRequireTransaction
    public Response deleteStore(@PathParam(PARAM_DER_ID) MCRObjectID derivateId) {
        ensureObjectExists();
        ensureStoreExists(derivateId);
        if (!MCRAccessManager.checkPermission(derivateId, MCRAccessManager.PERMISSION_DELETE)) {
            throw error(Response.Status.FORBIDDEN.getStatusCode(), "No permission to delete " + derivateId);
        }
        try {
            MCRExternalStoreService.getInstance().deleteStore(derivateId);
        } catch (MCRExternalStoreException e) {
            throw error(Response.Status.FORBIDDEN.getStatusCode(), "No permission to delete " + derivateId, e);
        }
        return Response.noContent().build();
    }

    /**
     * Returns the file infos of the root directory of a store.
     *
     * @param derivateId derivate id of the store
     * @param offset offset
     * @param limit limit
     * @return response with list over file info dtos
     */
    @GET
    @Path("{" + PARAM_DER_ID + "}/files")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFiles(@PathParam(PARAM_DER_ID) MCRObjectID derivateId,
        @DefaultValue("0") @QueryParam("offset") int offset,
        @DefaultValue("" + Integer.MAX_VALUE) @QueryParam("limit") int limit) {
        return listFiles(derivateId, "", offset, limit);
    }

    /**
     * Returns the file infos of a directory or archive of a store.
     *
     * @param derivateId derivate id of the store
     * @param path path of the directory or archive
     * @param offset offset
     * @param limit limit
     * @return response with list over file info dtos
     */
    @GET
    @Path("{" + PARAM_DER_ID + "}/files/{" + PARAM_PATH + ": .+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFiles(@PathParam(PARAM_DER_ID) MCRObjectID derivateId,
        @PathParam(PARAM_PATH) String path, @DefaultValue("0") @QueryParam("offset") int offset,
        @DefaultValue("" + Integer.MAX_VALUE) @QueryParam("limit") int limit) {
        ensureObjectExists();
        ensureStoreExists(derivateId);
        ensureDerivateReadPermission(derivateId);
        final List<MCRExternalStoreFileInfoDto> fileInfos = listFileInfos(derivateId, path);
        final List<MCRExternalStoreFileInfoDto> result = fileInfos.stream().skip(offset).limit(limit).toList();
        return Response.ok(result).header(HEADER_TOTAL_COUNT, fileInfos.size()).build();
    }

    private List<MCRExternalStoreFileInfoDto> listFileInfos(MCRObjectID derivateId, String path) {
        final MCRExternalStoreFileInfo fileInfo = path.isEmpty()
            ? new MCRExternalStoreFileInfo.Builder("", "").directory(true).build()
            : findFileInfo(derivateId, path);

        if (fileInfo.isDirectory() && !fileInfo.flags().contains(FileFlag.ARCHIVE_ENTRY)) {
            return INDEX.listFileInfos(derivateId, path).stream()
                .map(i -> MCRExternalStoreResourceHelper.toDto(i, true)).toList();
        }
        if (fileInfo.flags().contains(FileFlag.ARCHIVE)
            || (fileInfo.isDirectory() && fileInfo.flags().contains(FileFlag.ARCHIVE_ENTRY))) {
            return INDEX.listFileInfos(derivateId, path).stream()
                .map(i -> MCRExternalStoreResourceHelper.toDto(i, false)).toList();
        }
        throw error(Response.Status.BAD_REQUEST.getStatusCode(), "Path is not a directory or archive: " + path);
    }

    /**
     * Returns a download url for a file of a store.
     *
     * @param derivateId derivate id of the store
     * @param path path of the file
     * @return download url dto
     */
    @GET
    @Path("{" + PARAM_DER_ID + "}/download-url/{" + PARAM_PATH + ": .+}")
    @Produces(MediaType.APPLICATION_JSON)
    public MCRDownloadUrlDto getDownloadUrl(@PathParam(PARAM_DER_ID) MCRObjectID derivateId,
        @PathParam(PARAM_PATH) String path) {
        ensureObjectExists();
        ensureStoreExists(derivateId);
        ensureDerivateReadPermission(derivateId);
        final MCRExternalStoreFileInfo fileInfo = findFileInfo(derivateId, path);
        ensureFileIsDownloadable(fileInfo);
        ensureAllowedFileSize(fileInfo);
        ensureFileIntegrity(derivateId, fileInfo);
        return new MCRDownloadUrlDto(createDownloadUrl(derivateId, path));
    }

    private MCRExternalStoreFileInfo findFileInfo(MCRObjectID derivateId, String path) {
        return INDEX.findFileInfo(derivateId, path)
            .orElseThrow(() -> error(Response.Status.NOT_FOUND.getStatusCode(), "Path does not exist: " + path));
    }

    private void ensureFileIsDownloadable(MCRExternalStoreFileInfo fileInfo) {
        if (fileInfo.isDirectory()) {
            throw error(Response.Status.BAD_REQUEST.getStatusCode(), "File is a directory");
        }
        if (fileInfo.flags().contains(MCRExternalStoreFileInfo.FileFlag.ARCHIVE_ENTRY)) {
            throw error(Response.Status.BAD_REQUEST.getStatusCode(), "File is part of an archive");
        }
    }

    private void ensureFileIntegrity(MCRObjectID derivateId, MCRExternalStoreFileInfo fileInfo) {
        String storeArchiveChecksum;
        try {
            storeArchiveChecksum = MCRExternalStoreService.getInstance().getStore(derivateId)
                .getFileInfo(fileInfo.getAbsolutePath()).checksum();
        } catch (IOException e) {
            throw error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Detected integrity violation", e);
        }
        if (!Objects.equals(fileInfo.checksum(), storeArchiveChecksum)) {
            throw error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Detected integrity violation");
        }
    }

    private String createDownloadUrl(MCRObjectID derivateId, String path) {
        final MCRExternalStore store = MCRExternalStoreService.getInstance().getStore(derivateId);
        final URL downloadUrl = store.getStoreProvider().getDownloadUrl(path);
        if (store.useDownloadProxy()) {
            final String downloadProxyUrl = store.getCustomDownloadProxyUrl();
            if (downloadProxyUrl != null) {
                return createProxyDownloadUrl(downloadProxyUrl, downloadUrl);
            } else if (!DOWNLOD_PROXY_URL.isEmpty()) {
                return createProxyDownloadUrl(DOWNLOD_PROXY_URL.get() + "/" + derivateId, downloadUrl);
            }
            throw error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Internal proxy url is not set");
        }
        return downloadUrl.toString();
    }

    private String createProxyDownloadUrl(String proxy, URL downloadUrl) {
        return String.format(Locale.ROOT, "%s%s?%s", proxy, downloadUrl.getPath(), downloadUrl.getQuery());
    }

    private void ensureAllowedFileSize(MCRExternalStoreFileInfo fileInfo) {
        if (fileInfo.size() > MCRExternalStoreConstants.MAX_DOWNLOAD_SIZE) {
            throw error(Response.Status.BAD_REQUEST.getStatusCode(), "File size is not allowed to download");
        }
    }

    private void ensureObjectExists() {
        if (!MCRMetadataManager.exists(objectId)) {
            throw error(Response.Status.NOT_FOUND.getStatusCode(), objectId + " does not exist");
        }
    }

    private void ensureStoreExists(MCRObjectID derivateId) {
        if (!MCRExternalStoreResourceHelper.listStoreDerivates(objectId).contains(derivateId.toString())) {
            throw error(Response.Status.NOT_FOUND.getStatusCode(),
                derivateId + " is not a store of " + objectId);
        }
    }

    // TODO may replace with MCRMetadataManager#checkCreatePrivilege
    private boolean checkCreateStorePermission() {
        return MCRAccessManager.checkPermission(CREATE_DERIVATE_PERMISSION)
            && MCRAccessManager.checkPermission(objectId, MCRAccessManager.PERMISSION_WRITE);
    }

    /**
     * Ensures that the current user may access the content of a derivate.
     * <p>
     * Only {@link MCRAccessManager#PERMISSION_READ} is accepted here. {@code PERMISSION_VIEW} would be the natural
     * permission for browsing the file structure without downloading, but MIR applies its embargo check in
     * {@code MIRStrategy} to {@code read} only, while granting {@code view} to everyone through the
     * {@code default_mods} default rule. Accepting {@code view} would therefore bypass the embargo as well as the
     * {@code mir_access:intern} and {@code state:blocked}/{@code state:deleted} restrictions. Once MIR applies those
     * restrictions to {@code view} too, this check can be relaxed again.
     *
     * @param derivateId derivate id
     * @throws WebApplicationException with status 403 if the current user has no read permission
     */
    private void ensureDerivateReadPermission(MCRObjectID derivateId) {
        if (!MCRAccessManager.checkPermission(derivateId, MCRAccessManager.PERMISSION_READ)) {
            throw error(Response.Status.FORBIDDEN.getStatusCode(), "No permission to read " + derivateId);
        }
    }

    private static WebApplicationException error(int status, String message) {
        return MCRErrorResponse.fromStatus(status).withMessage(message).toException();
    }

    private static WebApplicationException error(int status, String message, Throwable cause) {
        final MCRErrorResponse response = MCRErrorResponse.fromStatus(status).withMessage(message).withCause(cause);
        // expose the cause only for client errors, server errors are logged
        if (status < Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
            response.withDetail(cause.getMessage());
        }
        return response.toException();
    }
}
