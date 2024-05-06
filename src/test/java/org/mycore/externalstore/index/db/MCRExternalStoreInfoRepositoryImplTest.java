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

package org.mycore.externalstore.index.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.mycore.common.MCRJPATestCase;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.externalstore.index.db.model.MCRExternalStoreFileInfoData;
import org.mycore.externalstore.index.db.model.MCRExternalStoreInfoData;

public class MCRExternalStoreInfoRepositoryImplTest extends MCRJPATestCase {

    @Override
    protected Map<String, String> getTestProperties() {
        Map<String, String> testProperties = super.getTestProperties();
        testProperties.put("MCR.Metadata.Type.test", Boolean.TRUE.toString());
        testProperties.put("MCR.Metadata.ObjectID.NumberPattern", "00000000");
        return testProperties;
    }

    @Test
    public void testFindExisting() {
        insertStoreInfo(1, "mcr_test_00000001");
        final Optional<MCRExternalStoreInfoData> storeInfo = new MCRExternalStoreInfoRepositoryImpl().find(1);
        assertTrue(storeInfo.isPresent());
        assertEquals(MCRObjectID.getInstance("mcr_test_00000001"), storeInfo.get().getDerivateId());
        assertNotNull(storeInfo.get().getFileInfos());
    }

    @Test
    public void testFindNotExisting() {
        insertStoreInfo(1, "mcr_test_00000001");
        final Optional<MCRExternalStoreInfoData> storeInfo = new MCRExternalStoreInfoRepositoryImpl().find(2);
        assertTrue(storeInfo.isEmpty());
    }

    @Test
    public void testFindWithFile() {
        insertStoreInfo(1, "mcr_test_00000001");
        insertFile(1, "test.txt", 1);
        final Optional<MCRExternalStoreInfoData> storeInfo = new MCRExternalStoreInfoRepositoryImpl().find(1);
        assertTrue(storeInfo.isPresent());
        final List<MCRExternalStoreFileInfoData> files = storeInfo.get().getFileInfos();
        assertNotNull(files);
        assertEquals(1, files.size());
    }

    @Test
    public void testFindByDerivateId() {
        insertStoreInfo(1, "mcr_test_00000001");
        final Optional<MCRExternalStoreInfoData> storeInfo = new MCRExternalStoreInfoRepositoryImpl()
            .findByDerivateId(MCRObjectID.getInstance("mcr_test_00000001"));
        assertTrue(storeInfo.isPresent());
    }

    @Test
    public void testFindAll() {
        insertStoreInfo(1, "mcr_test_00000001");
        insertStoreInfo(2, "mcr_test_00000002");
        final List<MCRExternalStoreInfoData> storeInfos = new MCRExternalStoreInfoRepositoryImpl().listAll();
        assertEquals(2, storeInfos.size());
        assertTrue(storeInfos.stream().map(MCRExternalStoreInfoData::getId).anyMatch(i -> i == 1));
        assertTrue(storeInfos.stream().map(MCRExternalStoreInfoData::getId).anyMatch(i -> i == 2));
    }

    @Test
    public void testInsert() throws SQLException {
        final MCRExternalStoreInfoData storeInfo = new MCRExternalStoreInfoData();
        storeInfo.setDerivateId(MCRObjectID.getInstance("mcr_test_00000001"));
        new MCRExternalStoreInfoRepositoryImpl().insert(storeInfo);
        assertTrue(storeInfo.getId() > 0);
        this.endTransaction();
        assertTrue(new MCRExternalStoreInfoRepositoryImpl().find(storeInfo.getId()).isPresent());
        MCRJPATestCase.executeQuery("SELECT * from \"MCRExternalStoreInfo\"", r -> {
            try {
                assertTrue(r.next());
                assertTrue(r.last());
                assertEquals("mcr_test_00000001", r.getString(2));
            } catch (SQLException e) {
                fail();
            }
        });
    }

    @Test
    public void testInsertWithFile() throws SQLException {
        final MCRExternalStoreInfoData storeInfo = new MCRExternalStoreInfoData();
        storeInfo.setDerivateId(MCRObjectID.getInstance("mcr_test_00000001"));
        final MCRExternalStoreFileInfoData file = new MCRExternalStoreFileInfoData();
        file.setName("test.txt");
        storeInfo.addFileInfo(file);
        new MCRExternalStoreInfoRepositoryImpl().insert(storeInfo);
        this.endTransaction();
        MCRJPATestCase.executeQuery("SELECT * from \"MCRExternalStoreFileInfo\"", r -> {
            try {
                assertTrue(r.next());
                assertTrue(r.last());
                assertEquals("test.txt", r.getString(5));
            } catch (SQLException e) {
                fail();
            }
        });
    }

    @Test
    public void testUpdate() {
        insertStoreInfo(1, "mcr_test_00000001");
        final MCRExternalStoreInfoData storeInfo = new MCRExternalStoreInfoData();
        storeInfo.setId(1l);
        storeInfo.setDerivateId(MCRObjectID.getInstance("mcr_test_00000002"));
        new MCRExternalStoreInfoRepositoryImpl().save(storeInfo);
        this.endTransaction();
        MCRJPATestCase.executeQuery("SELECT * from \"MCRExternalStoreInfo\"", r -> {
            try {
                assertTrue(r.next());
                assertTrue(r.last());
                assertEquals("mcr_test_00000002", r.getString(2));
            } catch (SQLException e) {
                fail();
            }
        });
    }

    @Test
    public void testDelete() {
        insertStoreInfo(1, "mcr_test_00000001");
        final MCRExternalStoreInfoData storeInfo = new MCRExternalStoreInfoData();
        storeInfo.setId(1l);
        storeInfo.setDerivateId(MCRObjectID.getInstance("mcr_test_00000001"));
        new MCRExternalStoreInfoRepositoryImpl().delete(storeInfo);
        this.endTransaction();
        MCRJPATestCase.executeQuery("SELECT * from \"MCRExternalStoreInfo\"", r -> {
            try {
                assertFalse(r.next());
            } catch (SQLException e) {
                fail();
            }
        });
    }

    @Test
    public void testDeleteWithFile() {
        insertStoreInfo(1, "mcr_test_00000001");
        insertFile(1, "test.txt", 1);
        final MCRExternalStoreInfoData storeInfo = new MCRExternalStoreInfoData();
        storeInfo.setId(1l);
        storeInfo.setDerivateId(MCRObjectID.getInstance("mcr_test_00000001"));
        new MCRExternalStoreInfoRepositoryImpl().delete(storeInfo);
        this.endTransaction();
        MCRJPATestCase.executeQuery("SELECT * from \"MCRExternalStoreFileInfo\"", r -> {
            try {
                assertFalse(r.next());
            } catch (SQLException e) {
                fail();
            }
        });
    }

    private void insertFile(long fileId, String name, long storeInfoId) {
        MCRJPATestCase
            .executeUpdate(
                "INSERT INTO \"MCRExternalStoreFileInfo\" (\"file_info_id\",\"name\",\"store_info_id\") VALUES ("
                    + fileId + ",'" + name + "','" + storeInfoId + "')");
    }

    private void insertStoreInfo(long id, String objectId) {
        MCRJPATestCase
            .executeUpdate(
                "INSERT INTO \"MCRExternalStoreInfo\" (\"store_info_id\",\"derivate_id\") VALUES ("
                    + id + ",'" + objectId + "')");
    }
}
