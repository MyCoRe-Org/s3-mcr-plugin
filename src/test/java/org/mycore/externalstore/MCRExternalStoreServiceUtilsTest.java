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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mycore.access.MCRAccessBaseImpl;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.MCRStoreTestCase;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaClassification;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.externalstore.exception.MCRExternalStoreException;

public class MCRExternalStoreServiceUtilsTest extends MCRStoreTestCase {

    @Override
    protected Map<String, String> getTestProperties() {
        final Map<String, String> testProperties = super.getTestProperties();
        testProperties.put("MCR.datadir", "%MCR.basedir%/data");
        testProperties
            .put("MCR.Persistence.LinkTable.Store.Class", "org.mycore.backend.hibernate.MCRHIBLinkTableStore");
        testProperties.put("MCR.Access.Class", MCRAccessBaseImpl.class.getName());
        testProperties.put("MCR.Metadata.Type.object", "true");
        testProperties.put("MCR.Metadata.Type.derivate", "true");
        testProperties.put("MCR.IFS.ContentStore.IFS2.BaseDir", "%MCR.datadir%/content");
        return testProperties;
    }

    @Test
    public void testCheckPathIsInformationFile_noMCRPath() {
        final Path path = Paths.get("test");
        assertFalse(MCRExternalStoreServiceUtils.checkPathIsFileInfosFile(path));
    }

    @Test
    public void testCheckPathIsInformationFile_noCorrectFileName() {
        final MCRPath path = MCRPath.getPath("derivate", "foo.txt");
        assertFalse(MCRExternalStoreServiceUtils.checkPathIsFileInfosFile(path));
    }

    @Test(expected = MCRExternalStoreException.class)
    public void testGetStoreType_noTypeExists() throws MCRPersistenceException, MCRAccessException {
        MCRDerivate derivate = createDerivate(Collections.emptyList());
        MCRExternalStoreServiceUtils.getStoreType(derivate);
    }

    @Test
    public void testGetStoreType() throws MCRPersistenceException, MCRAccessException, MCRException {
        MCRDerivate derivate
            = createDerivate(Arrays
                .asList(new MCRMetaClassification("classification", 0, null,
                    MCRExternalStoreService.CLASSIFICATION_ID,
                    MCRExternalStoreService.CLASSIFICATION_CATEGORY_ID_PREFIX + "test")));
        final String type = MCRExternalStoreServiceUtils.getStoreType(derivate);
        assertEquals("test", type);
    }

    private MCRDerivate createDerivate(List<MCRMetaClassification> classifications)
        throws MCRPersistenceException, MCRAccessException {
        MCRObjectID objectId = MCRExternalStoreServiceHelperTest.createObject();
        return MCRExternalStoreServiceHelper.createDerivate(objectId, classifications);
    }
}
