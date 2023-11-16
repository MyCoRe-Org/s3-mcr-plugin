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
import org.mycore.externalstore.exception.MCRExternalStoreIntegrityViolationException;
import org.mycore.externalstore.exception.MCRExternalStoreNotFoundException;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;

public class MCRExternalStore {

    private final String storeType;

    private final Map<String, String> storeProviderSettings;

    private MCRExternalStoreProvider storeProvider;

    /**
     * Constructs a new {@link MCRExternalStore}.
     *
     * @param storeType store type
     * @param storeProviderSettings store provider settings
     */
    public MCRExternalStore(String storeType, Map<String, String> storeProviderSettings) {
        this.storeType = storeType;
        this.storeProviderSettings = storeProviderSettings;
    }

    /**
     * Lists {@link MCRExternalStoreFileInfo} for given path.
     *
     * @param path path
     * @return file infos
     * @throws IOException
     */
    public List<MCRExternalStoreFileInfo> listFileInfos(String path) throws IOException {
        return getStoreProvider().listFileInfos(path);
    }

    public MCRExternalStoreFileInfo getFileInfo(String path) throws IOException {
        return getStoreProvider().getFileInfo(path);
    }

    /**
     * Creates new {@link InputStream} for given path.
     *
     * @param path path
     * @return input stream
     * @throws IOException
     * @throws MCRExternalStoreNotFoundException if cannot find file
     * @throws MCRExternalStoreIntegrityViolationException if local checksum does not match
     */
    public InputStream newInputStream(String path) throws IOException {
        checkFile(path);
        return getStoreProvider().newInputStream(path);
    }

    /**
     * Creates new {@link SeekableByteChannel} for given path.
     *
     * @param path path
     * @return input stream
     * @throws IOException
     * @throws MCRExternalStoreNotFoundException if cannot find file
     * @throws MCRExternalStoreIntegrityViolationException if local checksum does not match
     */
    public SeekableByteChannel newByteChannel(String path) throws IOException {
        checkFile(path);
        return getStoreProvider().newByteChannel(path);
    }

    /**
     * Returns all store provider settings.
     *
     * @return store provider settings
     */
    public Map<String, String> getStoreProviderSettings() {
        return storeProviderSettings;
    }

    private void checkFile(String path) throws IOException {
        final MCRExternalStoreFileInfo fileInfo = getStoreProvider().getFileInfo(path);
        if (fileInfo.isDirectory()) {
            throw new MCRExternalStoreException("Object is a directory");
        }
        if (fileInfo.getSize() > MCRExternalStoreConstants.MAX_DOWNLOAD_SIZE) {
            throw new MCRExternalStoreException("Object exceeds max size!");
        }
    }

    /**
     * Returns the {@link MCRExternalStoreProvider}.
     *
     * @return store provider
     */
    public MCRExternalStoreProvider getStoreProvider() {
        if (storeProvider == null) {
            storeProvider = MCRExternalStoreProviderFactory.createStoreProvider(storeType, storeProviderSettings);
        }
        return storeProvider;
    }

}
