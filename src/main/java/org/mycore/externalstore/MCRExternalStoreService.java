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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRCache;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.crypt.MCRCipher;
import org.mycore.crypt.MCRCipherManager;
import org.mycore.crypt.MCRCryptKeyFileNotFoundException;
import org.mycore.crypt.MCRCryptKeyNoPermissionException;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaClassification;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.externalstore.archive.MCRExternalStoreArchiveResolverFactory;
import org.mycore.externalstore.exception.MCRExternalStoreException;
import org.mycore.externalstore.model.MCRExternalStoreArchiveInfo;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;
import org.mycore.externalstore.model.MCRExternalStoreSettingsWrapper;
import org.mycore.services.queuedjob.MCRJob;
import org.mycore.services.queuedjob.MCRJobQueue;

/**
 * This service manages stores and can store and provide info about anyone.
 */
public class MCRExternalStoreService {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String PROPERTY_PEFIX = "MCR.ExternalStore.Service.";

    /**
     * Store provider settings filename.
     */
    protected static final String STORE_PROVIDER_SETTINGS_FILENAME = "provider_settings.json";

    /**
     * File infos filename.
     */
    protected static final String FILE_INFOS_FILENAME = "files.json";

    /**
     * Archive infos filename.
     */
    protected static final String ARCHIVE_INFOS_FILENAME = "archives.json";

    /**
     * Derivate types classification id.
     */
    protected static String CLASSIFICATION_ID = "derivate_types";

    /**
     * External store classification id prefix.
     */
    protected static String CLASSIFICATION_CATEGORY_ID_PREFIX = "external_store_";

    private static final MCRCache<String, MCRExternalStore> STORE_CACHE = new MCRCache<>(100, "MCRExternalStore cache");

    private static final MCRJobQueue CREATE_STORE_INFO_QUEUE = MCRJobQueue
        .getInstance(MCRExternalStoreCreateInfoJobAction.class);

    private MCRExternalStoreService() {
    }

    /**
     * Returns service instance.
     *
     * @return service
     */
    public static MCRExternalStoreService getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Creates and saves info over an {@link MCRExternalStore} to an object.
     *
     * @param objectId object id
     * @param storeType store type
     * @param storeProviderSettings map over provider store setting elements
     * @throws MCRExternalStoreException if an error while creating store info occurs
     */
    public static void createStore(MCRObjectID objectId, String storeType, Map<String, String> storeProviderSettings) {
        testStoreProviderSettings(storeType, storeProviderSettings);
        MCRObjectID derivateId;
        try {
            derivateId = createStoreDerivate(objectId, storeType);
        } catch (MCRAccessException | MCRException e) {
            throw new MCRExternalStoreException("Error while creating store", e);
        }
        try {
            saveStoreProviderSettings(derivateId, storeProviderSettings);
        } catch (IOException e) {
            throw new MCRExternalStoreException("Error while saving store provider settings", e);
        }
        enqueueCreateStoreJob(derivateId);
    }

    private static void testStoreProviderSettings(String storeType, Map<String, String> storeProviderSettings) {
        MCRExternalStoreProviderFactory.createStoreProvider(storeType, storeProviderSettings).ensureReadAccess();
    }

    private static MCRObjectID createStoreDerivate(MCRObjectID objectId, String storeType)
        throws MCRPersistenceException, MCRAccessException, MCRException {
        final MCRDerivate derivate = MCRExternalStoreServiceHelper.createDerivate(objectId,
            Stream
                .of(new MCRMetaClassification("classification", 0, null,
                    new MCRCategoryID(CLASSIFICATION_ID, CLASSIFICATION_CATEGORY_ID_PREFIX + storeType)))
                .collect(Collectors.toList()));
        MCRMetadataManager.create(derivate);
        MCRExternalStoreServiceHelper.setDefaultPermissions(derivate.getId());
        MCRMetadataManager.update(derivate);
        return derivate.getId();
    }

    /**
     * Encrypts and saves a map over store provider setting elements to derivate.
     *
     * @param derivateId the derivate id
     * @param storeProviderSettings map over store provider setting elements
     * @throws IOException if an I/O error occurs
     */
    protected static void saveStoreProviderSettings(MCRObjectID derivateId, Map<String, String> storeProviderSettings)
        throws IOException {
        MCRExternalStoreSettingsWrapper wrapper;
        try {
            final String settingsMapString = MCRExternalStoreServiceHelper.getMapString(storeProviderSettings);
            final String encryptedSettings = encryptString(settingsMapString);
            wrapper = new MCRExternalStoreSettingsWrapper(encryptedSettings, true);
        } catch (MCRCryptKeyFileNotFoundException | MCRCryptKeyNoPermissionException | IllegalArgumentException e) {
            throw new IOException("Error while processing settings", e);
        }
        final MCRPath path = MCRPath.getPath(derivateId.toString(), STORE_PROVIDER_SETTINGS_FILENAME);
        MCRExternalStoreServiceHelper.saveSettingsWrapper(path, wrapper);
    }

    private static void enqueueCreateStoreJob(MCRObjectID derivateId) {
        final MCRJob addJob = MCRExternalStoreCreateInfoJobAction.createJob(derivateId);
        CREATE_STORE_INFO_QUEUE.add(addJob);
    }

    /**
     * Creates and saves info over an {@link MCRExternalStore} to derivate.
     *
     * @param derivateId derivate id
     * @throws IOException if an I/O error occurs
     */
    protected static void createStoreInfo(MCRObjectID derivateId) throws IOException {
        final MCRExternalStoreProvider storeProvider = loadStoreProvider(derivateId);
        final List<MCRExternalStoreFileInfo> fileInfos = storeProvider.listFileInfosRecursive("");
        saveFileInfos(derivateId, fileInfos);
        if (checkResolveArchives()) {
            final List<MCRExternalStoreArchiveInfo> archives = new ArrayList<>();
            fileInfos.stream().filter(f -> !f.isDirectory())
                .filter(f -> MCRExternalStoreArchiveResolverFactory.checkResolvable(f.name())).forEach(f -> {
                    try {
                        final MCRExternalStoreArchiveInfo archive = MCRExternalStoreServiceHelper
                            .createArchive(storeProvider, f);
                        archives.add(archive);
                    } catch (MCRExternalStoreException e) {
                        LOGGER.warn(e);
                    }
                });
            saveArchiveInfos(derivateId, archives);
        }
    }

    private static MCRExternalStoreProvider loadStoreProvider(MCRObjectID derivateId) {
        final String storeType = MCRExternalStoreServiceUtils.getStoreType(derivateId);
        final Map<String, String> storeProviderSettings = loadStoreProviderSettings(derivateId);
        return MCRExternalStoreProviderFactory.createStoreProvider(storeType, storeProviderSettings);
    }

    private static boolean checkResolveArchives() {
        return MCRConfiguration2.getBoolean(PROPERTY_PEFIX + "ResolveArchives").orElseThrow();
    }

    /**
     * Saves a list of {@link MCRExternalStoreFileInfo} elements to derivate.
     *
     * @param derivateId derivate id
     * @param fileInfos list of file info elements
     * @throws IOException if an I/O error occurs
     */
    protected static void saveFileInfos(MCRObjectID derivateId, List<MCRExternalStoreFileInfo> fileInfos)
        throws IOException {
        final MCRPath path = MCRPath.getPath(derivateId.toString(), FILE_INFOS_FILENAME);
        MCRExternalStoreServiceHelper.saveFileInfos(path, fileInfos);
    }

    /**
     * Saves a list of {@link MCRExternalStoreArchiveInfo} elements to derivate.
     *
     * @param derivateId derivate id
     * @param archiveInfos list over archive info elements
     * @throws IOException if an I/O error occurs
     */
    protected static void saveArchiveInfos(MCRObjectID derivateId, List<MCRExternalStoreArchiveInfo> archiveInfos)
        throws IOException {
        final MCRPath path = MCRPath.getPath(derivateId.toString(), ARCHIVE_INFOS_FILENAME);
        MCRExternalStoreServiceHelper.saveArchiveInfos(path, archiveInfos);
    }

    /**
     * Returns an {@link MCRExternalStore} by derivate id.
     *
     * @param derivateId derivate id
     * @return external store
     * @throws MCRExternalStoreException if an error occurs while loading store
     */
    public MCRExternalStore getStore(MCRObjectID derivateId) {
        MCRExternalStore store = STORE_CACHE.get(derivateId.toString());
        if (store == null) {
            synchronized (STORE_CACHE) {
                store = STORE_CACHE.get(derivateId.toString());
                if (store == null) {
                    store = loadStore(derivateId);
                    STORE_CACHE.put(derivateId.toString(), store);
                }
            }
        }
        return store;
    }

    private MCRExternalStore loadStore(MCRObjectID derivateId) {
        if (!MCRMetadataManager.exists(derivateId)) {
            throw new MCRExternalStoreException("Store does not exist");
        }
        final String storeType = MCRExternalStoreServiceUtils.getStoreType(derivateId);
        final Map<String, String> storeProviderSettings = loadStoreProviderSettings(derivateId);
        return new MCRExternalStore(storeType, storeProviderSettings);
    }

    private static Map<String, String> loadStoreProviderSettings(MCRObjectID derivateId) {
        final MCRExternalStoreSettingsWrapper settingsWrapper = loadSettingsWrapper(derivateId);
        try {
            final String settingsMapString = decryptString(settingsWrapper.settings());
            return MCRExternalStoreServiceHelper.getMap(settingsMapString);
        } catch (IOException | MCRCryptKeyNoPermissionException | IllegalArgumentException e) {
            throw new MCRExternalStoreException("Error while processing settings wrapper", e);
        }
    }

    private static MCRExternalStoreSettingsWrapper loadSettingsWrapper(MCRObjectID derivateId) {
        try (InputStream inputStream = MCRExternalStoreServiceHelper.readFile(derivateId,
            STORE_PROVIDER_SETTINGS_FILENAME)) {
            return MCRExternalStoreServiceHelper.getSettingsWrapper(inputStream);
        } catch (IOException e) {
            throw new MCRExternalStoreException("Error while reading settings file", e);
        }
    }

    private static String encryptString(String input)
        throws MCRCryptKeyNoPermissionException, MCRCryptKeyFileNotFoundException {
        final MCRCipher chiper = MCRCipherManager.getCipher(getChiperFilePath());
        return chiper.encrypt(input);
    }

    private static String decryptString(String input)
        throws MCRCryptKeyNoPermissionException, MCRCryptKeyFileNotFoundException {
        final MCRCipher cipher = MCRCipherManager.getCipher(getChiperFilePath());
        return cipher.decrypt(input);
    }

    private static String getChiperFilePath() {
        return MCRConfiguration2.getStringOrThrow(PROPERTY_PEFIX + "StoreProviderSettings.Cipher");
    }

    /**
     * Deletes store/derivate.
     *
     * @param derivateId derivate id
     */
    public void deleteStore(MCRObjectID derivateId) {
        try {
            MCRMetadataManager.deleteMCRDerivate(derivateId);
        } catch (MCRAccessException e) {
            throw new MCRExternalStoreException("Error while deleting store", e);
        }
        STORE_CACHE.remove(derivateId.toString());
    }

    private static class InstanceHolder {
        private static final MCRExternalStoreService INSTANCE = new MCRExternalStoreService();
    }

}
