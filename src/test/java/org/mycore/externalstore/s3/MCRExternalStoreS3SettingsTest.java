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

package org.mycore.externalstore.s3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class MCRExternalStoreS3SettingsTest {

    private static final String ACCESS_KEY = "accesskey";

    private static final String BUCKET = "bucket";

    private static final String DIRECTORY = "directory";

    private static final String ENDPOINT = "endpoint";

    private static final String PROTOCOL = "https";

    private static final String SECRET_KEY = "secret";

    private static final String SIGNING_REGION = "foo";

    @Test
    public void testFromMap() {
        final MCRExternalStoreS3Settings settings = MCRExternalStoreS3Settings.fromMap(getSettingsMap());
        assertEquals(ACCESS_KEY, settings.accessKey());
        assertEquals(BUCKET, settings.bucket());
        assertEquals(DIRECTORY, settings.directory());
        assertEquals(ENDPOINT, settings.endpoint());
        assertTrue(settings.pathStyleAccess());
        assertEquals(PROTOCOL, settings.protocol());
        assertEquals(SECRET_KEY, settings.secretKey());
        assertEquals(SIGNING_REGION, settings.signingRegion());
    }

    @Test
    public void testFromMap_directoryNull() {
        final Map<String, String> settingsMap = getSettingsMap();
        settingsMap.remove(MCRExternalStoreS3Settings.DIRECTORY);
        final MCRExternalStoreS3Settings settings = MCRExternalStoreS3Settings.fromMap(settingsMap);
        assertNull(null, settings.directory());
    }

    @Test
    public void testFromMap_noPathStyleAccessNull() {
        final Map<String, String> settingsMap = getSettingsMap();
        settingsMap.remove(MCRExternalStoreS3Settings.PATH_STYLE_ACCESS);
        final MCRExternalStoreS3Settings settings = MCRExternalStoreS3Settings.fromMap(settingsMap);
        assertFalse(settings.pathStyleAccess());
    }

    @Test
    public void testFromMap_pathStyleAccessFalse() {
        final Map<String, String> settingsMap = getSettingsMap();
        settingsMap.put(MCRExternalStoreS3Settings.PATH_STYLE_ACCESS, "false");
        final MCRExternalStoreS3Settings settings = MCRExternalStoreS3Settings.fromMap(settingsMap);
        assertFalse(settings.pathStyleAccess());
    }

    private Map<String, String> getSettingsMap() {
        final Map<String, String> settingsMap = new HashMap<>();
        settingsMap.put(MCRExternalStoreS3Settings.ACCESS_KEY, ACCESS_KEY);
        settingsMap.put(MCRExternalStoreS3Settings.BUCKET, BUCKET);
        settingsMap.put(MCRExternalStoreS3Settings.DIRECTORY, DIRECTORY);
        settingsMap.put(MCRExternalStoreS3Settings.ENDPOINT, ENDPOINT);
        settingsMap.put(MCRExternalStoreS3Settings.PATH_STYLE_ACCESS, "true");
        settingsMap.put(MCRExternalStoreS3Settings.PROTOCOL, PROTOCOL);
        settingsMap.put(MCRExternalStoreS3Settings.SECRET_KEY, SECRET_KEY);
        settingsMap.put(MCRExternalStoreS3Settings.SIGNING_REGION, SIGNING_REGION);
        return settingsMap;
    }

}
