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

package org.mycore.externalstore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mycore.access.MCRAccessException;
import org.mycore.access.MCRAccessManager;
import org.mycore.access.MCRRuleAccessInterface;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaClassification;
import org.mycore.datamodel.metadata.MCRMetaIFS;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.externalstore.archive.MCRExternalStoreArchiveResolver;
import org.mycore.externalstore.archive.MCRExternalStoreArchiveResolverFactory;
import org.mycore.externalstore.exception.MCRExternalStoreException;
import org.mycore.externalstore.model.MCRExternalStoreArchiveInfo;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;
import org.mycore.externalstore.model.MCRExternalStoreSettingsWrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This class provides utility methods for {@link MCRExternalStoreService}.
 */
public class MCRExternalStoreServiceHelper {

    private static final ObjectMapper MAPPER = createMapper();

    /**
     * Creates and returns an {@link MCRDerivate} with given classifications for {@link MCRObjectID}.
     *
     * @param objectID object id
     * @param classifications list of classification elements
     * @return derivate
     * @throws MCRPersistenceException if a persistence error occurs
     * @throws MCRAccessException if an access error occurs
     */
    protected static MCRDerivate createDerivate(MCRObjectID objectID, List<MCRMetaClassification> classifications)
        throws MCRPersistenceException, MCRAccessException {
        MCRDerivate derivate = new MCRDerivate();
        derivate
            .setId(MCRMetadataManager.getMCRObjectIDGenerator().getNextFreeId(objectID.getProjectId() + "_derivate"));
        derivate.getDerivate().getClassifications().addAll(classifications);

        final String schema = MCRConfiguration2.getString("MCR.Metadata.Config.derivate")
            .orElse("datamodel-derivate.xml").replaceAll(".xml", ".xsd");
        derivate.setSchema(schema);

        final MCRMetaLinkID linkId = new MCRMetaLinkID();
        linkId.setSubTag("linkmeta");
        linkId.setReference(objectID, null, null);
        derivate.getDerivate().setLinkMeta(linkId);

        final MCRMetaIFS ifs = new MCRMetaIFS();
        ifs.setSubTag("internal");
        ifs.setSourcePath(null);
        derivate.getDerivate().setInternals(ifs);
        return derivate;
    }

    /**
     * Creates and returns a {@link MCRExternalStoreArchiveInfo} by provider and file info.
     *
     * @param provider provider
     * @param fileInfo file info
     * @return archive info
     * @throws MCRExternalStoreException if an error occurs while resolving
     */
    protected static MCRExternalStoreArchiveInfo createArchive(MCRExternalStoreProvider provider,
        MCRExternalStoreFileInfo fileInfo) {
        final Optional<String> resolverId = MCRExternalStoreArchiveResolverFactory.findResolverId(fileInfo.name());
        if (resolverId.isEmpty()) {
            throw new MCRExternalStoreException("there is no matching resolver");
        }
        try {
            final List<MCRExternalStoreFileInfo> fileInfos = resolveArchive(provider, resolverId.get(),
                fileInfo.getAbsolutePath()).stream()
                .peek(a -> a.flags().add(MCRExternalStoreFileInfo.FileFlag.ARCHIVE_ENTRY)).toList();
            return new MCRExternalStoreArchiveInfo(fileInfo.getAbsolutePath(), fileInfos);
        } catch (IOException e) {
            throw new MCRExternalStoreException("error while resolving archive", e);
        }
    }

    private static List<MCRExternalStoreFileInfo> resolveArchive(MCRExternalStoreProvider provider, String resolverId,
        String path) throws IOException {
        final MCRExternalStoreFileContent fileContent = new MCRExternalStoreFileContent(provider, path);
        final MCRExternalStoreArchiveResolver resolver = MCRExternalStoreArchiveResolverFactory
            .createResolver(resolverId, fileContent);
        return resolver.listFileInfos().stream().map(f -> addParentPath(f, path)).toList();
    }

    private static MCRExternalStoreFileInfo addParentPath(MCRExternalStoreFileInfo fileInfo, String path) {
        final String parentPath = Optional.of(fileInfo.parentPath()).filter(p -> !p.isEmpty())
            .map(p -> path.concat("/").concat(p)).orElse(path);
        return new MCRExternalStoreFileInfo.Builder(fileInfo.name(), parentPath).directory(fileInfo.isDirectory())
            .lastModified(fileInfo.lastModified()).flags(fileInfo.flags()).size(fileInfo.size())
            .checksum(fileInfo.checksum()).build();
    }

    /**
     * Sets default permissions to a derivate.
     *
     * @param derivateId derivate id
     */
    protected static void setDefaultPermissions(MCRObjectID derivateId) {
        if (MCRConfiguration2.getBoolean("MCR.Access.AddDerivateDefaultRule").orElse(true)) {
            final MCRRuleAccessInterface aclImpl = MCRAccessManager.getAccessImpl();
            aclImpl.getAccessPermissionsFromConfiguration().forEach(permission -> {
                MCRAccessManager.addRule(derivateId, permission, MCRAccessManager.getTrueRule(),
                    "default derivate rule");
            });
        }
    }

    /**
     * Saves a {@link MCRExternalStoreSettingsWrapper} to given {@link MCRPath}.
     *
     * @param path path
     * @param wrapper wrapper
     * @throws IOException if an I/O error occurs
     */
    protected static void saveSettingsWrapper(MCRPath path, MCRExternalStoreSettingsWrapper wrapper)
        throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            MAPPER.writeValue(outputStream, wrapper);
        }
    }

    /**
     * Saves {@link MCRExternalStoreArchiveInfo} list to given {@link MCRPath}.
     *
     * @param path the path
     * @param archiveInfos the list
     * @throws IOException if an I/O error occurs
     */
    protected static void saveArchiveInfos(MCRPath path, List<MCRExternalStoreArchiveInfo> archiveInfos)
        throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            MAPPER.writeValue(outputStream, archiveInfos);
        }
    }

    /**
     * Saves {@link MCRExternalStoreFileInfo} to {@link MCRPath}.
     *
     * @param path path
     * @param fileInfos file infos
     * @throws IOException if an I/O error occurs
     */
    protected static void saveFileInfos(MCRPath path, List<MCRExternalStoreFileInfo> fileInfos) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            MAPPER.writeValue(outputStream, fileInfos);
        }
    }

    /**
     * Returns a list of {@link MCRExternalStoreFileInfo} elements from {@link InputStream}
     *
     * @param inputStream input stream
     * @return list of file info elements
     * @throws IOException if an I/O error occurs
     */
    protected static List<MCRExternalStoreFileInfo> getFileInfos(InputStream inputStream) throws IOException {
        return Arrays.asList(MAPPER.readerFor(MCRExternalStoreFileInfo[].class).readValue(inputStream));
    }

    /**
     * Returns a list of {@link MCRExternalStoreArchiveInfo} elements from {@link InputStream}
     *
     * @param inputStream input stream
     * @return list of archive info elements
     * @throws IOException if an I/O error occurs
     */
    protected static List<MCRExternalStoreArchiveInfo> getArchiveInfos(InputStream inputStream) throws IOException {
        return Arrays.asList(MAPPER.readerFor(MCRExternalStoreArchiveInfo[].class).readValue(inputStream));
    }

    /**
     * Returns a {@link MCRExternalStoreSettingsWrapper} from given {@link InputStream}.
     *
     * @param inputStream input stream
     * @return wrapper
     * @throws IOException if an I/O error occurs
     */
    protected static MCRExternalStoreSettingsWrapper getSettingsWrapper(InputStream inputStream)
        throws IOException {
        ObjectReader reader = new ObjectMapper().readerFor(MCRExternalStoreSettingsWrapper.class);
        return reader.readValue(inputStream);
    }

    /**
     * Returns an {@link InputStream} for file in derivate specified by file name.
     *
     * @param derivateId derivate id
     * @param fileName file name
     * @return input stream
     * @throws IOException if an I/O error occurs
     */
    protected static InputStream readFile(MCRObjectID derivateId, String fileName) throws IOException {
        final MCRPath path = MCRPath.getPath(derivateId.toString(), fileName);
        return Files.newInputStream(path);
    }

    /**
     * Returns map from string.
     *
     * @param mapString map as string string
     * @return map
     * @throws IllegalArgumentException if input is invalid
     */
    protected static Map<String, String> getMap(String mapString) {
        final ObjectReader reader = new ObjectMapper().readerFor(Map.class);
        try {
            return reader.readValue(mapString);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Returns a map as string.
     * @param map map
     * @return map as string
     * @throws IllegalArgumentException if map is invalid
     */
    protected static String getMapString(Map<String, String> map) {
        try {
            return new ObjectMapper().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static ObjectMapper createMapper() {
        return JsonMapper.builder().addModule(new JavaTimeModule()).serializationInclusion(Include.NON_NULL)
            .addMixIn(MCRExternalStoreFileInfo.class, MCRExternalStoreFileInfoMinxIn.class).build();
    }

    abstract class MCRExternalStoreFileInfoMinxIn {

        @JsonIgnore
        abstract String getAbsolutePath();
    }
}
