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

package org.mycore.externalstore.index.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDateTime;
import java.util.Date;

import org.junit.Test;
import org.mycore.common.MCRTestCase;
import org.mycore.externalstore.index.db.model.MCRExternalStoreFileInfoData;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo.FileFlag;

public class MCRExternalStoreDbMapperTest extends MCRTestCase {

    @Test
    public void fileInfoDataToDomain() {
        final MCRExternalStoreFileInfoData fileInfoData = new MCRExternalStoreFileInfoData();
        fileInfoData.setDirectory(false);
        fileInfoData.setName("foo.txt");
        fileInfoData.setParentPath("bar");
        fileInfoData.setSize(1l);
        fileInfoData.setLastModified(LocalDateTime.now());
        fileInfoData.setChecksum("123");
        fileInfoData.getFlags().add(FileFlag.ARCHIVE.toString());
        final MCRExternalStoreFileInfo result = MCRExternalStoreDbMapper.toDomain(fileInfoData);
        assertFalse(result.isDirectory());
        assertEquals("foo.txt", result.name());
        assertEquals("bar", result.parentPath());
        assertEquals("123", result.checksum());
        assertEquals(Long.valueOf(1l), result.size());
        assertNotNull(result.flags());
        assertEquals(1, result.flags().size());
        assertNotNull(result.lastModified());
    }

    @Test
    public void fileInfoToDataTest() {
        final MCRExternalStoreFileInfo fileSummary
            = new MCRExternalStoreFileInfo.Builder("foo.txt", "bar").directory(false)
                .checksum("123").size(1l).lastModified(new Date()).build();
        final MCRExternalStoreFileInfoData result = MCRExternalStoreDbMapper.toData(fileSummary);
        assertFalse(result.isDirectory());
        assertEquals("foo.txt", result.getName());
        assertEquals("bar", result.getParentPath());
        assertEquals("123", result.getChecksum());
        assertEquals(Long.valueOf(1l), result.getSize());
        assertNotNull(result.getLastModified());
    }
}
