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
import java.util.Date;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

public class ZipFileDirectoryResolver extends CompressedDirectoryResolver<ZipFile, ZipArchiveEntry, S3SeekableFileChannel> {

    Enumeration<ZipArchiveEntry> entries;

    @Override
    ZipFile getCompressedStream(S3SeekableFileChannel compressedStream, String pathToTar, String path) throws IOException {
        return  new ZipFile(compressedStream, path, "UTF8", false);
    }

    @Override
    ZipArchiveEntry resolveNextEntry(ZipFile ais) throws IOException {
        if(entries==null){
            entries = ais.getEntries();
        }
        if(!entries.hasMoreElements()){
            return null;
        }
        return entries.nextElement();
    }

    @Override
    String getFileName(ZipArchiveEntry entry) {
        return entry.getName();
    }

    @Override
    Date getLastModifiedDate(ZipArchiveEntry entry) {
        return entry.getLastModifiedDate();
    }

    @Override
    int getSize(ZipArchiveEntry entry) {
        return (int) entry.getSize();
    }
}
