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

package org.mycore.filesystem.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.filesystem.model.Directory;
import org.mycore.filesystem.model.File;
import org.mycore.filesystem.model.FileBase;

public abstract class CompressedDirectoryResolver<T1, T2, T3> {

    private static final Logger LOGGER = LogManager.getLogger();

    public Directory resolveDirectory(T3 compressedStream, String pathToTar, String path) throws IOException {
        Objects.requireNonNull(compressedStream, "The stream needs to be not null!");

        T1 archiveInputStream = getCompressedStream(compressedStream, pathToTar, path);
        return resolveIntern(archiveInputStream, pathToTar, path);
    }

    abstract T1 getCompressedStream(T3 compressedStream, String pathToTar, String path) throws IOException;

    abstract T2 resolveNextEntry(T1 ais) throws IOException;

    abstract String getFileName(T2 entry);
    abstract Date getLastModifiedDate(T2 entry);
    abstract int getSize(T2 entry);

    private Directory resolveIntern(T1 ais, String pathToTar, String parentStr) throws IOException {
        T2 entry;
        // find a matching entry
        if (parentStr == null) {
            parentStr = "";
        }

        Directory root = new Directory();
        root.setChildren(new ArrayList<>());
        root.setPath(pathToTar + "/" + parentStr);

        final LazyFSBuilder lazyFSBuilder = new LazyFSBuilder(pathToTar+"/");
        while ((entry = resolveNextEntry(ais)) != null) {
            String filePathStr = getFileName(entry);

            if(filePathStr.endsWith("/")){
                lazyFSBuilder.addDirectory(filePathStr);
            } else {
                final int size = getSize(entry);
                final Date lastModifiedDate = getLastModifiedDate(entry);
                lazyFSBuilder.add(filePathStr,size, lastModifiedDate);
            }
        }

        return lazyFSBuilder.getDirectory(parentStr);
    }

}
