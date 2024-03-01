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

package org.mycore.externalstore.archive;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.mycore.common.content.MCRSeekableChannelContent;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;

/**
 * An archive resolver obtains information about archives and provides files.
 */
public abstract class MCRExternalStoreArchiveResolver {

    private MCRSeekableChannelContent content;

    /**
     * Sets {@link MCRSeekableChannelContent} to resolver.
     *
     * @param content content
     */
    public void setContent(MCRSeekableChannelContent content) {
        this.content = content;
    }

    /**
     * Returns the {@link MCRSeekableChannelContent} content.
     *
     * @return content
     */
    protected MCRSeekableChannelContent getContent() {
        return content;
    }

    /**
     * Resolves and returns a list of {@link MCRExternalStoreFileInfo} elements.
     *
     * @return a list of file info
     * @throws IOException if there is an exception while reading
     */
    public abstract List<MCRExternalStoreFileInfo> listFileInfos() throws IOException;

    /**
     * Opens and returns an {@link InputStream} for file specified by path.
     *
     * @param path the path
     * @return input stream
     * @throws IOException if an I/O error occurs
     */
    public abstract InputStream getInputStream(String path) throws IOException;

}
