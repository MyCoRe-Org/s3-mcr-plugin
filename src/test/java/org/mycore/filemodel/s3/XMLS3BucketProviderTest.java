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

package org.mycore.filemodel.s3;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.Assert;
import org.mycore.common.MCRConstants;
import org.mycore.filesystem.s3.XMLS3BucketProvider;
import org.mycore.filesystem.s3.S3BucketSettings;

import java.io.IOException;
import java.io.InputStream;


public class XMLS3BucketProviderTest {

    @org.junit.Test
    public void getBucketSettings() throws IOException, JDOMException {
        try(InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("s3/test.xml")){
            SAXBuilder saxBuilder = new SAXBuilder();
            Document doc = saxBuilder.build(resourceAsStream);
            Element modsExtension = doc.getRootElement().getChild("extension", MCRConstants.MODS_NAMESPACE);

            Element feb = modsExtension.getChild("folder-extension-bind");
            S3BucketSettings bucketSettings = new XMLS3BucketProvider().getBucketSettings(feb.getChildren().get(0));

            Assert.assertEquals("The endpoint should match","localhost:9000", bucketSettings.getEndpoint());
            Assert.assertEquals("The accesskey should match","ac1", bucketSettings.getAccessKey());
            Assert.assertEquals("The secretkey should match","alleswirdgut", bucketSettings.getSecretKey());
            Assert.assertEquals("The protocol should match","http", bucketSettings.getProtocol());
            Assert.assertEquals("The default region should match","us-east-1", bucketSettings.getSigningRegion());
            Assert.assertTrue("The default pathstyle should be true", bucketSettings.isPathStyleAccess());
        }
    }

}