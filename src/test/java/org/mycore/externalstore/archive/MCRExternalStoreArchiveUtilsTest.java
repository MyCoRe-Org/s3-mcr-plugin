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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.junit.Test;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;

public class MCRExternalStoreArchiveUtilsTest {

    @Test
    public void testMapToFileInfo() throws IOException {
        final List<ArchiveEntry> entries = generateTarEntries();
        final List<MCRExternalStoreFileInfo> fileInfos
            = entries.stream().map(MCRExternalStoreArchiveUtils::mapToFileInfo).toList();
        assertEquals(3, fileInfos.size());
        assertTrue(fileInfos.stream().filter(MCRExternalStoreFileInfo::isDirectory)
            .filter(f -> Objects.equals("bar", f.getName())).findAny().isPresent());
        assertTrue(fileInfos.stream().filter(f -> !f.isDirectory()).filter(f -> Objects.equals("foo", f.getName()))
            .findAny().isPresent());
        assertTrue(fileInfos.stream().filter(f -> !f.isDirectory()).filter(f -> Objects.equals("foo.txt", f.getName()))
            .findAny().isPresent());
    }

    @Test
    public void testGetTarEntriesRoot() throws IOException {
        final List<ArchiveEntry> entries = MCRExternalStoreArchiveUtils.getEntries(getTarTestFile(), "");
        assertEquals(3, entries.size());
        assertTrue(entries.stream().anyMatch(e -> Objects.equals("test1.txt", e.getName())));
        assertTrue(entries.stream().anyMatch(e -> Objects.equals("test2.txt", e.getName())));
        assertTrue(entries.stream().anyMatch(e -> Objects.equals("sub/", e.getName())));
    }

    @Test
    public void testGetTarEntriesNestedChild() throws IOException {
        final List<ArchiveEntry> entries = MCRExternalStoreArchiveUtils.getEntries(getTarTestFile(), "sub");
        assertEquals(2, entries.size());
        assertTrue(entries.stream().anyMatch(e -> Objects.equals("sub/sub2/", e.getName())));
        assertTrue(entries.stream().anyMatch(e -> Objects.equals("sub/test3.txt", e.getName())));
    }

    private List<ArchiveEntry> generateTarEntries() {
        final List<ArchiveEntry> entries = new ArrayList<>();
        entries.add(new TarArchiveEntry("foo"));
        entries.add(new TarArchiveEntry("foo.txt"));
        entries.add(new TarArchiveEntry("bar/"));
        return entries;
    }

    private static TarArchiveInputStream getTarTestFile() throws IOException {
        return new TarArchiveInputStream(
            new GzipCompressorInputStream(
                MCRExternalStoreArchiveUtilsTest.class.getClassLoader().getResourceAsStream("tar/test.tar.gz")));
    }

}
