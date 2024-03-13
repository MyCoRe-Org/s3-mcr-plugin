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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;

/**
 * Provides general utility methods.
 */
public class MCRExternalStoreUtils {

    /**
     * Returns path of the parent of given path.
     *
     * @param path path
     * @return path of the parent
     */
    public static String getParentPath(String path) {
        final String fixedPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        return FilenameUtils.getPathNoEndSeparator(fixedPath);
    }

    /**
     * Returns file name of given path.
     *
     * @param path path
     * @return file name
     */
    public static String getFileName(String path) {
        return FilenameUtils.getName(path);
    }

    /**
     * Returns concatenation of two paths.
     *
     * @param pathOne first path
     * @param pathTwo seconds path
     * @return path
     */
    public static String concatPaths(String pathOne, String pathTwo) {
        if (!pathOne.isEmpty() && !pathTwo.isEmpty()) {
            return pathOne.concat("/").concat(pathTwo);
        } else if (!pathTwo.isEmpty()) {
            return pathTwo;
        }
        return pathOne;
    }

    private static MCRExternalStoreFileInfo createDirectory(String path) {
        return buildBaseFile(path).directory(true).build();
    }

    /**
     * Returns a list of parents for path.
     *
     * @param path path
     * @return the list
     */
    public static List<String> getPaths(String path) {
        return Optional.of(path).filter(p -> !p.isEmpty()).map(p -> p.split("/")).map(List::of)
            .orElseGet(() -> Collections.emptyList());
    }

    /**
     * Returns a list of {@link MCRExternalStoreFileInfo} elements with parent directories.
     *
     * @param path path
     * @return list
     */
    public static List<MCRExternalStoreFileInfo> getDirectories(String path) {
        if (path.isEmpty()) {
            return Collections.emptyList();
        }
        final List<String> paths = getPaths(path);
        final List<MCRExternalStoreFileInfo> result = new ArrayList<MCRExternalStoreFileInfo>();
        String currentPath = paths.get(0);
        result.add(createDirectory(currentPath));
        for (int i = 1; i < paths.size(); i++) {
            currentPath = currentPath + "/" + paths.get(i);
            result.add(createDirectory(currentPath));
        }
        return result;
    }

    private static MCRExternalStoreFileInfo.Builder buildBaseFile(String path) {
        return new MCRExternalStoreFileInfo.Builder(getFileName(path), getParentPath(path));
    }

}
