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

import org.mycore.externalstore.model.MCRExternalStoreFileInfo;

/**
 * An external store offers functionalities for reading content or providing files.
 */
public class MCRExternalStore {

    private final String storeType;

    private final Map<String, String> storeSettings;

    private volatile MCRExternalStoreProvider storeProvider;

    /**
     * Constructs external store with store type and store settings.
     *
     * @param storeType store type
     * @param storeSettings store settings including provider settings
     */
    public MCRExternalStore(String storeType, Map<String, String> storeSettings) {
        this.storeType = storeType;
        this.storeSettings = storeSettings;
    }

    /**
     * Returns the store settings.
     *
     * @return store settings
     */
    public Map<String, String> getStoreSettings() {
        return storeSettings;
    }

    /**
     * Returns the {@link MCRExternalStoreProvider}.
     *
     * @return store provider
     */
    public MCRExternalStoreProvider getStoreProvider() {
        MCRExternalStoreProvider provider = storeProvider;
        if (provider == null) {
            synchronized (this) {
                provider = storeProvider;
                if (provider == null) {
                    storeProvider
                        = MCRExternalStoreProviderFactory.createStoreProvider(storeType, storeSettings);
                }
            }
        }
        return storeProvider;
    }

    /**
     * Returns a list over {@link MCRExternalStoreFileInfo} elements for given path.
     *
     * @param path path
     * @return list over file info elements
     * @throws IOException if an I/O error occurs
     */
    public List<MCRExternalStoreFileInfo> listFileInfos(String path) throws IOException {
        return getStoreProvider().listFileInfos(path);
    }

    /**
     * Returns {@link MCRExternalStoreFileInfo} for given path.
     *
     * @param path path
     * @return file info
     * @throws IOException if an I/O error occurs
     */
    public MCRExternalStoreFileInfo getFileInfo(String path) throws IOException {
        return getStoreProvider().getFileInfo(path);
    }

    /**
     * Opens and returns {@link InputStream} for given path.
     *
     * @param path path
     * @return input stream
     * @throws IOException if an I/O error occurs
     */
    public InputStream newInputStream(String path) throws IOException {
        checkFile(path);
        return getStoreProvider().newInputStream(path);
    }

    /**
     * Opens and returns {@link SeekableByteChannel} for given path.
     *
     * @param path path
     * @return input stream
     * @throws IOException if an I/O error occurs
     */
    public SeekableByteChannel newByteChannel(String path) throws IOException {
        checkFile(path);
        return getStoreProvider().newByteChannel(path);
    }

    private void checkFile(String path) throws IOException {
        final MCRExternalStoreFileInfo fileInfo = getStoreProvider().getFileInfo(path);
        if (fileInfo.isDirectory()) {
            throw new IOException("Object is a directory");
        }
        if (fileInfo.getSize() > MCRExternalStoreConstants.MAX_DOWNLOAD_SIZE) {
            throw new IOException("Object exceeds max size");
        }
    }
}
