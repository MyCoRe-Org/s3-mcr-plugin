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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;
import org.mycore.externalstore.util.MCRExternalStoreUtils;

/**
 * Provides archive utility methods.
 */
public class MCRExternalStoreArchiveUtils {

    /**
     * Maps and returns an {@link MCRExternalStoreFileInfo} from {@link ArchiveEntry}.
     *
     * @param entry archive entry
     * @return file info
     */
    public static MCRExternalStoreFileInfo mapToFileInfo(ArchiveEntry entry) {
        MCRExternalStoreFileInfo file;
        if (entry.isDirectory()) {
            final String path = entry.getName().substring(0, entry.getName().length() - 1);
            file = MCRExternalStoreUtils.createDirectory(path);
        } else {
            file = MCRExternalStoreUtils.createBaseFile(entry.getName());
            file.setLastModified(entry.getLastModifiedDate());
            file.setSize(entry.getSize());
        }
        return file;
    }

    /**
     * Returns a list of {@link ArchiveEntry} elements of a {@link TarArchiveInputStream} specified by path.
     *
     * @param tarArchiveInputStream input stream
     * @param path path
     * @return list of archive entries
     * @throws IOException if an I/O error occurs
     */
    public static List<ArchiveEntry> getEntries(TarArchiveInputStream tarArchiveInputStream, String path)
        throws IOException {
        final List<ArchiveEntry> entries = new ArrayList<>();
        ArchiveEntry entry;
        while ((entry = tarArchiveInputStream.getNextEntry()) != null) {
            final String entryName = entry.getName();
            final String parentPath = (entry.isDirectory())
                ? MCRExternalStoreUtils.getParentPath(entryName.substring(0, entryName.length() - 1))
                : MCRExternalStoreUtils.getParentPath(entryName);
            if (Objects.equals(path, parentPath)) {
                entries.add(entry);
            }
        }
        return entries;
    }

}
