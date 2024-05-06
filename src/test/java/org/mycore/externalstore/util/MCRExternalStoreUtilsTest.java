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

package org.mycore.externalstore.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;

public class MCRExternalStoreUtilsTest {

    @Test
    public void testGetParentPath() {
        assertEquals("", MCRExternalStoreUtils.getParentPath(""));
        assertEquals("", MCRExternalStoreUtils.getParentPath("test.txt"));
        assertEquals("foo", MCRExternalStoreUtils.getParentPath("foo/test.txt"));
        assertEquals("foo", MCRExternalStoreUtils.getParentPath("foo/bar"));
        assertEquals("", MCRExternalStoreUtils.getParentPath("/bar"));
        assertEquals("", MCRExternalStoreUtils.getParentPath("bar"));
        assertEquals("bar", MCRExternalStoreUtils.getParentPath("/bar/foo"));
    }

    @Test
    public void testGetName() {
        assertEquals("", MCRExternalStoreUtils.getFileName(""));
        assertEquals("test.txt", MCRExternalStoreUtils.getFileName("test.txt"));
        assertEquals("test.txt", MCRExternalStoreUtils.getFileName("/test.txt"));
        assertEquals("bar", MCRExternalStoreUtils.getFileName("bar"));
        assertEquals("bar", MCRExternalStoreUtils.getFileName("bar"));
        assertEquals("bar", MCRExternalStoreUtils.getFileName("/bar"));
        assertEquals("bar", MCRExternalStoreUtils.getFileName("/bar"));
        assertEquals("foo", MCRExternalStoreUtils.getFileName("/bar/foo"));
        assertEquals("test.txt", MCRExternalStoreUtils.getFileName("/bar/test.txt"));
    }

    @Test
    public void testGetParents() {
        assertEquals(0, MCRExternalStoreUtils.getPaths("").size());
        assertEquals(1, MCRExternalStoreUtils.getPaths("bla").size());
        assertEquals(2, MCRExternalStoreUtils.getPaths("bla/foo").size());
        assertEquals(3, MCRExternalStoreUtils.getPaths("bla/foo/bar").size());
    }

    @Test
    public void testGetParentDirectories() {
        assertEquals(0, MCRExternalStoreUtils.getDirectories("").size());
        List<MCRExternalStoreFileInfo> files = MCRExternalStoreUtils.getDirectories("bla");
        assertEquals(1, files.size());
        assertEquals("", files.get(0).parentPath());
        assertEquals("bla", files.get(0).name());
        assertTrue(files.get(0).isDirectory());
        files = MCRExternalStoreUtils.getDirectories("bla/foo");
        assertEquals(2, files.size());
        assertEquals("bla", files.get(0).name());
        assertEquals("", files.get(0).parentPath());
        assertTrue(files.get(0).isDirectory());
        assertEquals("foo", files.get(1).name());
        assertEquals("bla", files.get(1).parentPath());
        assertTrue(files.get(1).isDirectory());
    }

    @Test
    public void testGetPaths() {
        assertEquals(List.of(), MCRExternalStoreUtils.getPaths(""));
        assertEquals(List.of("foo"), MCRExternalStoreUtils.getPaths("foo"));
        assertEquals(List.of("foo", "bar"), MCRExternalStoreUtils.getPaths("foo/bar"));
    }

    @Test
    public void testConcatPaths() {
        assertEquals("bar", MCRExternalStoreUtils.concatPaths("", "bar"));
        assertEquals("bar", MCRExternalStoreUtils.concatPaths("bar", ""));
        assertEquals("foo/bar", MCRExternalStoreUtils.concatPaths("foo", "bar"));
    }

}
