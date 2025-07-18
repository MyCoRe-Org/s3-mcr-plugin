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

package org.mycore.externalstore.s3;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectAttributesRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectAttributes;

/**
 * {@link SeekableByteChannel} implementation for s3 object.
 */
public class MCRS3SeekableFileChannel implements SeekableByteChannel {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final int BUFFER_SIZE = 1024 * 1024 * 3; // 3 MB

    private final long size;

    private boolean open;

    private final S3Client client;

    private final String bucket;

    private final String key;

    private long position;

    private final ByteBuffer internalBuffer = ByteBuffer.allocate(BUFFER_SIZE);

    private long chunck = -1;

    /**
     * Creates {@link MCRS3SeekableFileChannel} by specified {@link AmazonS3}, bucket name and object key.
     *
     * @param client the client
     * @param bucket the bucket
     * @param key the key
     */
    public MCRS3SeekableFileChannel(S3Client client, String bucket, String key) {
        this.client = client;
        this.bucket = bucket;
        this.key = key;
        LOGGER.debug(bucket);
        LOGGER.debug(key);
        final GetObjectAttributesRequest request = GetObjectAttributesRequest.builder().bucket(bucket).key(key)
            .objectAttributes(ObjectAttributes.OBJECT_SIZE).build();
        size = client.getObjectAttributes(request).objectSize();
        open = true;
    }

    private long left() {
        return size - position;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        final int remaining = dst.remaining();
        if (remaining == 0 || remaining < 0) {
            return -1;
        }
        ensureOpen();

        final long left = left();
        final long bytesToRead = Math.min(left, remaining);

        final long from = position;
        final long to = position + bytesToRead;

        final long startChunk = Math.floorDiv(from, BUFFER_SIZE);
        final long endChunk = Math.floorDiv(to, BUFFER_SIZE);
        LOGGER.debug("Reading {} at pos {}", bytesToRead, position);

        long written = 0;
        for (long i = startChunk; i <= endChunk; i++) {
            if (chunck != i) {
                fillChunk(i);
            }
            long startInChunk = Math.max(from - (i * BUFFER_SIZE), 0);
            long endInChunk = Math.min(to - (i * BUFFER_SIZE), BUFFER_SIZE);

            final long length = endInChunk - startInChunk;
            final byte[] bytes = new byte[(int) length];
            internalBuffer.position((int) startInChunk);
            internalBuffer.get(bytes);
            dst.put(bytes);
            written += bytes.length;
        }

        position += written;

        return (int) written;
    }

    private void fillChunk(long chunck) throws IOException {
        final long start = chunck * BUFFER_SIZE;
        final long end = Math.min((chunck + 1) * BUFFER_SIZE, size);

        final GetObjectRequest objectRequest
            = GetObjectRequest.builder().bucket(bucket).key(key).range("bytes=" + start + "-" + end).build();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fill chunck {} with {} bytes", chunck, end - start);
        }

        try (InputStream is = client.getObject(objectRequest)) {
            final byte[] src = is.readAllBytes();
            internalBuffer.rewind().put(src);
        }
        this.chunck = chunck;
    }

    private void ensureOpen() throws IOException {
        if (!open) {
            throw new IOException("Channel already closed!");
        }
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        throw new IOException("write no supported on " + this.getClass().getName());
    }

    @Override
    public long position() throws IOException {
        return position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        LOGGER.debug("Set pos from {} to {}", position, newPosition);
        position = newPosition;
        return this;
    }

    @Override
    public long size() throws IOException {
        return size;
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        throw new IOException("truncate not supported on " + this.getClass().getName());
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() throws IOException {
        open = false;
    }

}
