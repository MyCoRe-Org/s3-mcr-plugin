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

import org.apache.commons.io.output.NullOutputStream;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.common.MCRException;
import org.mycore.crypt.MCRCryptKeyNoPermissionException;
import org.mycore.filesystem.model.Directory;
import org.mycore.filesystem.model.FileBase;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class FileSystemValidationHelper {

    public static Map<String, String> getSimpleValidationMap(FileSystemFromXML impl, Element extension) throws IOException {
        HashMap<String, String> validation = new HashMap<>();
        Directory rootDirectory = impl.getRootDirectory(extension);
        getValidationMap(impl, extension, rootDirectory, validation, FileBase::getEtag);
        return validation;
    }

    public static HashMap<String, String> getComplexValidationMap(FileSystemFromXML impl, Element extension) throws IOException {
        HashMap<String, String> validation = new HashMap<>();
        Directory rootDirectory = impl.getRootDirectory(extension);
        getValidationMap(impl, extension, rootDirectory, validation, (fileBase -> {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                try(DigestOutputStream os = new DigestOutputStream(NullOutputStream.nullOutputStream(), digest)){
                    impl.streamFile(extension, fileBase.getPath(), os);
                    return Base64.getEncoder().encodeToString(os.getMessageDigest().digest());
                }
            } catch (NoSuchAlgorithmException e) {
                throw new MCRException("No SHA-256 present :(", e);
            } catch (IOException e) {
                throw new UncheckedIOException("Error while calculating SHA-256", e);
            }
        }));
        return validation;
    }

    private static void getValidationMap(FileSystemFromXML impl, Element extension, Directory dir, Map<String, String> validationMap, Function<FileBase,String> sumFunction) {
        dir.getChildren().forEach(child -> {
            switch (child.getType()){
                case FILE:
                case BROWSABLE_FILE:
                    validationMap.put(child.getPath(), sumFunction.apply(child));
                    break;
                case ROOT:
                case DIRECTORY:
                    try {
                        Directory directory = impl.getDirectory(extension, child.getPath());
                        getValidationMap(impl,extension, directory, validationMap, sumFunction);
                    } catch (IOException e) {
                        throw new MCRException("Error while running validation", e);
                    }
                    break;
            }
        });
    }

}
