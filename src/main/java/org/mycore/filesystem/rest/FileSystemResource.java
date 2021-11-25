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

package org.mycore.filesystem.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.naming.AuthenticationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.mycore.access.MCRAccessManager;
import org.mycore.crypt.MCRCryptKeyNoPermissionException;
import org.mycore.datamodel.metadata.MCRMetaLink;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.filesystem.FileSystemFromXMLHelper;
import org.mycore.filesystem.FileSystemToXMLHelper;
import org.mycore.restapi.annotations.MCRRequireTransaction;

@Path("fs/")
public class FileSystemResource {

    private static final Logger LOGGER = LogManager.getLogger();

    @POST
    @Path("{objectID}/add/{impl}/")
    @Consumes(MediaType.APPLICATION_JSON)
    @MCRRequireTransaction
    public Response add(@PathParam("objectID") String objectIDString, @PathParam("impl") String impl,
        Map<String, String> settings) {
        try {
            if (!MCRAccessManager.checkPermission(objectIDString, MCRAccessManager.PERMISSION_WRITE)) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
            return FileSystemToXMLHelper.addFileSystem(objectIDString, impl, settings);
        } catch (AuthenticationException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOGGER.error("Error ", e);
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("{objectID}/info")
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRequireTransaction
    public Response listInfo(@PathParam("objectID") String objectIDString) {
        if (!MCRAccessManager.checkPermission(objectIDString, MCRAccessManager.PERMISSION_READ)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        MCRObject obj = getObject(objectIDString);
        List<String> infoList = getExtensionDerivates(obj);
        return Response.ok(infoList).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("{objectID}/download/{derivateID}/{filePath}")
    @Produces("*/*")
    @MCRRequireTransaction
    public Response getFile(@PathParam("objectID") String objectIDString,
        @PathParam("derivateID") String base64DerivateID,
        @PathParam("filePath") String base64FilePath) {
        if (!MCRAccessManager.checkPermission(objectIDString, MCRAccessManager.PERMISSION_READ)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        String derivateID = new String(Base64.getUrlDecoder().decode(base64DerivateID), StandardCharsets.UTF_8);
        String filePath = new String(Base64.getUrlDecoder().decode(base64FilePath), StandardCharsets.UTF_8);

        final List<String> extensionDerivates = getExtensionDerivates(getObject(objectIDString));
        if (!extensionDerivates.contains(derivateID)) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(derivateID + " is not a extension derivate of  " + objectIDString).build();
        }

        final Element extension;
        try {
            extension = readExtensionFromDerivate(derivateID);
        } catch (IOException | JDOMException e) {
            LOGGER.error("Error while reading extension.xml from derivate", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error while reading extension.xml from derivate").build();
        }

        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        return Response.ok(new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                FileSystemFromXMLHelper.streamFile(extension, filePath, outputStream);
            }
        })
            .header("Content-Disposition", "attachment; filename=" + fileName)
            .header("Access-Control-Allow-Origin", "*").build();

    }

    @GET
    @Path("{objectID}/list/{derivateID}")
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRequireTransaction
    public Response listRoot(@PathParam("objectID") String objectIDString,
        @PathParam("derivateID") String base64DerivateID) {
        if (!MCRAccessManager.checkPermission(objectIDString, MCRAccessManager.PERMISSION_READ)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        String derivateID = new String(Base64.getUrlDecoder().decode(base64DerivateID), StandardCharsets.UTF_8);

        final List<String> extensionDerivates = getExtensionDerivates(getObject(objectIDString));
        if (!extensionDerivates.contains(derivateID)) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(derivateID + " is not a extension derivate of  " + objectIDString).build();
        }

        final Element extension;
        try {
            extension = readExtensionFromDerivate(derivateID);
        } catch (IOException | JDOMException e) {
            LOGGER.error("Error while reading extension.xml from derivate", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error while reading extension.xml from derivate").build();
        }

        try {
            return Response.ok(FileSystemFromXMLHelper.getDirectory(extension))
                .header("Access-Control-Allow-Origin", "*")
                .build();
        } catch (MCRCryptKeyNoPermissionException mcrCryptKeyNoPermissionException) {
            return Response.status(Response.Status.FORBIDDEN).entity(mcrCryptKeyNoPermissionException.getMessage())
                .build();
        } catch (IOException | JDOMException ioException) {
            return Response.serverError().entity(ioException.getMessage()).build();
        }

    }

    @GET
    @Path("{objectID}/list/{derivateID}/{directoryID}")
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRequireTransaction
    public Response listRoot(@PathParam("objectID") String objectIDString, @PathParam("derivateID") String base64DerivateID,
        @PathParam("directoryID") String base64DirectoryID) {
        if (!MCRAccessManager.checkPermission(objectIDString, MCRAccessManager.PERMISSION_READ)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        String derivateID = new String(Base64.getUrlDecoder().decode(base64DerivateID), StandardCharsets.UTF_8);
        String directoryID = new String(Base64.getUrlDecoder().decode(base64DirectoryID), StandardCharsets.UTF_8);

        final List<String> extensionDerivates = getExtensionDerivates(getObject(objectIDString));
        if (!extensionDerivates.contains(derivateID)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(derivateID + " is not a extension derivate of  " + objectIDString).build();
        }

        final Element extension;
        try {
            extension = readExtensionFromDerivate(derivateID);
        } catch (IOException | JDOMException e) {
            LOGGER.error("Error while reading extension.xml from derivate", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error while reading extension.xml from derivate").build();
        }

        try {
            return Response.ok(FileSystemFromXMLHelper.getDirectory(extension, directoryID))
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        } catch (MCRCryptKeyNoPermissionException mcrCryptKeyNoPermissionException) {
            return Response.status(Response.Status.FORBIDDEN).entity(mcrCryptKeyNoPermissionException.getMessage())
                    .build();
        } catch (IOException | JDOMException ioException) {
            return Response.serverError().entity(ioException.getMessage()).build();
        }
    }

    private List<String> getExtensionDerivates(MCRObject obj) {
        return obj.getStructure().getDerivates()
            .stream()
            .filter(der -> der.getClassifications().stream()
                .anyMatch(clazz -> clazz.toString().equals("derivate_types:extension")))
            .map(MCRMetaLink::getXLinkHref)
            .collect(Collectors.toList());
    }

    private Element readExtensionFromDerivate(String derivateID) throws IOException, JDOMException {
        final MCRPath path = MCRPath.getPath(derivateID, "extension.xml");
        try (InputStream is = Files.newInputStream(path)) {
            final Document doc = new SAXBuilder().build(is);
            return doc.detachRootElement();
        }
    }

    private MCRObject getObject(String objectIDString) {
        MCRObjectID objectID = MCRObjectID.getInstance(objectIDString);
        MCRObject object = MCRMetadataManager.retrieveMCRObject(objectID);
        return object;
    }
}
