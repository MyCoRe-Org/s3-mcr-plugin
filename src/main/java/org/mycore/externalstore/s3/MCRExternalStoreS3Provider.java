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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.SeekableByteChannel;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.externalstore.MCRExternalStoreProvider;
import org.mycore.externalstore.exception.MCRExternalStoreNoAccessException;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;
import org.mycore.externalstore.util.MCRExternalStoreUtils;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/**
 * An {@link MCRExternalStoreProvider} for s3 bucket.
 */
public class MCRExternalStoreS3Provider implements MCRExternalStoreProvider {

    private static final Logger LOGGER = LogManager.getLogger();

    private MCRExternalStoreS3Settings settings;

    private S3Client client;

    @Override
    public void init(Map<String, String> settingsMap) {
        settings = MCRExternalStoreS3Settings.fromMap(settingsMap);
        client = createClient(settings);
    }

    @Override
    public InputStream newInputStream(String path) throws IOException {
        final GetObjectRequest objectRequest = GetObjectRequest.builder()
            .bucket(settings.bucket())
            .key(getKey(path))
            .build();
        return client.getObject(objectRequest);
    }

    @Override
    public SeekableByteChannel newByteChannel(String path) throws IOException {
        return new MCRS3SeekableFileChannel(client, settings.bucket(), getKey(path));
    }

    @Override
    public MCRExternalStoreFileInfo getFileInfo(String path) {
        final HeadObjectResponse object = headObject(path);
        final String parentPath = MCRExternalStoreUtils.getParentPath(path);
        final String name = MCRExternalStoreUtils.getFileName(path);
        return new MCRExternalStoreFileInfo.Builder(name, parentPath)
            .checksum(object.eTag().replace("\"", ""))
            .size(object.contentLength())
            .lastModified(Date.from(object.lastModified()))
            .directory(false)
            .build();
    }

    @Override
    public List<MCRExternalStoreFileInfo> listFileInfos(String path) throws IOException {
        final ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
            .bucket(settings.bucket())
            .prefix(getKey(path))
            .delimiter("/")
            .build();
        return list(listObjectsV2Request);
    }

    @Override
    public List<MCRExternalStoreFileInfo> listFileInfosRecursive(String path) throws IOException {
        final ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
            .bucket(settings.bucket())
            .prefix(getKey(path))
            .build();
        final List<MCRExternalStoreFileInfo> result = list(listObjectsV2Request);
        final Set<MCRExternalStoreFileInfo> directories = new HashSet<>();
        for (MCRExternalStoreFileInfo file : result) {
            directories.addAll(MCRExternalStoreUtils.getDirectories(file.parentPath()));
        }
        result.addAll(directories);
        return result;
    }

    @Override
    public URL getDownloadUrl(String path) {
        try (S3Presigner presigner = S3Presigner.create()) {
            final GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(settings.bucket())
                .key(getKey(path))
                .build();
            final GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(objectRequest)
                .signatureDuration(Duration.ofMinutes(60))
                .build();
            return presigner.presignGetObject(presignRequest).url();
        }
    }

    @Override
    public void ensureReadAccess() {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                .bucket(settings.bucket())
                .build();
            client.headBucket(headBucketRequest);
        } catch (S3Exception exception) {
            LOGGER.warn("Bucket head request failed", exception);
            if (exception.statusCode() == 403) {
                throw new MCRExternalStoreNoAccessException(exception.getMessage(), exception);
            }
            throw new MCRExternalStoreNoAccessException("Test failed.", exception);
        }
    }

    @Override
    public URL getEndpointUrl() throws MalformedURLException, URISyntaxException {
        String url;
        if (settings.pathStyleAccess()) {
            url = settings.protocol() + "://" + settings.endpoint();
        } else {
            url = settings.protocol() + "://" + settings.bucket() + "." + settings.endpoint();
        }
        return new URI(url).toURL();
    }

    /**
     * Builds {@link S3Client} instance by given {@link MCRExternalStoreS3Settings}.
     *
     * @param settings the settings
     * @return the client
     */
    protected static S3Client createClient(MCRExternalStoreS3Settings settings) {
        final AwsBasicCredentials awsCreds = AwsBasicCredentials.create(settings.accessKey(), settings.secretKey());
        final S3Configuration serviceConfiguration
            = S3Configuration.builder().pathStyleAccessEnabled(settings.pathStyleAccess()).build();
        return S3Client.builder()
            .endpointOverride(URI.create(settings.protocol() + "://" + settings.endpoint()))
            .region(Region.of(settings.signingRegion()))
            .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
            .serviceConfiguration(serviceConfiguration)
            .build();
    }

    private List<MCRExternalStoreFileInfo> list(ListObjectsV2Request listObjectsV2Request) throws IOException {
        final List<MCRExternalStoreFileInfo> fileInfos = new ArrayList<>();
        boolean done = false;
        while (!done) {
            final ListObjectsV2Response listObjectsV2response = client.listObjectsV2(listObjectsV2Request);
            listObjectsV2response.contents().stream().map(this::toFileInfo).forEach(fileInfos::add);
            listObjectsV2response.commonPrefixes().stream().map(this::toDirectoryInfo).forEach(fileInfos::add);
            if (listObjectsV2response.nextContinuationToken() == null) {
                done = true;
            }
            listObjectsV2Request = listObjectsV2Request.toBuilder()
                .continuationToken(listObjectsV2response.nextContinuationToken())
                .build();
        }
        return fileInfos;
    }

    /**
     * Maps {@link S3Object} to {@link MCRExternalStoreFileInfo}.
     *
     * @param object the object
     * @return file info
     */
    protected MCRExternalStoreFileInfo toFileInfo(S3Object object) {
        final String fileName = MCRExternalStoreUtils.getFileName(object.key());
        final String parentPath = removeDirectory(MCRExternalStoreUtils.getParentPath(object.key()));
        return new MCRExternalStoreFileInfo.Builder(fileName, parentPath)
            .size(object.size())
            .directory(false)
            .lastModified(Date.from(object.lastModified()))
            .checksum(object.eTag().replace("\"", ""))
            .build();
    }

    /**
     * Maps prefix to {@link MCRExternalStoreFileInfo}.
     *
     * @param prefix prefix 
     * @return directory info
     */
    protected MCRExternalStoreFileInfo toDirectoryInfo(CommonPrefix prefix) {
        final String fileName = MCRExternalStoreUtils.getFileName(prefix.prefix());
        final String parentPath = removeDirectory(MCRExternalStoreUtils.getParentPath(prefix.prefix()));
        return new MCRExternalStoreFileInfo.Builder(fileName, parentPath).directory(true).build();
    }

    private HeadObjectResponse headObject(String path) {
        final HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
            .bucket(settings.bucket())
            .key(getKey(path))
            .build();
        return client.headObject(headObjectRequest);
    }

    private String getKey(String path) {
        return Optional.ofNullable(settings.directory())
            .map(d -> MCRExternalStoreUtils.concatPaths(d, path))
            .orElse(path);
    }

    private String removeDirectory(String path) {
        return Optional.ofNullable(settings.directory()).map(d -> {
            final String fixedPath = path.substring(d.length());
            return (fixedPath.startsWith("/")) ? fixedPath.substring(1) : fixedPath;
        }).orElse(path);
    }

}
