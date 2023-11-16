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

package org.mycore.externalstore.archive;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.mycore.externalstore.exception.MCRExternalStoreException;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;

/**
 * A {@link MCRExternalStoreArchiveResolver} for zip file.
 */
public class MCRExternalStoreZipArchiveResolver extends MCRExternalStoreArchiveResolver {

    @Override
    public List<MCRExternalStoreFileInfo> listFileInfos() throws IOException {
        final List<ArchiveEntry> list = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(getContent().getSeekableByteChannel())) {
            zipFile.getEntries().asIterator().forEachRemaining(list::add);
        }
        return list.stream().map(MCRExternalStoreArchiveUtils::mapToFileInfo).toList();
    }

    @Override
    public InputStream getInputStream(String path) throws IOException {
        try (ZipFile zipFile = new ZipFile(getContent().getSeekableByteChannel())) {
            final ZipArchiveEntry entry = zipFile.getEntry(path);
            if (entry != null) {
                return new ByteArrayInputStream(zipFile.getInputStream(entry).readAllBytes());
            } else {
                throw new MCRExternalStoreException("Path does not exist.");
            }
        }
    }
}
