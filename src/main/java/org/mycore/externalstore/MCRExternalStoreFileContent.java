package org.mycore.externalstore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;

import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRSeekableChannelContent;

/**
 * Extends {@link MCRContent} by {@link MCRSeekableChannelContent}.
 */
public class MCRExternalStoreFileContent extends MCRContent implements MCRSeekableChannelContent {

    private final MCRExternalStoreProvider provider;

    private final String path;

    /**
     * Constructs content with {@link MCRExternalStoreProvider} and path.
     *
     * @param provider provider
     * @param path path
     */
    public MCRExternalStoreFileContent(MCRExternalStoreProvider provider, String path) {
        this.provider = provider;
        this.path = path;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return provider.newInputStream(path);
    }

    @Override
    public SeekableByteChannel getSeekableByteChannel() throws IOException {
        return provider.newByteChannel(path);
    }

}
