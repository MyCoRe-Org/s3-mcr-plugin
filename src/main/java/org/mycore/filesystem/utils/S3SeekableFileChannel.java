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

package org.mycore.filesystem.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class S3SeekableFileChannel implements SeekableByteChannel {

    public static final int BUFFER_SIZE = 1024 * 1024 * 3;

    private final long size;

    private boolean open;

    private static final Logger LOGGER = LogManager.getLogger();

    private final AmazonS3 conn;

    private final String bucket;

    private final String id;

    private long position = 0;

    private final ByteBuffer internalBuffer = ByteBuffer.allocate(BUFFER_SIZE); // 10MB Buffer

    private long chunck = -1;

    public S3SeekableFileChannel(AmazonS3 conn, String bucket, String id) {
        this.conn = conn;
        this.bucket = bucket;
        this.id = id;
        final ObjectMetadata metadata = conn.getObjectMetadata(bucket, id);
        size = metadata.getContentLength();
        this.open = true;
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

        final long from = this.position;
        final long to = this.position + bytesToRead;

        final long startChunk = Math.floorDiv(from, BUFFER_SIZE);
        final long endChunk = Math.floorDiv(to, BUFFER_SIZE);
        LOGGER.debug("Reading {} at pos {}", bytesToRead, this.position);

        long written = 0;
        for (long i = startChunk; i <= endChunk; i++) {
            if (chunck != i) {
                fillChunk(i);
            }
            long startInChunk = Math.max(from - (i * BUFFER_SIZE), 0);
            long endInChunk = Math.min(to - (i * BUFFER_SIZE), BUFFER_SIZE);

            final long length = endInChunk - startInChunk;
            final byte[] bytes = new byte[(int) length];
            this.internalBuffer.position((int) startInChunk);
            this.internalBuffer.get(bytes);
            dst.put(bytes);
            written += bytes.length;
        }

        this.position+=written;

        return (int) written;
    }

    private void fillChunk(long chunck) throws IOException {
        final GetObjectRequest objectRequest = new GetObjectRequest(bucket, id);
        final long start = chunck * BUFFER_SIZE;
        final long end = Math.min((chunck + 1) * BUFFER_SIZE, size);
        objectRequest.setRange(start, end-1);

        LOGGER.debug("Fill chunck {} with {} bytes", chunck, end - start);

        final S3Object object = this.conn.getObject(objectRequest);
        try (InputStream is = object.getObjectContent()) {
            final byte[] src = is.readAllBytes();
            this.internalBuffer.rewind().put(src);
        }
        this.chunck = chunck;
    }

    private void ensureOpen() throws IOException {
        if (!this.open) {
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
        this.position = newPosition;
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
        this.open = false;
    }
}
