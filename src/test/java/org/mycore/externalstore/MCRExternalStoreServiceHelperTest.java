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

import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.mycore.access.MCRAccessBaseImpl;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.MCRStoreTestCase;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

public class MCRExternalStoreServiceHelperTest extends MCRStoreTestCase {

    @Override
    protected Map<String, String> getTestProperties() {
        final Map<String, String> testProperties = super.getTestProperties();
        testProperties.put("MCR.datadir", "%MCR.basedir%/data");
        testProperties
            .put("MCR.Persistence.LinkTable.Store.Class", "org.mycore.backend.hibernate.MCRHIBLinkTableStore");
        testProperties.put("MCR.Access.Class", MCRAccessBaseImpl.class.getName());
        testProperties.put("MCR.Metadata.Type.object", "true");
        testProperties.put("MCR.Metadata.Type.derivate", "true");
        return testProperties;
    }

    @Test
    public void testCreateDerivate() throws MCRPersistenceException, MCRAccessException, MCRActiveLinkException {
        MCRObjectID objectId = createObject();
        final MCRDerivate derivate = MCRExternalStoreServiceHelper.createDerivate(objectId, Collections.emptyList());
        assertEquals(objectId, derivate.getDerivate().getMetaLink().getXLinkHrefID());
    }

    protected static MCRObjectID createObject() throws MCRPersistenceException, MCRAccessException {
        final MCRObjectID objectId = MCRMetadataManager.getMCRObjectIDGenerator().getNextFreeId("mycore_object");
        final MCRObject object = new MCRObject();
        object.setSchema("noSchema");
        object.setId(objectId);
        return objectId;
    }
}
