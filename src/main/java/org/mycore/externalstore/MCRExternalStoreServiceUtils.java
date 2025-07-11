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

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaClassification;
import org.mycore.datamodel.metadata.MCRMetaLink;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.externalstore.exception.MCRExternalStoreException;
import org.mycore.externalstore.exception.MCRExternalStoreNotExistsException;

/**
 * This class provides utility methods for {@link MCRExternalStoreService}.
 */
public class MCRExternalStoreServiceUtils {

    /**
     * Checks if {@link MCRMetaClassification} is a store service classification.
     *
     * @param clazz classification
     * @return true if classification is a store service classification
     */
    public static boolean checkExternalStoreClassification(MCRMetaClassification clazz) {
        return Objects.equals(MCRExternalStoreService.CLASSIFICATION_ID, clazz.getClassId())
            && clazz.getCategId().startsWith(MCRExternalStoreService.CLASSIFICATION_CATEGORY_ID_PREFIX);
    }

    /**
     * Checks if {@link MCRCategoryID} is a store service category.
     *
     * @param categoryId classification
     * @return true if category is a store service category
     */
    public static boolean checkExternalStoreClassification(MCRCategoryID categoryId) {
        return Objects.equals(MCRExternalStoreService.CLASSIFICATION_ID, categoryId.getRootID())
            && categoryId.getId().startsWith(MCRExternalStoreService.CLASSIFICATION_CATEGORY_ID_PREFIX);
    }

    /**
     * Returns a list of derivate id elements which are external store classified for an object.
     *
     * @param object object
     * @return list of derivate id elements
     */
    public static List<String> listExternalStoreDerivateIds(MCRObject object) {
        return object.getStructure().getDerivates().stream()
            .filter(der -> der.getClassifications().stream()
                .anyMatch(MCRExternalStoreServiceUtils::checkExternalStoreClassification))
            .map(MCRMetaLink::getXLinkHref).toList();
    }

    /**
     * Returns the store type of an {@link MCRDerivate}.
     *
     * @param derivate derivate
     * @return store type
     * @throws MCRExternalStoreException if store type cannot be determined
     */
    public static String getStoreType(MCRDerivate derivate) {
        final List<String> types
            = derivate.getDerivate().getClassifications().stream().map(MCRMetaClassification::getCategId).toList();
        if (types.isEmpty()) {
            throw new MCRExternalStoreNotExistsException("Type does not exist");
        } else if (types.size() > 1) {
            throw new MCRExternalStoreException("Type is not unique");
        } else {
            return types.getFirst().substring(MCRExternalStoreService.CLASSIFICATION_CATEGORY_ID_PREFIX.length());
        }
    }

    /**
     * Returns the store type of a {@link MCRDerivate}.
     *
     * @param derivateId derivate id
     * @return store type
     * @throws MCRExternalStoreNotExistsException if store type cannot be determined
     */
    public static String getStoreType(MCRObjectID derivateId) {
        if (!MCRMetadataManager.exists(derivateId)) {
            throw new MCRExternalStoreNotExistsException("Derivate does not exist");
        }
        return getStoreType(MCRMetadataManager.retrieveMCRDerivate(derivateId));
    }

    /**
     * Checks if file is file infos file.
     *
     * @param file the file
     * @return true if file is info file
     */
    public static boolean checkPathIsFileInfosFile(Path file) {
        return checkFileName(file, MCRExternalStoreService.FILE_INFOS_FILENAME);
    }

    /**
     * Checks if file is archives file.
     *
     * @param file the file
     * @return true if file is archives file
     */
    public static boolean checkPathIsArchiveInfoFile(Path file) {
        return checkFileName(file, MCRExternalStoreService.ARCHIVE_INFOS_FILENAME);
    }

    private static boolean checkFileName(Path file, String fileName) {
        return file instanceof MCRPath && Objects.equals(fileName, file.getFileName().toString());
    }
}
