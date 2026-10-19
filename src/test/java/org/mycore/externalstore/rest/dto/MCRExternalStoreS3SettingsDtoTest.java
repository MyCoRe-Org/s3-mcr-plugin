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

package org.mycore.externalstore.rest.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mycore.externalstore.MCRExternalStore;
import org.mycore.externalstore.s3.MCRExternalStoreS3Settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MCRExternalStoreS3SettingsDtoTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void testSecretKeyIsNotExposed() throws JsonProcessingException {
        final Map<String, String> settings = Map.of(
            MCRExternalStoreS3Settings.PROTOCOL, "http",
            MCRExternalStoreS3Settings.ENDPOINT, "localhost:9000",
            MCRExternalStoreS3Settings.BUCKET, "test",
            MCRExternalStoreS3Settings.ACCESS_KEY, "access",
            MCRExternalStoreS3Settings.SECRET_KEY, "secret",
            MCRExternalStore.USE_DOWNLOAD_PROXY, "true");
        final MCRDerivateInfoDto info = new MCRDerivateInfoDto("mcr_derivate_00000001", List.of(),
            MCRExternalStoreS3SettingsDto.fromMap(settings), true, true, true);

        final String json = MAPPER.writeValueAsString(info);
        final JsonNode metadata = MAPPER.readTree(json).get("metadata");

        assertFalse(json.contains("secret"));
        assertFalse(metadata.has(MCRExternalStoreS3Settings.SECRET_KEY));
        assertEquals("access", metadata.get(MCRExternalStoreS3Settings.ACCESS_KEY).asText());
        assertEquals("true", metadata.get(MCRExternalStore.USE_DOWNLOAD_PROXY).asText());
        // unset settings are omitted
        assertFalse(metadata.has(MCRExternalStoreS3Settings.DIRECTORY));
        assertTrue(metadata.has(MCRExternalStoreS3Settings.ENDPOINT));
    }
}
