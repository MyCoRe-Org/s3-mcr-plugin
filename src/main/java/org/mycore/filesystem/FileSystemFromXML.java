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
import org.mycore.filesystem.model.Directory;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public interface FileSystemFromXML {
    void streamFile(Element extensionGrandChild, String path, OutputStream os) throws IOException;
    Directory getRootDirectory(Element extensionGrandChild) throws IOException;
    Directory getDirectory(Element extensionGrandChild, String path) throws IOException;
    boolean test(Element extensionGrandChild) throws AuthenticationException, IOException;
    Map<String, Object> getMetadata(Element element, boolean canWrite);
}
