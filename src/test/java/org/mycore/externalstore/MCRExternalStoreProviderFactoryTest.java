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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.mycore.common.MCRTestCase;
import org.mycore.externalstore.exception.MCRExternalStoreException;
import org.mycore.externalstore.s3.MCRExternalStoreS3Provider;

public class MCRExternalStoreProviderFactoryTest extends MCRTestCase {

    @Override
    protected Map<String, String> getTestProperties() {
        final Map<String, String> testProperties = super.getTestProperties();
        testProperties.put("MCR.ExternalStore.s3.Provider.Class", MCRExternalStoreS3Provider.class.getName());
        testProperties.put("MCR.ExternalStore.broken.Provider.Class", "org.mycore.externalstore.DoesNotExist");
        testProperties.put("MCR.ExternalStore.wrongtype.Provider.Class", Object.class.getName());
        return testProperties;
    }

    @Test
    public void testIsConfigured() {
        assertTrue(MCRExternalStoreProviderFactory.isConfigured("s3"));
        assertTrue(MCRExternalStoreProviderFactory.isConfigured("broken"));
        assertFalse(MCRExternalStoreProviderFactory.isConfigured("unknown"));
    }

    @Test
    public void testCreateStoreProvider_brokenConfiguration() {
        assertThrows(MCRExternalStoreException.class,
            () -> MCRExternalStoreProviderFactory.createStoreProvider("broken", Map.of()));
    }

    @Test
    public void testCreateStoreProvider_noProviderClass() {
        assertThrows(MCRExternalStoreException.class,
            () -> MCRExternalStoreProviderFactory.createStoreProvider("wrongtype", Map.of()));
    }

    @Test
    public void testCreateStoreProvider_notConfigured() {
        assertThrows(MCRExternalStoreException.class,
            () -> MCRExternalStoreProviderFactory.createStoreProvider("unknown", Map.of()));
    }
}
