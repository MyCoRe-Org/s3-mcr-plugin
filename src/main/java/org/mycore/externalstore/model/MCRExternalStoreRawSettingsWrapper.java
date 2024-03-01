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

package org.mycore.externalstore.model;

import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Wrapper model for remote store config/settings.
 */
public class MCRExternalStoreRawSettingsWrapper {

    private String content;

    private boolean encryptedContent;

    /**
     * Constructs new settings wrapper.
     * Necessary for {@link ObjectMapper}.
     */
    @SuppressWarnings("PMD.UnnecessaryConstructor")
    public MCRExternalStoreRawSettingsWrapper() {

    }

    /**
     * Returns if content is encrypted.
     *
     * @return true if content is encrypted
     */
    public boolean isEncryptedContent() {
        return encryptedContent;
    }

    /**
     * Sets if content is encrypted.
     *
     * @param encryptedContent true is content is encrypted
     */
    public void setEncryptedContent(boolean encryptedContent) {
        this.encryptedContent = encryptedContent;
    }

    /**
     * Returns the raw content which is may encrypted.
     *
     * @return the raw content
     */
    public String getRawContent() {
        return content;
    }

    /**
     * Sets the raw content which is may encrypted.
     *
     * @param rawContent the raw content
     */
    public void setRawContent(String rawContent) {
        this.content = rawContent;
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, encryptedContent);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MCRExternalStoreRawSettingsWrapper other = (MCRExternalStoreRawSettingsWrapper) obj;
        return Objects.equals(content, other.content) && encryptedContent == other.encryptedContent;
    }

    @Override
    public String toString() {
        return "Content: " + content + "\nEncrypted: " + encryptedContent + "\n";
    }

}
