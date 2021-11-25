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

package org.mycore.filesystem;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.naming.AuthenticationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.mycore.access.MCRAccessException;
import org.mycore.access.MCRAccessManager;
import org.mycore.access.MCRRuleAccessInterface;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.crypt.MCRCipher;
import org.mycore.crypt.MCRCipherManager;
import org.mycore.crypt.MCRCryptKeyNoPermissionException;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaClassification;
import org.mycore.datamodel.metadata.MCRMetaIFS;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;

public class FileSystemToXMLHelper {

    public static Response addFileSystem(String id, String impl, Map<String, String> settings)
        throws AuthenticationException {
        FileSystemToXML fs = MCRConfiguration2.<FileSystemToXML>getInstanceOf("MCR.FS.Impl." + impl).orElseThrow();
        String keyName = MCRConfiguration2.getStringOrThrow("MCR.FS.Impl." + impl + ".Key");

        try {
            Element bindChildElement = fs.getElement(settings);

            MCRObjectID objectID = MCRObjectID.getInstance(id);
            //MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(objectID);

            MCRDerivate derivate = createDerivate(objectID, Stream.of(new MCRMetaClassification("classification", 0, null, "derivate_types", "extension")).collect(Collectors.toList()));

            Element fileSystemExtension = new Element("extension");
            fileSystemExtension.setAttribute("class", fs.getClass().getName());
            fileSystemExtension.setAttribute("encrypted", "true");
            fileSystemExtension.setAttribute("key", keyName);

            String febString = new XMLOutputter().outputString(bindChildElement);
            MCRCipher s3 = MCRCipherManager.getCipher(keyName);
            String encrypt = s3.encrypt(febString);
            fileSystemExtension.setText(encrypt);

            Document extensionFile = new Document(fileSystemExtension);

            MCRPath extensionXML = MCRPath.getPath(derivate.getId().toString(), "extension.xml");

            try (OutputStream outputStream = Files.newOutputStream(extensionXML)) {
                new MCRJDOMContent(extensionFile).sendTo(outputStream, true);
            }
            return Response.ok().header("Access-Control-Allow-Origin", "*").build();
        } catch (JAXBException | IOException e) {
            return Response.serverError().entity(e.getMessage()).build();
        } catch (MCRAccessException | MCRCryptKeyNoPermissionException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }
    }

    private static MCRObjectID getNewCreateDerivateID(MCRObjectID objId) {
        String projectID = objId.getProjectId();
        return MCRObjectID.getNextFreeId(projectID + "_derivate");
    }

    private static MCRDerivate createDerivate(MCRObjectID objectID, List<MCRMetaClassification> classifications)
            throws MCRPersistenceException, MCRAccessException {

        MCRObjectID derivateID = getNewCreateDerivateID(objectID);
        MCRDerivate derivate = new MCRDerivate();
        derivate.setId(derivateID);
        derivate.getDerivate().getClassifications().addAll(classifications);

        String schema = MCRConfiguration2.getString("MCR.Metadata.Config.derivate").orElse("datamodel-derivate.xml")
                .replaceAll(".xml", ".xsd");
        derivate.setSchema(schema);

        MCRMetaLinkID linkId = new MCRMetaLinkID();
        linkId.setSubTag("linkmeta");
        linkId.setReference(objectID, null, null);
        derivate.getDerivate().setLinkMeta(linkId);

        MCRMetaIFS ifs = new MCRMetaIFS();
        ifs.setSubTag("internal");
        ifs.setSourcePath(null);
        derivate.getDerivate().setInternals(ifs);


        MCRMetadataManager.create(derivate);

        setDefaultPermissions(derivateID);

        return derivate;
    }

    private static void setDefaultPermissions(MCRObjectID derivateID) {
        if (MCRConfiguration2.getBoolean("MCR.Access.AddDerivateDefaultRule").orElse(true)) {
            MCRRuleAccessInterface aclImpl = MCRAccessManager.getAccessImpl();
            Collection<String> configuredPermissions = aclImpl.getAccessPermissionsFromConfiguration();
            for (String permission : configuredPermissions) {
                MCRAccessManager.addRule(derivateID, permission, MCRAccessManager.getTrueRule(),
                        "default derivate rule");
            }
        }
    }
}
