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

import org.junit.Assert;
import org.junit.Test;
import org.mycore.filesystem.model.Directory;

import java.io.IOException;

public class TarDirectoryResolverTest  {

    @Test
    public void test() throws IOException {
        TarDirectoryResolver tarDirectoryResolver = new TarDirectoryResolver();

        Directory subDirectory = tarDirectoryResolver.resolveDirectory(getClass().getClassLoader().getResourceAsStream("tar/test.tar.gz"), "tar/test.tar.gz", "sub/");

        Assert.assertEquals(subDirectory.getChildren().size(), 2);
        Assert.assertTrue(subDirectory.getChildren().stream().anyMatch(e -> e.getName().equals("test3.txt")));
        Assert.assertTrue(subDirectory.getChildren().stream().anyMatch(e -> e.getName().equals("sub2/")));

        Directory sub2Directory = tarDirectoryResolver.resolveDirectory(getClass().getClassLoader().getResourceAsStream("tar/test.tar.gz"), "tar/test.tar.gz", "sub/sub2/");

        Assert.assertEquals(sub2Directory.getChildren().size(), 1);
        Assert.assertTrue(sub2Directory.getChildren().stream().anyMatch(e -> e.getName().equals("test4.txt")));

        Directory rootDirectory = tarDirectoryResolver.resolveDirectory(getClass().getClassLoader().getResourceAsStream("tar/test.tar.gz"), "tar/test.tar.gz", "");

        Assert.assertEquals(rootDirectory.getChildren().size(), 3);
        Assert.assertTrue(rootDirectory.getChildren().stream().anyMatch(e -> e.getName().equals("sub/")));
        Assert.assertTrue(rootDirectory.getChildren().stream().anyMatch(e -> e.getName().equals("test1.txt")));
        Assert.assertTrue(rootDirectory.getChildren().stream().anyMatch(e -> e.getName().equals("test2.txt")));
    }

}