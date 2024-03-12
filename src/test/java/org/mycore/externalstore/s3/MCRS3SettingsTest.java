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

public class MCRS3SettingsTest {

    private static final String ACCESS_KEY = "accesskey";

    private static final String BUCKET = "bucket";

    private static final String DIRECTORY = "directory";

    private static final String ENDPOINT = "endpoint";

    private static final String PROTOCOL = "https";

    private static final String SECRET_KEY = "secret";

    private static final String SIGNING_REGION = "foo";

    @Test
    public void testFromMap() {
        final Map<String, String> settingsMap = new HashMap<>();
        settingsMap.put(MCRS3Settings.ACCESS_KEY, ACCESS_KEY);
        settingsMap.put(MCRS3Settings.BUCKET, BUCKET);
        settingsMap.put(MCRS3Settings.DIRECTORY, DIRECTORY);
        settingsMap.put(MCRS3Settings.ENDPOINT, ENDPOINT);
        settingsMap.put(MCRS3Settings.PATH_STYLE_ACCESS, "true");
        settingsMap.put(MCRS3Settings.PROTOCOL, PROTOCOL);
        settingsMap.put(MCRS3Settings.SECRET_KEY, SECRET_KEY);
        settingsMap.put(MCRS3Settings.SIGNING_REGION, SIGNING_REGION);

        final MCRS3Settings settings = MCRS3Settings.fromMap(settingsMap);
        assertEquals(ACCESS_KEY, settings.getAccessKey());
        assertEquals(BUCKET, settings.getBucket());
        assertEquals(DIRECTORY, settings.getDirectory());
        assertEquals(ENDPOINT, settings.getEndpoint());
        assertTrue(settings.isPathStyleAccess());
        assertEquals(PROTOCOL, settings.getProtocol());
        assertEquals(SECRET_KEY, settings.getSecretKey());
        assertEquals(SIGNING_REGION, settings.getSigningRegion());
    }

    @Test
    public void testFromMap_directoryNull() {
        final Map<String, String> settingsMap = new HashMap<>();
        final MCRS3Settings settings = MCRS3Settings.fromMap(settingsMap);
        assertNull(null, settings.getDirectory());
    }

    @Test
    public void testFromMap_noPathStyleAccessNull() {
        final Map<String, String> settingsMap = new HashMap<>();
        final MCRS3Settings settings = MCRS3Settings.fromMap(settingsMap);
        assertFalse(settings.isPathStyleAccess());
    }

    @Test
    public void testFromMap_pathStyleAccessFalse() {
        final Map<String, String> settingsMap = new HashMap<>();
        settingsMap.put(MCRS3Settings.PATH_STYLE_ACCESS, "false");
        final MCRS3Settings settings = MCRS3Settings.fromMap(settingsMap);
        assertFalse(settings.isPathStyleAccess());
    }

}
