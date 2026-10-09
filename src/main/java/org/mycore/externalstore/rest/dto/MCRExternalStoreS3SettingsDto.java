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

import java.util.Map;

import org.mycore.externalstore.MCRExternalStore;
import org.mycore.externalstore.s3.MCRExternalStoreS3Settings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Dto for S3 store settings without secret key.
 *
 * @param protocol protocol
 * @param endpoint endpoint
 * @param bucket bucket
 * @param accessKey access key
 * @param signingRegion signing region
 * @param pathStyleAccess path style access
 * @param directory directory
 * @param useDownloadProxy use download proxy
 * @param customDownloadProxyUrl custom download proxy url
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MCRExternalStoreS3SettingsDto(@JsonProperty("protocol") String protocol,
    @JsonProperty("endpoint") String endpoint, @JsonProperty("bucket") String bucket,
    @JsonProperty("accessKey") String accessKey, @JsonProperty("signingRegion") String signingRegion,
    @JsonProperty("pathStyleAccess") String pathStyleAccess, @JsonProperty("directory") String directory,
    @JsonProperty("useDownloadProxy") String useDownloadProxy,
    @JsonProperty("customDownloadProxyUrl") String customDownloadProxyUrl) implements MCRExternalStoreSettingsDto {

    /**
     * Creates dto from store settings.
     *
     * @param settings store settings
     * @return dto
     */
    public static MCRExternalStoreS3SettingsDto fromMap(Map<String, String> settings) {
        return new MCRExternalStoreS3SettingsDto(settings.get(MCRExternalStoreS3Settings.PROTOCOL),
            settings.get(MCRExternalStoreS3Settings.ENDPOINT), settings.get(MCRExternalStoreS3Settings.BUCKET),
            settings.get(MCRExternalStoreS3Settings.ACCESS_KEY),
            settings.get(MCRExternalStoreS3Settings.SIGNING_REGION),
            settings.get(MCRExternalStoreS3Settings.PATH_STYLE_ACCESS),
            settings.get(MCRExternalStoreS3Settings.DIRECTORY), settings.get(MCRExternalStore.USE_DOWNLOAD_PROXY),
            settings.get(MCRExternalStore.CUSTOM_DOWNLOAD_PROXY_URL));
    }
}
