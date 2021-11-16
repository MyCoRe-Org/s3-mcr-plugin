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

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.crypt.MCRCipher;
import org.mycore.crypt.MCRCipherManager;
import org.mycore.crypt.MCRCryptKeyNoPermissionException;
import org.mycore.filesystem.model.Directory;
import org.mycore.filesystem.model.RootInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.List;

/**
 *
 */
public class FileSystemFromXMLProvider {

    public static RootInfo getInfo(Element extensionElement)  {
        if (extensionElement.getAttributeValue("class") == null) {
            return null;
        }
        String clazz = extensionElement.getAttributeValue("class");

        FileSystemFromXML fsProvider = MCRConfiguration2.instantiateClass(clazz);
        Element element = null;
        try {
            element = processBindElement(extensionElement);
        } catch (IOException | JDOMException | MCRCryptKeyNoPermissionException e) {
            return null;
        }

        return fsProvider.getRootInfo(element);
    }

    public static Directory getDirectory(Element extensionElement) throws IOException, MCRCryptKeyNoPermissionException, JDOMException {
        String clazz = extensionElement.getAttributeValue("class");

        FileSystemFromXML fsProvider = MCRConfiguration2.instantiateClass(clazz);

         Element element = processBindElement(extensionElement);

        return fsProvider.getRootDirectory(element);
    }

    public static Directory getDirectory(Element extensionElement, String path) throws IOException, MCRCryptKeyNoPermissionException, JDOMException {
        String clazz = extensionElement.getAttributeValue("class");
        FileSystemFromXML fsProvider = MCRConfiguration2.instantiateClass(clazz);
        Element element = processBindElement(extensionElement);
        return  fsProvider.getDirectory(element, path);
    }

    private static Element processBindElement(Element extensionElement) throws IOException, MCRCryptKeyNoPermissionException, JDOMException {
        Element element;
        boolean encrypted = Boolean.parseBoolean(extensionElement.getAttributeValue("encrypted"));
        if(!encrypted){
            List<Element> children = extensionElement.getChildren();
            if (children.size() == 0) {
                throw new IOException("Data seems to be invalid, because no child present in extension element");
            }
            element =children.get(0);
        } else {
            element = decryptElement(extensionElement);
        }
        return element;
    }

    private static Element decryptElement(Element extensionElement) throws MCRCryptKeyNoPermissionException, JDOMException, IOException {
        String key = extensionElement.getAttributeValue("key");
        MCRCipher cipher = MCRCipherManager.getCipher(key);
        String decrypt = cipher.decrypt(extensionElement.getTextTrim());
        Element element = new SAXBuilder().build(new StringReader(decrypt)).detachRootElement();
        return element;
    }

    public static void streamFile(Element extensionElement, String path, OutputStream os) throws IOException {
        String clazz = extensionElement.getAttributeValue("class");

        FileSystemFromXML fsProvider = MCRConfiguration2.instantiateClass(clazz);

        List<Element> children = extensionElement.getChildren();
        if (children.size() == 0) {
            throw new IOException("Data seems to be invalid, because no child present in extension element");
        }
        fsProvider.streamFile(children.get(0), path, os);
    }

}