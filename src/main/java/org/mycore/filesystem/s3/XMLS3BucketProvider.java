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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.naming.AuthenticationException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.DOMOutputter;
import org.jdom2.transform.JDOMResult;
import org.mycore.filesystem.FileSystemFromXML;
import org.mycore.filesystem.FileSystemToXML;
import org.mycore.filesystem.FileSystemValidationHelper;
import org.mycore.filesystem.model.BrowsableFile;
import org.mycore.filesystem.model.Directory;
import org.mycore.filesystem.model.File;
import org.mycore.filesystem.model.FileBase;
import org.mycore.filesystem.model.Root;
import org.mycore.filesystem.utils.CompressedDirectoryResolver;
import org.mycore.filesystem.utils.S3SeekableFileChannel;
import org.mycore.filesystem.utils.TarFileDirectoryResolver;
import org.mycore.filesystem.utils.ZipFileDirectoryResolver;
import org.w3c.dom.Node;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.amazonaws.services.s3.model.HeadBucketResult;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class XMLS3BucketProvider implements FileSystemFromXML, FileSystemToXML {

    public static final String ENDPOINT = "endpoint";
    public static final String BUCKET = "bucket";
    public static final String SECRET_KEY = "secretKey";
    public static final String PATH_STYLE_ACCESS = "pathStyleAccess";
    public static final String PROTOCOL = "protocol";
    public static final String ACCESS_KEY = "accessKey";

    public static final String DIRECTORY = "directory";

    private static final Logger LOGGER = LogManager.getLogger();

    public S3BucketSettings getBucketSettings(Element extensionGrandChild) {
        Node bucketSettingsNode;
        try {
            bucketSettingsNode = new DOMOutputter().output(extensionGrandChild);
        } catch (JDOMException e) {
            LOGGER.warn("Error while converting JDOM node to W3Node", e);
            return null;
        }

        return getBucketSettings(bucketSettingsNode);
    }

    protected S3BucketSettings getBucketSettings(Node bucketSettings) {
        JAXBContext context;
        Unmarshaller unmarshaller;

        try {
            context = getJAXBContext();
            unmarshaller = context.createUnmarshaller();
            return (S3BucketSettings) unmarshaller.unmarshal(bucketSettings);
        } catch (JAXBException e) {
            LOGGER.warn("Error while reading S3 Config from XML", e);
            return null;
        }
    }

    private JAXBContext getJAXBContext() throws JAXBException {
        return JAXBContext.newInstance(S3BucketSettings.class);
    }

    @Override
    public void streamFile(Element extensionGrandChild, String path, OutputStream os) throws IOException {
        S3BucketSettings bucketSettings = getBucketSettings(extensionGrandChild);
        AmazonS3 conn = getConnection(bucketSettings);
        S3Object object = conn.getObject(bucketSettings.getBucket(), path);
        object.getObjectContent().transferTo(os);
    }

    @Override
    public Directory getRootDirectory(Element extensionGrandChild) throws IOException {
        return getDirectory(extensionGrandChild, "");
    }

    @Override
    public Directory getDirectory(Element extensionGrandChild, String path) throws IOException {
        S3BucketSettings bucketSettings = getBucketSettings(extensionGrandChild);
        AmazonS3 conn = getConnection(bucketSettings);

        ListObjectsV2Result lor = null;

        final Optional<String> directory = Optional.ofNullable(bucketSettings.getDirectory())
            .filter(p -> !p.equals(""));
        String pathToRoot = directory.map(ptr -> ptr.concat("/")).orElse("");

        String partsToRoot = Optional.ofNullable(path).orElse("");
        String[] parts = partsToRoot.split("/");

        boolean isCompressed = Arrays.stream(parts)
            .anyMatch(p -> p.endsWith(".zip") || p.endsWith("tar.gz") || p.endsWith(".tar"));

        if (!isCompressed) {
            HashMap<String, Directory> directoryMap = new HashMap<>();

            final Directory root = buildRootDirectory(bucketSettings, directory, directoryMap);

            do {
                ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request();
                listObjectsV2Request.setBucketName(bucketSettings.getBucket());
                listObjectsV2Request.setPrefix(pathToRoot + path);

                if (lor != null) {
                    listObjectsV2Request.setContinuationToken(lor.getNextContinuationToken());
                }

                lor = conn.listObjectsV2(listObjectsV2Request);
                processSummaries(lor, pathToRoot, Objects.equals(path, "") ? root : null, path, directoryMap);
            } while (lor.isTruncated());

            return directoryMap.get(path);
        } else {
            // get path to tar
            StringBuilder pathToCompressedFile = new StringBuilder();
            StringBuilder pathAfterCompressedFile = new StringBuilder();

            boolean cfPassed = false;
            String cfPart = null;
            for (String part : parts) {
                if (!cfPassed) {
                    pathToCompressedFile.append(part).append('/');
                } else {
                    pathAfterCompressedFile.append(part).append('/');
                }
                if (part.endsWith(".tar.gz") || part.endsWith(".tar") || part.endsWith(".zip")) {
                    cfPassed = true;
                    cfPart = part;
                }
            }

            directory.ifPresent(dir -> pathToCompressedFile.insert(0, pathToRoot));

            final String fileInBucket = pathToCompressedFile.deleteCharAt(pathToCompressedFile.length() - 1).toString();

            CompressedDirectoryResolver compressedDirectoryResolver = cfPart != null && cfPart.endsWith(".zip")
                ? new ZipFileDirectoryResolver()
                : new TarFileDirectoryResolver();

            final S3SeekableFileChannel s3SeekableFileChannel = new S3SeekableFileChannel(conn,
                bucketSettings.getBucket(), fileInBucket);
            final long time = new Date().getTime();
            final Directory dir = compressedDirectoryResolver.resolveDirectory(s3SeekableFileChannel,
                pathToCompressedFile.substring(pathToRoot.length()),
                        pathAfterCompressedFile.toString());

                final long time2 = new Date().getTime();
                LOGGER.info("ZIP resolution needs {}", time2 - time);
                return dir;

            }

        }

    private Directory buildRootDirectory(S3BucketSettings bucketSettings, Optional<String> directory,
        HashMap<String, Directory> directoryMap) {
        final Directory root = new Root();
        root.setName(directory.orElse(bucketSettings.getBucket()));
        root.setPath("");
        root.setChildren(new ArrayList<>());
        directoryMap.put("", root);
        return root;
    }

    private void processSummaries(ListObjectsV2Result lor, String pathToRoot, Directory parent, String prefix,
        HashMap<String, Directory> lazyDirectoryMap) {
        for (S3ObjectSummary summary : lor.getObjectSummaries()) {
            String filePath = summary.getKey().substring(pathToRoot.length());
            Date lastModified = summary.getLastModified();
            String eTag = summary.getETag();

            List<String> pathParts = getParentFolders(filePath);
            Directory fileParentDirectory = buildDirectories(lazyDirectoryMap, parent, pathParts);

            String fn = getFileName(filePath);

            FileBase file = fn.endsWith(".tar") || fn.endsWith(".tar.gz") || fn.endsWith(".zip") ? new BrowsableFile()
                : new File();
            file.setPath(filePath);
            file.setName(fn);
            file.setSize((int) summary.getSize());
            file.setLastModified(lastModified);
            file.setEtag(eTag);
            fileParentDirectory.getChildren().add(file);
        }
    }

    private Directory buildDirectories(HashMap<String, Directory> lazyDirectoryMap, Directory bucketParent,
        List<String> pathParts) {
        if (lazyDirectoryMap == null) {
            lazyDirectoryMap = new HashMap<>();
        }

        Directory fileParentDirectory = bucketParent;
        StringBuilder pathBuilder = new StringBuilder();
        for (String pathPart : pathParts) {
            pathBuilder.append(pathPart).append('/');
            fileParentDirectory = createDirectory(lazyDirectoryMap, pathBuilder.toString(), fileParentDirectory);
        }
        return fileParentDirectory;
    }

    private Directory createDirectory(HashMap<String, Directory> lazyFolderMap, String path, Directory parent) {
        return lazyFolderMap.computeIfAbsent(path, (_path) -> {
            Directory directory = new Directory();
            directory.setPath(_path);
            directory.setName(getFileName(_path) + "/");
            directory.setChildren(new ArrayList<>());
            if (parent != null) {
                parent.getChildren().add(directory);
            }
            return directory;
        });
    }

    private String getFileName(String filePath) {
        String[] split = filePath.split("/");
        List<String> pathPartsWF = Arrays.asList(split);
        return pathPartsWF.get(pathPartsWF.size() - 1);
    }

    private List<String> getParentFolders(String filePath) {
        String[] split = filePath.split("/");
        return Arrays.stream(split).limit(split.length - 1).collect(Collectors.toList());
    }

    private AmazonS3 getConnection(S3BucketSettings bucketSettings) {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
        builder.setEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(bucketSettings.getEndpoint(),
            bucketSettings.getSigningRegion()));
        builder.withPathStyleAccessEnabled(bucketSettings.isPathStyleAccess());

        AWSCredentials credentials = new BasicAWSCredentials(bucketSettings.getAccessKey(),
            bucketSettings.getSecretKey());
        builder.withCredentials(new AWSStaticCredentialsProvider(credentials));

        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(Protocol.valueOf(bucketSettings.getProtocol().toUpperCase(Locale.ROOT)));
        builder.withClientConfiguration(clientConfig);
        return builder.build();
    }

    @Override
    public boolean test(Element extensionGrandChild) throws AuthenticationException {
        S3BucketSettings bucketSettings = getBucketSettings(extensionGrandChild);
        return test(bucketSettings);
    }

    private boolean test(S3BucketSettings bucketSettings) throws AuthenticationException {
        AmazonS3 conn = getConnection(bucketSettings);
        try {
            HeadBucketResult headBucketResult = conn.headBucket(new HeadBucketRequest(bucketSettings.getBucket()));
        } catch (AmazonServiceException ase) {
            LOGGER.warn("Bucket head request failed! ", ase);

            if (ase.getStatusCode() == 403) {
                throw new AuthenticationException(ase.getErrorMessage());
            }

            return false; // bucket does not exist or authentication is wrong
        }

        return true;
    }

    @Override
    public Element getElement(Map<String, String> settingsObject)
        throws JAXBException, AuthenticationException, IOException {
        if (settingsObject.containsKey(ENDPOINT) && settingsObject.containsKey(BUCKET)
            && settingsObject.containsKey(PROTOCOL) && settingsObject.containsKey(SECRET_KEY)
            && settingsObject.containsKey(ACCESS_KEY) && settingsObject.containsKey(PATH_STYLE_ACCESS)) {

            S3BucketSettings bucketSettings = new S3BucketSettings();
            bucketSettings.setBucket(settingsObject.get(BUCKET));
            bucketSettings.setProtocol(settingsObject.get(PROTOCOL));
            bucketSettings.setEndpoint(settingsObject.get(ENDPOINT));
            bucketSettings.setAccessKey(settingsObject.get(ACCESS_KEY));
            bucketSettings.setSecretKey(settingsObject.get(SECRET_KEY));
            bucketSettings.setPathStyleAccess(Boolean.parseBoolean(settingsObject.get(PATH_STYLE_ACCESS)));

            if (settingsObject.containsKey(DIRECTORY)) {
                bucketSettings.setDirectory(settingsObject.get(DIRECTORY));
            }

            if (test(bucketSettings)) {
                JDOMResult result = new JDOMResult();
                getJAXBContext().createMarshaller().marshal(bucketSettings, result);
                final Element extension = result.getDocument().detachRootElement();

                final Map<String, String> map = FileSystemValidationHelper.getSimpleValidationMap(this, extension);
                final Element validation = new Element("validation");
                extension.addContent(validation);

                map.forEach((key,val )-> {
                    final Element file = new Element("file");
                    validation.addContent(file);

                    file.setAttribute("path", key);
                    file.setAttribute("checksum", val);
                });

                return extension;
            }
        }
        return null;
    }

}
