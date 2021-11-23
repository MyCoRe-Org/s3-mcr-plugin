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

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class ZipDirectoryResolver extends CompressedDirectoryResolver<ZipArchiveInputStream, ZipArchiveEntry> {
    @Override
    ZipArchiveInputStream getCompressedStream(InputStream compressedStream, String pathToTar, String path) throws IOException {
        return new ZipArchiveInputStream(compressedStream);
    }

    @Override
    ZipArchiveEntry resolveNextEntry(ZipArchiveInputStream ais) throws IOException {
        return ais.getNextZipEntry();
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
