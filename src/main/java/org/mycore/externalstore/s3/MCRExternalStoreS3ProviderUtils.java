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

/**
 * Provides utility methods for s3.
 */
public class MCRExternalStoreS3ProviderUtils {

    public static final String ENDPOINT = "endpoint";

    public static final String BUCKET = "bucket";

    public static final String SECRET_KEY = "secretKey";

    public static final String PATH_STYLE_ACCESS = "pathStyleAccess";

    public static final String PROTOCOL = "protocol";

    public static final String ACCESS_KEY = "accessKey";

    public static final String DIRECTORY = "directory";

    public static final String SIGNING_REGION = "signingRegion";

    /**
     * Maps map to {@link MCRS3Settings}.
     *
     * @param map the map
     * @return the s3 settings.
     */
    public static MCRS3Settings mapToS3Settings(Map<String, String> map) {
        final MCRS3Settings settings = new MCRS3Settings();
        settings.setAccessKey(map.get(MCRS3Settings.ACCESS_KEY));
        settings.setBucket(map.get(MCRS3Settings.BUCKET));
        settings.setSecretKey(map.get(MCRS3Settings.SECRET_KEY));
        settings.setDirectory(map.get(MCRS3Settings.DIRECTORY));
        settings.setEndpoint(map.get(MCRS3Settings.ENDPOINT));
        settings.setProtocol(map.get(MCRS3Settings.PROTOCOL));
        settings.setPathStyleAccess(Boolean.valueOf(map.get(MCRS3Settings.PATH_STYLE_ACCESS)));
        settings.setSigningRegion(map.get(MCRS3Settings.SIGNING_REGION));
        return settings;
    }

}
