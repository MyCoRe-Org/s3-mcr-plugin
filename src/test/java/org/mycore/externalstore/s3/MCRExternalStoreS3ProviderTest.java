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

package org.mycore.externalstore.s3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Date;

import org.junit.Test;
import org.mycore.common.MCRTestCase;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;

import com.amazonaws.services.s3.model.S3ObjectSummary;

public class MCRExternalStoreS3ProviderTest extends MCRTestCase {

    @Test
    public void testToFileInfo() {
        final S3ObjectSummary objectSummary = new S3ObjectSummary();
        objectSummary.setKey("foo/bar.txt");
        objectSummary.setETag("etag");
        objectSummary.setSize(10);
        final Date date = new Date();
        objectSummary.setLastModified(date);
        final MCRExternalStoreFileInfo fileSummary = new MCRExternalStoreS3Provider().toFileInfo(objectSummary);
        assertEquals("foo", fileSummary.parentPath());
        assertEquals("bar.txt", fileSummary.name());
        assertFalse(fileSummary.isDirectory());
        assertEquals("etag", fileSummary.checksum());
        assertEquals(10, fileSummary.size().longValue());
        assertEquals(date, fileSummary.lastModified());
    }

}
