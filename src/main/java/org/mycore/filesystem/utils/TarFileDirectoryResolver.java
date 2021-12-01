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

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.IOException;
import java.nio.channels.Channels;
import java.util.Date;
import java.util.Iterator;

public class TarFileDirectoryResolver extends CompressedDirectoryResolver<Object, TarArchiveEntry, S3SeekableFileChannel> {

    private Iterator<TarArchiveEntry> iterator;

    @Override
    Object getCompressedStream(S3SeekableFileChannel compressedStream, String pathToTar, String path) throws IOException {
        if(pathToTar.endsWith(".tar.gz")) {
            // i think this has trash performance
            return new TarArchiveInputStream(new GzipCompressorInputStream(Channels.newInputStream(compressedStream)));
        } else {
            return new TarFile(compressedStream);
        }
    }

    @Override
    TarArchiveEntry resolveNextEntry(Object ais) throws IOException {
        if(ais instanceof TarFile){
            if(iterator==null){
                iterator = ((TarFile)ais).getEntries().iterator();
            }
            return iterator.hasNext() ? iterator.next() : null;
        } else {
            TarArchiveInputStream tais = (TarArchiveInputStream) ais;
            return tais.getNextTarEntry();
        }
    }

    @Override
    String getFileName(TarArchiveEntry entry) {
        return entry.getName();
    }

    @Override
    Date getLastModifiedDate(TarArchiveEntry entry) {
        return entry.getLastModifiedDate();
    }

    @Override
    int getSize(TarArchiveEntry entry) {
        return (int) entry.getSize();
    }
}
