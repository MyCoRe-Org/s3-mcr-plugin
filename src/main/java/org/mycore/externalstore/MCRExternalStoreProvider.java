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

package org.mycore.externalstore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;
import java.util.List;
import java.util.Map;

import org.mycore.externalstore.exception.MCRExternalStoreException;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;

/**
 * A {@link MCRExternalStoreProvider} offers an interface to an external store.
 * This includes providing file infos or the provision of files.
 */
public interface MCRExternalStoreProvider {

    /**
     * Initializes the provider by given settings map.
     *
     * @param settings the settings map
     */
    public void init(Map<String, String> settingsMap);

    /**
     * Creates new {@link SeekableByteChannel} instance for path.
     *
     * @param path the path
     * @return the channel
     * @throws IOException if channeling fails
     */
    public SeekableByteChannel newByteChannel(String path) throws IOException;

    /**
     * Creates new {@link InputStream} instance for path.
     *
     * @param path the path
     * @return the channel
     * @throws IOException if streaming fails
     */
    public InputStream newInputStream(String path) throws IOException;

    /**
     * Get {@link MCRExternalStoreFileInfo} for given path.
     *
     * @param path the path
     * @return the file info
     */
    public MCRExternalStoreFileInfo getFileInfo(String path) throws IOException;

    /**
     * Lists all files and directories as {@link MCRExternalStoreFileInfo} by specified path.
     *
     * @param path the path
     * @return list of all files or directories
     * @throws IOException if request fails
     */
    public List<MCRExternalStoreFileInfo> listFileInfos(String path) throws IOException;

    /**
     * Lists all files in directory as {@link MCRExternalStoreFileInfo} recursive specified by path.
     *
     * @param path the path
     * @return list of all files or directories
     * @throws IOException if the directory cannot be read
     */
    public List<MCRExternalStoreFileInfo> listFileInfosRecursive(String path) throws IOException;

    /**
     * Checks whether the client is able to read.
     *
     * @throws MCRExternalStoreException if there is an access problem
     */
    public void checkReadAccess();

}
