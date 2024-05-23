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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.SeekableByteChannel;
import java.util.List;
import java.util.Map;

import org.mycore.externalstore.exception.MCRExternalStoreNoAccessException;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;

/**
 * An external store provider offers an interface to an external store.
 * This includes providing file info or the provision of files.
 */
public interface MCRExternalStoreProvider {

    /**
     * Initializes the provider by given settings map.
     *
     * @param settingsMap settings map
     */
    public void init(Map<String, String> settingsMap);

    /**
     * Opens and returns a {@link SeekableByteChannel} for a path.
     *
     * @param path path to file
     * @return byte channel
     * @throws IOException if am I/O error occurs
     */
    public SeekableByteChannel newByteChannel(String path) throws IOException;

    /**
     * Opens and returns an {@link InputStream} for a path.
     *
     * @param path path to file
     * @return input stream
     * @throws IOException if an I/O error occurs
     */
    public InputStream newInputStream(String path) throws IOException;

    /**
     * Returns an {@link MCRExternalStoreFileInfo} for a path.
     *
     * @param path path to file
     * @return file info
     * @throws IOException if an I/O error occurs
     */
    public MCRExternalStoreFileInfo getFileInfo(String path) throws IOException;

    /**
     * Returns a list over {@link MCRExternalStoreFileInfo} elements for a path.
     *
     * @param path path to files
     * @return list of file info elements
     * @throws IOException if an I/O error occurs
     */
    public List<MCRExternalStoreFileInfo> listFileInfos(String path) throws IOException;

    /**
     * Returns a list over {@link MCRExternalStoreFileInfo} elements for a path (recursive).
     *
     * @param path the path to files
     * @return list of all files or directories
     * @throws IOException if an I/O error occurs
     */
    public List<MCRExternalStoreFileInfo> listFileInfosRecursive(String path) throws IOException;

    /**
     * Generates and returns url to download file.
     *
     * @param path path to file
     * @return download url
     */
    public URL getDownloadUrl(String path);

    /**
     * Returns base url.
     *
     * @return base url
     * @throws MalformedURLException if url is malformed
     */
    public URL getEndpointUrl() throws MalformedURLException;

    /**
     * Ensures that the provider is able to read.
     *
     * @throws MCRExternalStoreNoAccessException if an access problem occurs
     */
    public void ensureReadAccess();

}
