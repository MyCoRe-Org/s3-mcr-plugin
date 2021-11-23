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
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.filesystem.model.Directory;
import org.mycore.filesystem.model.File;
import org.mycore.filesystem.model.FileBase;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class TarDirectoryResolver extends CompressedDirectoryResolver<TarArchiveInputStream, TarArchiveEntry> {


    @Override
    TarArchiveInputStream getCompressedStream(InputStream compressedStream, String pathToTar, String path) throws IOException {
        if (pathToTar.endsWith(".gz")) {
            return new TarArchiveInputStream(new GzipCompressorInputStream(compressedStream));
        } else {
            return new TarArchiveInputStream(compressedStream);
        }
    }

    @Override
    TarArchiveEntry resolveNextEntry(TarArchiveInputStream ais) throws IOException {
        return ais.getNextTarEntry();
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
