package org.mycore.externalstore.archive;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.junit.Test;
import org.mycore.common.MCRTestCase;
import org.mycore.common.content.MCRURLContent;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;

import com.amazonaws.util.IOUtils;

public class MCRExternalStoreTarArchiveResolverTest extends MCRTestCase {

    @Test
    public void testListFileSummaries() throws IOException {
        final SeekableByteChannel channel = getTarTestFile();
        final List<MCRExternalStoreFileInfo> summaries
            = new MCRExternalStoreTarArchiveResolver().listFileInfos(channel);
        assertEquals(6, summaries.size());
    }

    @Test
    public void testGetInputStream() throws IOException {
        final SeekableByteChannel channel = getTarTestFile();
        final InputStream inputStream
            = new MCRExternalStoreTarArchiveResolver().getInputStream(channel, "sub/sub2/test4.txt");
        final String result = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("Hello from test4.txt\n", result);

    }

    private SeekableByteChannel getTarTestFile() throws IOException {
        MCRURLContent content = new MCRURLContent(
            MCRExternalStoreTarArchiveResolverTest.class.getClassLoader().getResource("tar/test.tar"));
        return new SeekableInMemoryByteChannel(IOUtils.toByteArray(content.getInputStream()));
    }
}
