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

import java.util.Optional;

/**
 * Provides model for s3 settings.
 */
public class MCRS3Settings {

    public static final String ENDPOINT = "endpoint";

    public static final String BUCKET = "bucket";

    public static final String SECRET_KEY = "secretKey";

    public static final String PATH_STYLE_ACCESS = "pathStyleAccess";

    public static final String PROTOCOL = "protocol";

    public static final String ACCESS_KEY = "accessKey";

    public static final String DIRECTORY = "directory";

    public static final String SIGNING_REGION = "signingRegion";

    private String endpoint;

    private Boolean pathStyleAccess;

    private String signingRegion;

    private String accessKey;

    private String secretKey;

    private String protocol;

    private String bucket;

    private String directory;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public boolean isPathStyleAccess() {
        return Optional.ofNullable(pathStyleAccess).orElse(true);
    }

    public void setPathStyleAccess(boolean pathStyleAccess) {
        this.pathStyleAccess = pathStyleAccess;
    }

    public String getSigningRegion() {
        return Optional.ofNullable(signingRegion).orElse("us-east-1");
    }

    public void setSigningRegion(String signingRegion) {
        this.signingRegion = signingRegion;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = Optional.ofNullable(protocol).orElse("https");
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

}
