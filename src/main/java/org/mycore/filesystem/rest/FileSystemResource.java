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
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.naming.AuthenticationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.crypt.MCRCryptKeyNoPermissionException;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.filesystem.FileSystemFromXMLProvider;
import org.mycore.filesystem.FileSystemToXMLProvider;
import org.mycore.filesystem.model.Directory;
import org.mycore.filesystem.model.RootInfo;
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
            return FileSystemToXMLProvider.addFileSystem(objectIDString, impl, settings);
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
        Document document = getDocument(objectIDString);
        List<Element> results = getExtensionList(document);

        // TODO: decrypt here later

        List<RootInfo> infoList = results.stream().map(FileSystemFromXMLProvider::getInfo)
                .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return Response.ok(infoList).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("{objectID}/download/{rootID}/{filePath}")
    @Produces("*/*")
    @MCRRequireTransaction
    public Response getFile(@PathParam("objectID") String objectIDString, @PathParam("rootID") String base64RootID,
        @PathParam("filePath") String base64FilePath) {
        Document document = getDocument(objectIDString);
        List<Element> results = getExtensionList(document);

        String rootID = new String(Base64.getUrlDecoder().decode(base64RootID), StandardCharsets.UTF_8);
        String filePath = new String(Base64.getUrlDecoder().decode(base64FilePath), StandardCharsets.UTF_8);

        Optional<StreamingOutput> streamingOutputOptional = results.stream().filter(e -> {
            RootInfo info = FileSystemFromXMLProvider.getInfo(e);
            if (info == null) {
                return false;
            }
            return Objects.equals(info.getId(), rootID);
        }).map(e -> (StreamingOutput) (outputStream) -> {
            FileSystemFromXMLProvider.streamFile(e, filePath, outputStream);
        }).findFirst();

        StreamingOutput streamingOutput = streamingOutputOptional.get();

        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        return Response.ok(streamingOutput)
            .header("Content-Disposition", "attachment; filename=" + fileName)
            .header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("{objectID}/list/{rootID}")
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRequireTransaction
    public Response listRoot(@PathParam("objectID") String objectIDString, @PathParam("rootID") String base64RootID) {
        Document document = getDocument(objectIDString);
        List<Element> results = getExtensionList(document);

        String rootID = new String(Base64.getUrlDecoder().decode(base64RootID), StandardCharsets.UTF_8);
        return results.stream().filter(e -> {
            RootInfo info = FileSystemFromXMLProvider.getInfo(e);
            if (info == null) {
                return false;
            }
            return Objects.equals(info.getId(), rootID);
        }).map(e -> {
            try {
                return Response.ok(FileSystemFromXMLProvider.getDirectory(e)).header("Access-Control-Allow-Origin", "*")
                    .build();
            } catch (MCRCryptKeyNoPermissionException mcrCryptKeyNoPermissionException) {
                return Response.status(Response.Status.FORBIDDEN).entity(mcrCryptKeyNoPermissionException.getMessage())
                    .build();
            } catch (IOException | JDOMException ioException) {
                return Response.serverError().entity(ioException.getMessage()).build();
            }
        })
            .findFirst().orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("{objectID}/list/{rootID}/{directoryID}")
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRequireTransaction
    public Response listRoot(@PathParam("objectID") String objectIDString, @PathParam("rootID") String base64RootID,
        @PathParam("directoryID") String base64DirectoryID) {
        Document document = getDocument(objectIDString);
        List<Element> results = getExtensionList(document);

        String rootID = new String(Base64.getUrlDecoder().decode(base64RootID), StandardCharsets.UTF_8);
        String directoryID = new String(Base64.getUrlDecoder().decode(base64DirectoryID), StandardCharsets.UTF_8);

        return results.stream().filter(e -> {
            RootInfo info = FileSystemFromXMLProvider.getInfo(e);
            if (info == null) {
                return false;
            }
            return Objects.equals(info.getId(), rootID);
        }).map(e -> {
            try {
                return Response.ok(FileSystemFromXMLProvider.getDirectory(e, directoryID))
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
            } catch (MCRCryptKeyNoPermissionException mcrCryptKeyNoPermissionException) {
                return Response.status(Response.Status.FORBIDDEN).entity(mcrCryptKeyNoPermissionException.getMessage())
                    .build();
            } catch (IOException | JDOMException ioException) {
                return Response.serverError().entity(ioException.getMessage()).build();
            }
        })
            .findFirst().orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    private List<Element> getExtensionList(Document document) {
        String xpath = "//folder-extension-bind[@class]";
        XPathExpression<Element> xPathExpression = XPathFactory.instance().compile(xpath, Filters.element());
        return xPathExpression.evaluate(document);
    }

    private Document getDocument(String objectIDString) {
        MCRObjectID objectID = MCRObjectID.getInstance(objectIDString);
        MCRObject object = MCRMetadataManager.retrieveMCRObject(objectID);
        return object.createXML();
    }
}
