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

package org.mycore.filesystem.s3;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Optional;

@XmlRootElement(name = "XMLS3Bucket")
public class S3BucketSettings {

    private String endpoint;

    private Boolean pathStyleAccess;

    private String signingRegion;

    private String accessKey;

    private String secretKey;

    private String protocol;

    private String bucket;

    public String getEndpoint() {
        return endpoint;
    }

    @XmlElement(name = "endpoint", required = true)
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public boolean isPathStyleAccess() {
        return Optional.ofNullable(pathStyleAccess).orElse(true);
    }

    @XmlElement(name = "pathStyleAccess", defaultValue = "true", required = true)
    public void setPathStyleAccess(boolean pathStyleAccess) {
        this.pathStyleAccess = pathStyleAccess;
    }

    public String getSigningRegion() {
        return Optional.ofNullable(signingRegion).orElse("us-east-1");
    }

    @XmlElement(name = "signingRegion", defaultValue = "us-east-1")
    public void setSigningRegion(String signingRegion) {
        this.signingRegion = signingRegion;
    }

    public String getAccessKey() {
        return accessKey;
    }

    @XmlElement(name = "accessKey", required = true)
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    @XmlElement(name = "secretKey", required = true)
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getProtocol() {
        return protocol;
    }

    @XmlElement(name = "protocol", required = true, defaultValue = "https")
    public void setProtocol(String protocol) {
        this.protocol = Optional.ofNullable(protocol).orElse("https");
    }

    public String getBucket() {
        return bucket;
    }

    @XmlElement(name = "bucket", required = true)
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }
}
