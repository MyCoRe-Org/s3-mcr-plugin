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
import java.nio.channels.SeekableByteChannel;
import java.util.List;
import java.util.Objects;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.mycore.externalstore.exception.MCRExternalStoreException;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;

/**
 * An {@link MCRExternalStoreArchiveResolver} implementation for tar file.
 */
public class MCRExternalStoreTarArchiveResolver extends MCRExternalStoreArchiveResolver {

    @Override
    public List<MCRExternalStoreFileInfo> listFileInfos() throws IOException {
        return listFileInfos(getContent().getSeekableByteChannel());
    }

    /**
     * Maps and returns tar file entries to list over {@link MCRExternalStoreFileInfo} elements.
     *
     * @param channel channel
     * @return list over file info elements
     * @throws IOException if an I/O error occurs
     */
    protected List<MCRExternalStoreFileInfo> listFileInfos(SeekableByteChannel channel) throws IOException {
        try (TarFile tarFile = new TarFile(channel)) {
            return tarFile.getEntries().stream().map(MCRExternalStoreArchiveUtils::mapToFileInfo).toList();
        }
    }

    @Override
    public InputStream getInputStream(String path) throws IOException {
        return getInputStream(getContent().getSeekableByteChannel(), path);
    }

    /**
     * Creates and returns input stream for path.
     *
     * @param channel channel
     * @param path path
     * @return input stream
     * @throws IOException if an I/O error occurs
     */
    protected InputStream getInputStream(SeekableByteChannel channel, String path) throws IOException {
        try (TarFile tarFile = new TarFile(channel)) {
            final TarArchiveEntry entry = tarFile.getEntries().stream().filter(t -> Objects.equals(path, t.getName()))
                .findAny()
                .orElseThrow(() -> new MCRExternalStoreException("Path does not exist"));
            return new ByteArrayInputStream(tarFile.getInputStream(entry).readAllBytes());
        }
    }

}
