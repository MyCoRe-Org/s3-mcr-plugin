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

import static org.mycore.restapi.v2.MCRRestAuthorizationFilter.PARAM_DERID;
import static org.mycore.restapi.v2.MCRRestAuthorizationFilter.PARAM_DER_PATH;
import static org.mycore.restapi.v2.MCRRestAuthorizationFilter.PARAM_MCRID;

import java.net.URI;
import java.net.URL;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.mycore.access.MCRAccessManager;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.externalstore.MCRExternalStore;
import org.mycore.externalstore.MCRExternalStoreConstants;
import org.mycore.externalstore.MCRExternalStoreProviderFactory;
import org.mycore.externalstore.MCRExternalStoreService;
import org.mycore.externalstore.MCRExternalStoreServiceUtils;
import org.mycore.externalstore.exception.MCRExternalStoreException;
import org.mycore.externalstore.exception.MCRExternalStoreIntegrityViolationException;
import org.mycore.externalstore.exception.MCRExternalStorePermissionException;
import org.mycore.externalstore.exception.MCRExternalStoreUnavailableException;
import org.mycore.externalstore.index.MCRExternalStoreInfoIndex;
import org.mycore.externalstore.index.MCRExternalStoreInfoIndexManager;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo.FileFlag;
import org.mycore.externalstore.rest.dto.MCRCreateStoreDto;
import org.mycore.externalstore.rest.dto.MCRDerivateInfoDto;
import org.mycore.externalstore.rest.dto.MCRDerivateInfosDto;
import org.mycore.externalstore.rest.dto.MCRDownloadUrlDto;
import org.mycore.externalstore.rest.dto.MCRExternalStoreFileInfoDto;
import org.mycore.externalstore.rest.dto.MCRStoreCapability;
import org.mycore.frontend.jersey.MCRCacheControl;
import org.mycore.restapi.annotations.MCRRequireTransaction;
import org.mycore.restapi.v2.MCRErrorResponse;
import org.mycore.restapi.v2.MCRRestAuthorizationFilter;
import org.mycore.restapi.v2.MCRRestDerivates;

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
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

/**
 * Provides the external stores of an object as rest resource.
 * <p>
 * A store is represented by a derivate of the object. The path parameters are named like in
 * {@link MCRRestAuthorizationFilter}, so the filter also checks the base permissions on object and derivate.
 */
@Path("objects/{" + PARAM_MCRID + "}/external-stores")
public class MCRExternalStoreResource {

    private static final String PATH_PARAM_PATH = "{" + PARAM_DER_PATH + ":(/[^/]+)*}";

    private static final String PATH_PARAM_FILE_PATH = "{" + PARAM_DER_PATH + ":(/[^/]+)+}";

    private static final String CREATE_DERIVATE_PERMISSION = "create-derivate";

    private static final String HEADER_TOTAL_COUNT = "X-Total-Count";

    private static final int STATUS_UNPROCESSABLE_ENTITY = 422;

    private static final Optional<String> DOWNLOD_PROXY_URL
        = MCRConfiguration2.getString(MCRExternalStoreConstants.PROPERTY_PREFIX + "ProxyServlet.Url");

    private static final MCRExternalStoreInfoIndex INDEX = MCRExternalStoreInfoIndexManager.getInfoIndex();

    @PathParam(PARAM_MCRID)
    private MCRObjectID objectId;

    @Context
    private UriInfo uriInfo;

    @Context
    private HttpHeaders httpHeaders;

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
        final Set<MCRStoreCapability> capabilities = checkCreateStorePermission()
            ? EnumSet.of(MCRStoreCapability.CREATE)
            : EnumSet.noneOf(MCRStoreCapability.class);
        return new MCRDerivateInfosDto(derivateInfos, capabilities);
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
        if (!MCRExternalStoreProviderFactory.isConfigured(createStore.type())) {
            throw error(Response.Status.BAD_REQUEST.getStatusCode(),
                "Unsupported store type: " + createStore.type());
        }
        final MCRObjectID derivateId;
        try {
            derivateId = MCRExternalStoreService.createStore(objectId, createStore.type(), createStore.settings());
        } catch (IllegalArgumentException e) {
            throw error(Response.Status.BAD_REQUEST.getStatusCode(), "Invalid store settings", e);
        } catch (MCRExternalStoreUnavailableException e) {
            throw error(STATUS_UNPROCESSABLE_ENTITY, "Store is not available with given settings", e);
        } catch (MCRExternalStorePermissionException e) {
            throw error(Response.Status.FORBIDDEN.getStatusCode(), "No permission to create store for " + objectId, e);
        } catch (MCRExternalStoreException e) {
            throw error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Error while creating store", e);
        }
        final URI location = uriInfo.getAbsolutePathBuilder().path(derivateId.toString()).build();
        return Response.created(location).build();
    }

    /**
     * Returns a store of the object.
     *
     * @param derivateId derivate id of the store
     * @return derivate info dto
     */
    @GET
    @Path("{" + PARAM_DERID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public MCRDerivateInfoDto getStore(@PathParam(PARAM_DERID) MCRObjectID derivateId) {
        ensureObjectExists();
        ensureStoreExists(derivateId);
        ensureDerivateReadPermission(derivateId);
        return MCRExternalStoreResourceHelper.getDerivateInformation(derivateId);
    }

    /**
     * Deletes a store of the object.
     *
     * @param derivateId derivate id of the store
     * @return response with status 204
     */
    @DELETE
    @Path("{" + PARAM_DERID + "}")
    @MCRRequireTransaction
    public Response deleteStore(@PathParam(PARAM_DERID) MCRObjectID derivateId) {
        ensureObjectExists();
        ensureStoreExists(derivateId);
        if (!MCRAccessManager.checkPermission(derivateId, MCRAccessManager.PERMISSION_DELETE)) {
            throw error(Response.Status.FORBIDDEN.getStatusCode(), "No permission to delete " + derivateId);
        }
        try {
            MCRExternalStoreService.getInstance().deleteStore(derivateId);
        } catch (MCRExternalStorePermissionException e) {
            throw error(Response.Status.FORBIDDEN.getStatusCode(), "No permission to delete " + derivateId, e);
        } catch (MCRExternalStoreException e) {
            throw error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Error while deleting store", e);
        }
        return Response.noContent().build();
    }

    /**
     * Returns the content of a path of a store.
     * <p>
     * A directory is listed. If JSON is requested, an archive is listed and the file info of other files is returned.
     * Otherwise, the client is redirected to the download url of the file.
     *
     * @param derivateId derivate id of the store
     * @param path path, empty for the root directory
     * @param offset offset of a listing
     * @param limit limit of a listing
     * @return response with list over file info dtos, with file info dto or with redirect to the download url
     */
    @GET
    @Path("{" + PARAM_DERID + "}/contents" + PATH_PARAM_PATH)
    @Produces({ MediaType.APPLICATION_JSON, MediaType.WILDCARD })
    public Response getContents(@PathParam(PARAM_DERID) MCRObjectID derivateId,
        @PathParam(PARAM_DER_PATH) String path, @DefaultValue("0") @QueryParam("offset") int offset,
        @DefaultValue("" + Integer.MAX_VALUE) @QueryParam("limit") int limit) {
        ensureValidPaging(offset, limit);
        ensureObjectExists();
        ensureStoreExists(derivateId);
        ensureDerivateReadPermission(derivateId);
        final String relativePath = toRelativePath(path);
        final MCRExternalStoreFileInfo fileInfo = relativePath.isEmpty()
            ? new MCRExternalStoreFileInfo.Builder("", "").directory(true).build()
            : findFileInfo(derivateId, relativePath);
        if (fileInfo.isDirectory() || (fileInfo.flags().contains(FileFlag.ARCHIVE) && isJsonRequested())) {
            final List<MCRExternalStoreFileInfoDto> fileInfos = listFileInfos(derivateId, relativePath, fileInfo);
            final List<MCRExternalStoreFileInfoDto> result = fileInfos.stream().skip(offset).limit(limit).toList();
            return Response.ok(result, MediaType.APPLICATION_JSON_TYPE)
                .header(HEADER_TOTAL_COUNT, fileInfos.size()).build();
        }
        if (isJsonRequested()) {
            final boolean downloadable = !fileInfo.flags().contains(FileFlag.ARCHIVE_ENTRY);
            return Response.ok(MCRExternalStoreResourceHelper.toDto(fileInfo, downloadable),
                MediaType.APPLICATION_JSON_TYPE).build();
        }
        return Response.seeOther(URI.create(getCheckedDownloadUrl(derivateId, relativePath, fileInfo)))
            .cacheControl(noStore()).build();
    }

    private List<MCRExternalStoreFileInfoDto> listFileInfos(MCRObjectID derivateId, String path,
        MCRExternalStoreFileInfo fileInfo) {
        final boolean downloadable = !fileInfo.flags().contains(FileFlag.ARCHIVE)
            && !fileInfo.flags().contains(FileFlag.ARCHIVE_ENTRY);
        return INDEX.listFileInfos(derivateId, path).stream()
            .map(i -> MCRExternalStoreResourceHelper.toDto(i, downloadable)).toList();
    }

    /**
     * Returns a download url for a file of a store.
     *
     * @param derivateId derivate id of the store
     * @param path path of the file
     * @return download url dto
     */
    @GET
    @Path("{" + PARAM_DERID + "}/download-url" + PATH_PARAM_FILE_PATH)
    @Produces(MediaType.APPLICATION_JSON)
    @MCRCacheControl(noStore = true)
    public MCRDownloadUrlDto getDownloadUrl(@PathParam(PARAM_DERID) MCRObjectID derivateId,
        @PathParam(PARAM_DER_PATH) String path) {
        ensureObjectExists();
        ensureStoreExists(derivateId);
        ensureDerivateReadPermission(derivateId);
        final String relativePath = toRelativePath(path);
        final MCRExternalStoreFileInfo fileInfo = findFileInfo(derivateId, relativePath);
        return new MCRDownloadUrlDto(getCheckedDownloadUrl(derivateId, relativePath, fileInfo));
    }

    /**
     * Download urls are short-lived bearer tokens and must not be cached.
     * <p>
     * Set by hand because {@link MCRCacheControl} is not applied to redirects.
     *
     * @return no-store cache control
     */
    private static CacheControl noStore() {
        final CacheControl cacheControl = new CacheControl();
        cacheControl.setNoStore(true);
        return cacheControl;
    }

    private String getCheckedDownloadUrl(MCRObjectID derivateId, String path, MCRExternalStoreFileInfo fileInfo) {
        ensureFileIsDownloadable(fileInfo);
        ensureAllowedFileSize(fileInfo);
        ensureFileIntegrity(derivateId, fileInfo);
        return createDownloadUrl(derivateId, path);
    }

    private static String toRelativePath(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }

    /**
     * Checks if the client prefers JSON over other media types, e.g. a browser or {@code curl} does not.
     *
     * @return true if JSON is the most acceptable media type
     */
    private boolean isJsonRequested() {
        final MediaType preferred = httpHeaders.getAcceptableMediaTypes().get(0);
        return !preferred.isWildcardType() && preferred.isCompatible(MediaType.APPLICATION_JSON_TYPE);
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
        try {
            MCRExternalStoreService.getInstance().ensureFileIntegrity(derivateId, fileInfo);
        } catch (MCRExternalStoreIntegrityViolationException e) {
            throw error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Detected integrity violation", e);
        } catch (MCRExternalStoreUnavailableException e) {
            throw error(Response.Status.BAD_GATEWAY.getStatusCode(), "Store is not available", e);
        }
    }

    private String createDownloadUrl(MCRObjectID derivateId, String path) {
        final MCRExternalStore store = MCRExternalStoreService.getInstance().getStore(derivateId);
        final URL downloadUrl = store.getStoreProvider()
            .getDownloadUrl(path, MCRExternalStoreConstants.DOWNLOAD_URL_VALIDITY);
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

    private static void ensureValidPaging(int offset, int limit) {
        if (offset < 0 || limit < 0) {
            throw error(Response.Status.BAD_REQUEST.getStatusCode(), "Offset and limit must not be negative");
        }
    }

    private void ensureObjectExists() {
        if (!MCRMetadataManager.exists(objectId)) {
            throw error(Response.Status.NOT_FOUND.getStatusCode(), objectId + " does not exist");
        }
    }

    /**
     * Ensures that the derivate belongs to the object and is a store.
     * <p>
     * The relation is checked via the link table, so the object does not have to be loaded.
     *
     * @param derivateId derivate id
     * @throws WebApplicationException with status 404 if the derivate is not a store of the object
     */
    private void ensureStoreExists(MCRObjectID derivateId) {
        MCRRestDerivates.validateDerivateRelation(objectId, derivateId);
        if (!isStoreDerivate(derivateId)) {
            throw error(Response.Status.NOT_FOUND.getStatusCode(),
                derivateId + " is not a store of " + objectId);
        }
    }

    private static boolean isStoreDerivate(MCRObjectID derivateId) {
        // the relation lookup is cached, so the derivate may have been deleted in the meantime
        return MCRMetadataManager.exists(derivateId)
            && MCRMetadataManager.retrieveMCRDerivate(derivateId).getDerivate().getClassifications().stream()
                .anyMatch(MCRExternalStoreServiceUtils::checkExternalStoreClassification);
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
        return MCRErrorResponse.ofStatusCode(status).withMessage(message).toException();
    }

    private static WebApplicationException error(int status, String message, Throwable cause) {
        final MCRErrorResponse response = MCRErrorResponse.ofStatusCode(status).withMessage(message).withCause(cause);
        // expose the cause only for client errors, server errors are logged
        if (status < Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
            response.withDetail(cause.getMessage());
        }
        return response.toException();
    }
}
