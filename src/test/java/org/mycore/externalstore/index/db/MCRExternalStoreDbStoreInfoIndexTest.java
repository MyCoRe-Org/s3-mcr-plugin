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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.mycore.common.MCRJPATestCase;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.externalstore.index.db.model.MCRExternalStoreFileInfoData;
import org.mycore.externalstore.index.db.model.MCRExternalStoreInfoData;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;

public class MCRExternalStoreDbStoreInfoIndexTest extends MCRJPATestCase {

    private MCRExternalStoreInfoRepository storeInfoRepository;

    private MCRExternalStoreDbStoreInfoIndex storeIndex;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        storeInfoRepository = new MCRExternalStoreInfoRepositoryImpl();
        storeIndex = new MCRExternalStoreDbStoreInfoIndex();
    }

    @Override
    protected Map<String, String> getTestProperties() {
        Map<String, String> testProperties = super.getTestProperties();
        testProperties.put("MCR.Metadata.Type.test", Boolean.TRUE.toString());
        return testProperties;
    }

    @Test
    public void testListDirectory() {
        final MCRObjectID derivateId = MCRObjectID.getInstance("mcr_test_00000001");
        final MCRExternalStoreInfoData information = createTestInfoData(derivateId);
        storeInfoRepository.insert(information);
        this.startNewTransaction();
        final List<MCRExternalStoreFileInfo> rootFiles = storeIndex.listFileInfos(derivateId, "");
        assertNotNull(rootFiles);
        assertEquals(3, rootFiles.size());
        final List<MCRExternalStoreFileInfo> emptyList = storeIndex.listFileInfos(derivateId, "null");
        assertNotNull(emptyList);
        assertTrue(emptyList.isEmpty());
        final List<MCRExternalStoreFileInfo> fooFiles = storeIndex.listFileInfos(derivateId, "foo");
        assertNotNull(fooFiles);
        assertEquals(1, fooFiles.size());
        assertEquals("test.txt", fooFiles.get(0).name());
        assertEquals("foo", fooFiles.get(0).parentPath());
    }

    @Test
    public void testAddFileInfo() {
        final MCRObjectID derivateId = MCRObjectID.getInstance("mcr_test_00000001");
        final MCRExternalStoreInfoData storeInfoData = createTestInfoData(derivateId);
        storeInfoRepository.insert(storeInfoData);
        this.startNewTransaction();
        final MCRExternalStoreFileInfo fileInfo = new MCRExternalStoreFileInfo.Builder("baz", "stu").build();
        storeIndex.addFileInfo(derivateId, fileInfo);
        final Optional<MCRExternalStoreFileInfo> result = storeIndex.findFileInfo(derivateId, "stu/baz");
        assertTrue(result.isPresent());
    }

    @Test
    public void testUpdateFileInfo() {
        final MCRObjectID derivateId = MCRObjectID.getInstance("mcr_test_00000001");
        final MCRExternalStoreInfoData storeInfoData = createTestInfoData(derivateId);
        storeInfoRepository.insert(storeInfoData);
        this.startNewTransaction();
        final MCRExternalStoreFileInfoData updatedFileInfoData = storeInfoData.getFileInfos().get(0);
        final MCRExternalStoreFileInfo updatedFileInfo
            = new MCRExternalStoreFileInfo.Builder(updatedFileInfoData.getName(), updatedFileInfoData.getParentPath())
                .flags(Set.of(MCRExternalStoreFileInfo.FileFlag.ARCHIVE)).build();
        storeIndex.updateFileInfo(derivateId, updatedFileInfo);
        final Optional<MCRExternalStoreFileInfo> result
            = storeIndex.findFileInfo(derivateId, updatedFileInfoData.getAbsolutePath());
        assertTrue(result.isPresent());
        assertTrue(result.get().flags().size() > 0);
    }

    @Test
    public void testDeleteStoreInfoByDerivateId() {
        final MCRObjectID derivateIdOne = MCRObjectID.getInstance("mcr_test_00000001");
        final MCRExternalStoreInfoData storeInfoOne = createTestInfoData(derivateIdOne);
        final MCRObjectID derivateIdTwo = MCRObjectID.getInstance("mcr_test_00000002");
        final MCRExternalStoreInfoData storeInfoTwo = createTestInfoData(derivateIdTwo);
        storeInfoRepository.insert(storeInfoOne);
        storeInfoRepository.insert(storeInfoTwo);
        this.startNewTransaction();
        storeIndex.deleteFileInfos(derivateIdOne);
        assertTrue(storeInfoRepository.findByDerivateId(derivateIdOne).isEmpty());
        assertTrue(storeInfoRepository.findByDerivateId(derivateIdTwo).isPresent());
    }

    @Test
    public void testFindFileInfo() {
        final MCRObjectID derivateId = MCRObjectID.getInstance("mcr_test_00000001");
        final MCRExternalStoreInfoData storeInfoData = createTestInfoData(derivateId);
        storeInfoRepository.insert(storeInfoData);
        this.startNewTransaction();
        final Optional<MCRExternalStoreFileInfo> fileInfo = storeIndex.findFileInfo(derivateId, "foo/test.txt");
        assertTrue(fileInfo.isPresent());
        assertEquals("test.txt", fileInfo.get().name());
        assertEquals("foo", fileInfo.get().parentPath());
    }

    private MCRExternalStoreInfoData createTestInfoData(MCRObjectID derivateId) {
        final MCRExternalStoreInfoData storeInfoData = new MCRExternalStoreInfoData();
        storeInfoData.setDerivateId(derivateId);
        final MCRExternalStoreFileInfoData fileA = new MCRExternalStoreFileInfoData();
        fileA.setDirectory(true);
        fileA.setName("foo");
        fileA.setParentPath("");
        storeInfoData.addFileInfo(fileA);
        final MCRExternalStoreFileInfoData fileB = new MCRExternalStoreFileInfoData();
        fileB.setDirectory(true);
        fileB.setName("bar.zip");
        fileB.setParentPath("");
        storeInfoData.addFileInfo(fileB);
        final MCRExternalStoreFileInfoData fileC = new MCRExternalStoreFileInfoData();
        fileC.setDirectory(false);
        fileC.setName("hello.txt");
        fileC.setParentPath("");
        storeInfoData.addFileInfo(fileC);
        final MCRExternalStoreFileInfoData fileD = new MCRExternalStoreFileInfoData();
        fileD.setDirectory(false);
        fileD.setName("test.txt");
        fileD.setParentPath("foo");
        storeInfoData.addFileInfo(fileD);
        return storeInfoData;
    }
}
