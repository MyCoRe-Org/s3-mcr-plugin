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

import java.util.Locale;

import org.mycore.externalstore.model.MCRExternalStoreFileInfo;
import org.mycore.externalstore.util.MCRExternalStoreUtils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * Provides helper methods for {@link MCRExternalStoreS3Provider}.
 */
public class MCRExternalStoreS3ProviderHelper {

    /**
     * Builds {@link AmazonS3} instance by given {@link MCRS3Settings}.
     *
     * @param settings the settings
     * @return the client
     */
    public static AmazonS3 createClient(MCRS3Settings settings) {
        final ClientConfiguration clientConfig
            = new ClientConfiguration().withProtocol(Protocol.valueOf(settings.getProtocol().toUpperCase(Locale.ROOT)));
        final AWSCredentials credentials = new BasicAWSCredentials(settings.getAccessKey(), settings.getSecretKey());
        final AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(settings.getEndpoint(), settings.getSigningRegion()))
            .withPathStyleAccessEnabled(settings.isPathStyleAccess())
            .withClientConfiguration(clientConfig)
            .withCredentials(new AWSStaticCredentialsProvider(credentials));
        return builder.build();
    }

    /**
     * Maps {@link S3ObjectSummary} to {@link MCRExternalStoreFileInfo}.
     *
     * @param summary the object summary
     * @return the remote stored file
     */
    protected static MCRExternalStoreFileInfo mapToFileInfos(S3ObjectSummary summary) {
        final MCRExternalStoreFileInfo fileInfo = MCRExternalStoreUtils.createBaseFile(summary.getKey());
        fileInfo.setSize(summary.getSize());
        fileInfo.setLastModified(summary.getLastModified());
        fileInfo.setChecksum(summary.getETag());
        return fileInfo;
    }

}
