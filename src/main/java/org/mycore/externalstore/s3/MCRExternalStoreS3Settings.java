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

import java.util.Map;
import java.util.Optional;

/**
 * Provides model for external store s3 settings.
 *
 * @param protocol protocol name
 * @param endpoint endpoint url
 * @param accessKey access key
 * @param secretKey secret key
 * @param bucket bucket name
 * @param signingRegion signing region
 * @param pathStyleAccess if path style access is enabled
 * @param directory key prefix
 */
public record MCRExternalStoreS3Settings(String protocol, String endpoint, String accessKey, String secretKey,
    String bucket, String signingRegion, boolean pathStyleAccess, String directory) {

    /**
     * Protocol map key.
     */
    public static final String PROTOCOL = "protocol";

    /**
     * Endpoint map key.
     */
    public static final String ENDPOINT = "endpoint";

    /**
     * Access key map key.
     */
    public static final String ACCESS_KEY = "accessKey";

    /**
     * Secret key map key.
     */
    public static final String SECRET_KEY = "secretKey";

    /**
     * Bucket map key.
     */
    public static final String BUCKET = "bucket";

    /**
     * Signing region map key.
     */
    public static final String SIGNING_REGION = "signingRegion";

    /**
     * Path style access key.
     */
    public static final String PATH_STYLE_ACCESS = "pathStyleAccess";

    /**
     * Directory map key.
     */
    public static final String DIRECTORY = "directory";

    /**
     * Creates and returns settings from map.
     *
     * @param map map
     * @return s3 settings
     */
    public static MCRExternalStoreS3Settings fromMap(Map<String, String> map) {
        final String protocol = Optional.ofNullable(map.get(PROTOCOL)).orElse("https");
        final String endpoint = Optional.ofNullable(map.get(ENDPOINT))
            .orElseThrow(() -> new IllegalArgumentException());
        final String accessKey = Optional.ofNullable(map.get(ACCESS_KEY))
            .orElseThrow(() -> new IllegalArgumentException());
        final String secretKey = Optional.ofNullable(map.get(SECRET_KEY))
            .orElseThrow(() -> new IllegalArgumentException());
        final String bucket = Optional.ofNullable(map.get(BUCKET))
            .orElseThrow(() -> new IllegalArgumentException());
        final String signingRegion = Optional.ofNullable(map.get(SIGNING_REGION)).orElse("us-east-1");
        final boolean isPathStyleAccess = Optional.ofNullable(map.get(PATH_STYLE_ACCESS)).map(Boolean::parseBoolean)
            .orElse(false);
        final String directory = map.get(DIRECTORY);

        return new MCRExternalStoreS3Settings(protocol, endpoint, accessKey, secretKey, bucket, signingRegion,
            isPathStyleAccess,
            directory);
    }
}
