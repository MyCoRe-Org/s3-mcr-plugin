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
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.externalstore.archive.MCRExternalStoreArchiveResolver;
import org.mycore.externalstore.archive.MCRExternalStoreArchiveResolverFactory;
import org.mycore.externalstore.exception.MCRExternalStoreException;
import org.mycore.externalstore.model.MCRExternalStoreArchiveInfo;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;
import org.mycore.externalstore.model.MCRExternalStoreRawSettingsWrapper;
import org.mycore.externalstore.util.MCRExternalStoreUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This class provides utility methods for {@link MCRExternalStoreConfigService}.
 */
public class MCRExternalStoreServiceHelper {

    private static final ObjectMapper mapper = createMapper();

    /**
     * Creates {@link MCRDerivate} with given classifications and links it to specified {@link MCRObjectID}.
     *
     * @param objectID object id
     * @param classifications classification list
     * @return derivate
     * @throws MCRPersistenceException if there was an persistence problem
     * @throws MCRAccessException if there was an access problem
     */
    protected static MCRDerivate createDerivate(MCRObjectID objectID, List<MCRMetaClassification> classifications)
        throws MCRPersistenceException, MCRAccessException {
        MCRDerivate derivate = new MCRDerivate();
        derivate.setId(MCRObjectID.getNextFreeId(objectID.getProjectId() + "_derivate"));
        derivate.getDerivate().getClassifications().addAll(classifications);

        final String schema
            = MCRConfiguration2.getString("MCR.Metadata.Config.derivate").orElse("datamodel-derivate.xml")
                .replaceAll(".xml", ".xsd");
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
     * Resolves and creates {@link MCRExternalStoreArchiveInfo} by provider and file info.
     *
     * @param provider provider
     * @param fileInfo file info
     * @return archive info
     * @throws MCRExternalStoreException if error occurs while resolving
     */
    protected static MCRExternalStoreArchiveInfo createArchive(MCRExternalStoreProvider provider,
        MCRExternalStoreFileInfo fileInfo) {
        if (fileInfo.isDirectory()) {
            throw new MCRExternalStoreException("directory is not allowed");
        }
        if (fileInfo.getSize() > MCRExternalStoreConstants.MAX_DOWNLOAD_SIZE) {
            throw new MCRExternalStoreException("file is too large");
        }
        final Optional<String> resolverId = MCRExternalStoreArchiveResolverFactory.findResolverId(fileInfo.getName());
        if (resolverId.isEmpty()) {
            throw new MCRExternalStoreException("there is no matching resolver");
        }
        MCRExternalStoreArchiveInfo archive = new MCRExternalStoreArchiveInfo(fileInfo.getAbsolutePath());
        try {
            resolveArchive(provider, resolverId.get(), fileInfo.getAbsolutePath()).stream()
                .peek(a -> a.getFlags().add(MCRExternalStoreFileInfo.FileFlag.ARCHIVE_ENTRY))
                .forEach(archive::addFile);
        } catch (IOException e) {
            throw new MCRExternalStoreException("error while resolving archive");
        }
        return archive;
    }

    private static List<MCRExternalStoreFileInfo> resolveArchive(MCRExternalStoreProvider provider, String resolverId,
        String path) throws IOException {
        final MCRExternalStoreFileContent fileContent = new MCRExternalStoreFileContent(provider, path);
        final MCRExternalStoreArchiveResolver resolver
            = MCRExternalStoreArchiveResolverFactory.createResolver(resolverId, fileContent);
        return resolver.listFileInfos().stream().peek(f -> MCRExternalStoreUtils.addParentPath(f, path)).toList();
    }

    /**
     * Sets default permissions to derivate by given id.
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
     * Stores {@link MCRExternalStoreRawSettingsWrapper} to given {@link MCRPath}.
     *
     * @param path path
     * @param wrapper wrapper
     * @throws MCRExternalStoreException if cannot store
     */
    protected static void saveRawSettingsWrapper(MCRPath path, MCRExternalStoreRawSettingsWrapper wrapper) {
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            mapper.writeValue(outputStream, wrapper);
        } catch (Exception e) {
            throw new MCRExternalStoreException("Error while saving settings", e);
        }
    }

    /**
     * Saves {@link MCRExternalStoreArchiveInfo} list to given {@link MCRPath}.
     *
     * @param path the path
     * @param archives the list
     */
    protected static void saveArchiveInfos(MCRPath path, List<MCRExternalStoreArchiveInfo> archiveInfos) {
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            mapper.writeValue(outputStream, archiveInfos);
        } catch (Exception e) {
            throw new MCRExternalStoreException("Error while saving settings", e);
        }
    }

    /**
     * Saves {@link MCRExternalStoreFileInfo} to {@link MCRPath}.
     *
     * @param path path
     * @param fileInfos file infos
     * @throws MCRExternalStoreException error while saving
     */
    protected static void saveFileInfos(MCRPath path, List<MCRExternalStoreFileInfo> fileInfos) {
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            mapper.writeValue(outputStream, fileInfos);
        } catch (Exception e) {
            throw new MCRExternalStoreException("Error while saving file infos", e);
        }
    }

    /**
     * Returns {@link MCRExternalStoreFileInfo} from {@link InputStream}
     *
     * @param inputStream input stream
     * @return file infos
     */
    protected static List<MCRExternalStoreFileInfo> getFileInfos(InputStream inputStream) {
        try {
            return Arrays.asList(mapper.readerFor(MCRExternalStoreFileInfo[].class).readValue(inputStream));
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot map to file infos", e);
        }
    }

    /**
     * Returns {@link MCRExternalStoreArchiveInfo} from {@link InputStream}
     *
     * @param inputStream input stream
     * @return archive infos
     */
    protected static List<MCRExternalStoreArchiveInfo> getArchiveInfos(InputStream inputStream) {
        try {
            return Arrays.asList(mapper.readerFor(MCRExternalStoreArchiveInfo[].class).readValue(inputStream));
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot map to archive info", e);
        }
    }

    /**
     * Gets {@link MCRExternalStoreRawSettingsWrapper} from given {@link InputStream}.
     *
     * @param inputStream input stream
     * @return wrapper
     * @throws IOException if cannot read or wrap
     */
    protected static MCRExternalStoreRawSettingsWrapper getRawSettingsWrapper(InputStream inputStream)
        throws IOException {
        ObjectReader reader = new ObjectMapper().readerFor(MCRExternalStoreRawSettingsWrapper.class);
        return reader.readValue(inputStream);
    }

    /**
     * Creates {@link InputStream} for file in derivate specified by id and file name.
     *
     * @param derivateId derivate id
     * @param fileName file name
     * @return input stream
     * @throws IOException cannot read
     */
    protected static InputStream readFile(MCRObjectID derivateId, String fileName) throws IOException {
        final MCRPath path = MCRPath.getPath(derivateId.toString(), fileName);
        return Files.newInputStream(path);
    }

    private static ObjectMapper createMapper() {
        return JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .serializationInclusion(Include.NON_NULL)
            .addMixIn(MCRExternalStoreFileInfo.class, MCRExternalStoreFileInfoMinxIn.class)
            .build();
    }

    abstract class MCRExternalStoreFileInfoMinxIn {
        @JsonIgnore
        abstract Long getId();

        @JsonIgnore
        abstract String getAbsolutePath();
    }
}
