package com.zhuanyi.leveldb.core.store;

import java.nio.ByteBuffer;

public class SelectMappedBufferResult {

    private final long startOffset;

    private final ByteBuffer byteBuffer;

    private int size;

    private MappedWritableFile mappedWritableFile;

    public SelectMappedBufferResult(long startOffset, ByteBuffer byteBuffer, int size, MappedWritableFile mappedWritableFile) {
        this.startOffset = startOffset;
        this.byteBuffer = byteBuffer;
        this.size = size;
        this.mappedWritableFile = mappedWritableFile;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public int getSize() {
        return size;
    }

    public void setSize(final int s) {
        this.size = s;
        this.byteBuffer.limit(this.size);
    }

    public long getStartOffset() {
        return startOffset;
    }
}
