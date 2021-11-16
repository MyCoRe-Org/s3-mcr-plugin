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

import java.util.Map;
import java.util.Optional;

import javax.naming.AuthenticationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRCrypt;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.crypt.MCRCipher;
import org.mycore.crypt.MCRCipherManager;
import org.mycore.crypt.MCRCryptKeyFileNotFoundException;
import org.mycore.crypt.MCRCryptKeyNoPermissionException;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;

public class FileSystemToXMLProvider {

    public static Response addFileSystem(String id, String impl, Map<String, String> settings)
        throws AuthenticationException {
        FileSystemToXML fs = MCRConfiguration2.<FileSystemToXML>getInstanceOf("MCR.FS.Impl." + impl).orElseThrow();
        String keyName = MCRConfiguration2.getStringOrThrow("MCR.FS.Impl." + impl + ".Key");

        try {
            Element bindChildElement = fs.getElement(settings);
            MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(id));

            MCRMODSWrapper mcrmodsWrapper = new MCRMODSWrapper(mcrObject);

            Element extensionElement = new Element("extension", MCRConstants.MODS_NAMESPACE);
            mcrmodsWrapper.getMODS().addContent(extensionElement);
            Element folderExtensionBind = new Element("folder-extension-bind");
            folderExtensionBind.setAttribute("class", fs.getClass().getName());
            folderExtensionBind.setAttribute("encrypted", "true");
            folderExtensionBind.setAttribute("key", keyName);
            extensionElement.addContent(folderExtensionBind);


            String febString = new XMLOutputter().outputString(bindChildElement);
            MCRCipher s3 = MCRCipherManager.getCipher(keyName);
            String encrypt = s3.encrypt(febString);
            folderExtensionBind.setText(encrypt);

            MCRMetadataManager.update(mcrObject);
            return Response.ok().header("Access-Control-Allow-Origin", "*").build();
        } catch (JAXBException|MCRCryptKeyFileNotFoundException e) {
            return Response.serverError().entity(e.getMessage()).build();
        } catch (MCRAccessException | MCRCryptKeyNoPermissionException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }
    }

}
