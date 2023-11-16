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

import java.util.Map;

import org.mycore.common.MCRTestCase;

public class MCRExternalStoreServiceTest extends MCRTestCase {

    private static final String KEY_FILE_NAME = "test.key";

    @Override
    protected Map<String, String> getTestProperties() {
        Map<String, String> testProperties = super.getTestProperties();
        testProperties.put("MCR.Crypt.Cipher.bucket-crypt.class", "org.mycore.crypt.MCRAESCipher");
        testProperties.put("MCR.Crypt.Cipher.bucket-crypt.EnableACL", Boolean.FALSE.toString());
        testProperties.put("MCR.Crypt.Cipher.bucket-crypt.KeyFile", "data/" + KEY_FILE_NAME);
        testProperties.put("MCR.ExternalStore.DefaultService.test.Settings.Encrypted", Boolean.TRUE.toString());
        testProperties.put("MCR.ExternalStore.DefaultService.test.Settings.Cipher", "bucket-crypt");
        testProperties.put("MCR.ExternalStore.DefaultService.default.Settings.Encrypted",
            Boolean.FALSE.toString());
        return testProperties;
    }

    /*
    @Test
    public void testCreateSettingsWrapper() throws Exception {
        final Map<String, String> settingsMap = new HashMap<>();
        settingsMap.put("var0", "foo");
        settingsMap.put("var1", "bar");
        final MCRExternalStoreService handler = new MCRExternalStoreService();
        final MCRExternalStoreRawSettingsWrapper wrapper = handler.createSettingsWrapper(settingsMap);
        assertFalse(wrapper.isEncryptedContent());
        final String rawContent = wrapper.getRawContent();
        assertEquals("{\"var1\":\"bar\",\"var0\":\"foo\"}", rawContent);
    }
    
    @Ignore
    @Test
    public void testCreateSettingsWrapper_crypt() throws Exception {
        copyKeyFile();
        final Map<String, String> settingsMap = new HashMap<>();
        settingsMap.put("var0", "foo");
        settingsMap.put("var1", "bar");
        final MCRExternalStoreService handler = new MCRExternalStoreService();
        final MCRExternalStoreRawSettingsWrapper wrapper = handler.createSettingsWrapper(settingsMap);
        assertTrue(wrapper.isEncryptedContent());
        final String rawContent = wrapper.getRawContent();
        final MCRCipher cipher = MCRCipherManager.getCipher("bucket-crypt");
        assertEquals("{\"var1\":\"bar\",\"var0\":\"foo\"}", cipher.decrypt(rawContent));
    }
    
    @Test
    public void testProcessSettingsWrapper() throws Exception {
        final MCRExternalStoreRawSettingsWrapper wrapper = new MCRExternalStoreRawSettingsWrapper();
        wrapper.setEncryptedContent(false);
        wrapper.setRawContent("{\"var1\":\"bar\",\"var0\":\"foo\"}");
        final MCRExternalStoreService handler = new MCRExternalStoreService();
        handler.setId("default");
        final Map<String, String> expectedSettingsMap = new HashMap<>();
        expectedSettingsMap.put("var0", "foo");
        expectedSettingsMap.put("var1", "bar");
        assertEquals(expectedSettingsMap, handler.processSettingsWrapper(wrapper));
    }
    
    @Ignore
    @Test
    public void testProcessSettingsWrapper_crypt() throws Exception {
        copyKeyFile();
        final MCRExternalStoreRawSettingsWrapper wrapper = new MCRExternalStoreRawSettingsWrapper();
        wrapper.setEncryptedContent(true);
        final MCRCipher cipher = MCRCipherManager.getCipher("bucket-crypt");
        wrapper.setRawContent(cipher.encrypt("{\"var1\":\"bar\",\"var0\":\"foo\"}"));
        final MCRExternalStoreService handler = new MCRExternalStoreService();
        final Map<String, String> expectedSettingsMap = new HashMap<>();
        expectedSettingsMap.put("var0", "foo");
        expectedSettingsMap.put("var1", "bar");
        assertEquals(expectedSettingsMap, handler.processSettingsWrapper(wrapper));
    }
    
    private void copyKeyFile() throws IOException {
        final InputStream input = MCRClassTools.getClassLoader().getResourceAsStream(KEY_FILE_NAME);
        final File keyFile = MCRConfigurationDir.getConfigFile("data/" + KEY_FILE_NAME);
        FileUtils.copyInputStreamToFile(input, keyFile);
    }
    */

}
