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
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
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

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;

/**
 * An {@link MCRExternalStoreProvider} for an s3 bucket.
 */
public class MCRExternalStoreS3Provider implements MCRExternalStoreProvider {

    private static final Logger LOGGER = LogManager.getLogger();

    private AmazonS3 client;

    private String bucket;

    private Optional<String> directory;

    @Override
    public void init(Map<String, String> settings) {
        final MCRS3Settings s3Settings = MCRExternalStoreS3ProviderUtils.mapToS3Settings(settings);
        this.client = MCRExternalStoreS3ProviderHelper.createClient(s3Settings);
        this.bucket = s3Settings.getBucket();
        directory = Optional.ofNullable(s3Settings.getDirectory());
    }

    @Override
    public InputStream newInputStream(String path) throws IOException {
        return client.getObject(bucket, getKey(path)).getObjectContent();
    }

    @Override
    public SeekableByteChannel newByteChannel(String path) throws IOException {
        return new MCRS3SeekableFileChannel(client, bucket, getKey(path));
    }

    @Override
    public MCRExternalStoreFileInfo getFileInfo(String path) {
        final ObjectMetadata metadata = getObjectMetadata(path);
        final String parentPath = MCRExternalStoreUtils.getParentPath(path);
        final String name = MCRExternalStoreUtils.getFileName(path);
        return new MCRExternalStoreFileInfo.MCRExternalStoreFileInfoBuilder(name, parentPath)
            .checksum(metadata.getETag())
            .size(metadata.getContentLength())
            .lastModified(metadata.getLastModified())
            .directory(false)
            .build();
    }

    @Override
    public List<MCRExternalStoreFileInfo> listFileInfos(String path) throws IOException {
        final ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request()
            .withBucketName(bucket)
            .withPrefix(getKey(path))
            .withDelimiter("/");
        return list(listObjectsV2Request);
    }

    @Override
    public List<MCRExternalStoreFileInfo> listFileInfosRecursive(String path) throws IOException {
        final ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request()
            .withBucketName(bucket)
            .withPrefix(getKey(path));
        final List<MCRExternalStoreFileInfo> result = list(listObjectsV2Request);
        final Set<MCRExternalStoreFileInfo> directories = new HashSet<MCRExternalStoreFileInfo>();
        for (MCRExternalStoreFileInfo file : result) {
            directories.addAll(MCRExternalStoreUtils.getDirectories(file.getParentPath()));
        }
        result.addAll(directories);
        return result;

    }

    @Override
    public void ensureReadAccess() {
        try {
            client.headBucket(new HeadBucketRequest(bucket));
        } catch (AmazonServiceException amazonServiceException) {
            LOGGER.warn("Bucket head request failed", amazonServiceException);
            if (amazonServiceException.getStatusCode() == 403) {
                throw new MCRExternalStoreNoAccessException(amazonServiceException.getErrorMessage());
            }
            throw new MCRExternalStoreNoAccessException("Test failed.");
        }
    }

    private List<MCRExternalStoreFileInfo> list(ListObjectsV2Request listObjectsV2Request) throws IOException {
        ListObjectsV2Result listObjectsV2Result = null;
        final List<MCRExternalStoreFileInfo> fileInfos = new ArrayList<>();
        do {
            if (listObjectsV2Result != null) {
                listObjectsV2Request.setContinuationToken(listObjectsV2Result.getNextContinuationToken());
            }
            listObjectsV2Result = client.listObjectsV2(listObjectsV2Request);
            listObjectsV2Result.getObjectSummaries().stream()
                .map(s -> MCRExternalStoreS3ProviderHelper.mapToFileInfos(s)).forEach(fileInfos::add);
            listObjectsV2Result.getCommonPrefixes().stream().map(p -> MCRExternalStoreUtils.createDirectory(p))
                .forEach(fileInfos::add);
        } while (listObjectsV2Result.isTruncated());
        fileInfos.forEach(f -> fixDirectory(f));
        return fileInfos;
    }

    private ObjectMetadata getObjectMetadata(String path) {
        return client.getObjectMetadata(bucket, getKey(path));
    }

    private String getKey(String path) {
        return (directory.isPresent()) ? MCRExternalStoreUtils.concatPaths(directory.get(), path) : path;
    }

    private void fixDirectory(MCRExternalStoreFileInfo fileInfo) {
        fileInfo.setParentPath(removeDirectory(fileInfo.getParentPath()));
    }

    private String removeDirectory(String path) {
        if (directory.isPresent()) {
            final String fixedPath = path.substring(directory.get().length());
            return (fixedPath.startsWith("/")) ? fixedPath.substring(1) : fixedPath;
        }
        return path;
    }

}
