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
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo.MCRExternalStoreFileInfoBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

/**
 * Provides general utility methods.
 */
public class MCRExternalStoreUtils {

    /**
     * Extracts path of the parent of given path.
     *
     * @param path the path
     * @return the path of the parent
     */
    public static String getParentPath(String path) {
        final String fixedPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        return FilenameUtils.getPathNoEndSeparator(fixedPath);
    }

    /**
     * Extracts file name of of given path.
     *
     * @param path the path
     * @return the file name
     */
    public static String getFileName(String path) {
        return FilenameUtils.getName(path);
    }

    /**
     * Adds parent path to {@link MCRExternalStoreFileInfo} parent path.
     *
     * @param fileInfo the file info
     * @param path the path
     */
    public static void addParentPath(MCRExternalStoreFileInfo fileInfo, String path) {
        Optional.of(fileInfo).filter(f -> f.getParentPath().isEmpty()).ifPresentOrElse(f -> f.setParentPath(path),
            () -> fileInfo.setParentPath(path.concat("/").concat(fileInfo.getParentPath())));
    }

    /**
     * Concatenates two paths.
     *
     * @param pathOne the first path
     * @param pathTwo the seconds path
     * @return the result path
     */
    public static String concatPaths(String pathOne, String pathTwo) {
        if (!pathOne.isEmpty() && !pathTwo.isEmpty()) {
            return pathOne.concat("/").concat(pathTwo);
        } else if (!pathTwo.isEmpty()) {
            return pathTwo;
        }
        return pathOne;
    }

    /**
     * Creates file instance with path of parent and file name by given path.
     *
     * @param path the path
     * @return the file as {@link MCRExternalStoreFileInfo} instance
     */
    public static MCRExternalStoreFileInfo createBaseFile(String path) {
        return buildBaseFile(path).build();
    }

    /**
     * Creates directory instance with path of parent and file name by given path.
     *
     * @param path the path
     * @return the file as {@link MCRExternalStoreFileInfo} instance
     */
    public static MCRExternalStoreFileInfo createDirectory(String path) {
        return buildBaseFile(path).directory(true).build();
    }

    /**
     * Generates {@link Map} instance from string.
     *
     * @param mapString the string
     * @return the {@link Map}
     * @throws JsonMappingException if input is invalid
     * @throws JsonProcessingException if input is invalid
     */
    public static Map<String, String> getMap(String mapString)
        throws JsonMappingException, JsonProcessingException {
        final ObjectReader reader = new ObjectMapper().readerFor(Map.class);
        return reader.readValue(mapString);
    }

    /**
     * Returns a list of parents for path.
     *
     * @param path the path
     * @return the list
     */
    public static List<String> getPaths(String path) {
        return Optional.of(path).filter(p -> !p.isEmpty()).map(p -> p.split("/")).map(List::of)
            .orElseGet(() -> Collections.emptyList());
    }

    /**
     * Returns a list of {@link MCRExternalStoreFileInfo} with all parent directories.
     *
     * @param path the path
     * @return the list
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

    /**
     * Generates string of {@link Map} instance.
     * @param map the map
     * @return the map as string
     * @throws JsonProcessingException if map is invalid
     */
    public static String getMapString(Map<String, String> map) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(map);
    }

    private static MCRExternalStoreFileInfoBuilder buildBaseFile(String path) {
        return new MCRExternalStoreFileInfo.MCRExternalStoreFileInfoBuilder(getFileName(path),
            getParentPath(path));
    }

}
