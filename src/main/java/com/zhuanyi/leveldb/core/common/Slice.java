package com.zhuanyi.leveldb.core.common;


import com.zhuanyi.leveldb.common.Pair;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class Slice implements Comparable<Slice> {

    private byte[] data;

    private int begin;

    private int end;

    public Slice() {
        data = new byte[0];
    }

    private Slice(byte[] data, int begin, int size) {
        this.data = data;
        this.begin = begin;
        this.end = begin + size;
    }

    public Slice(byte[] data) {
        this.data = data;
        this.begin = 0;
        this.end = data.length;
    }

    public Slice(int n) {
        data = new byte[n];
        begin = 0;
        end = 0;
    }

    public Slice read(int n) {
        Slice res = new Slice();
        res.data = data;
        res.begin = begin;
        res.end = begin + n;

        begin += n;
        return res;
    }

    public long crc32(int n) {
        CRC32 crc32 = new CRC32();
        crc32.update(data, begin, n);
        return crc32.getValue();
    }

    public Integer readVarInt() {

        Pair<Integer, Integer> beginNumPair = Coding.getVarInt32Ptr(data, begin, end);
        if (beginNumPair == null) {
            return null;
        }
        begin = beginNumPair.getKey();
        return beginNumPair.getValue();
    }

    public long readLong() {
        long res = Coding.decodeFixed64(data, begin);
        begin += 8;
        return res;
    }

    public void writeLong(long value) {
        Coding.encodeFixed64(data, end, value);
        end += 8;
    }

    public void writeVarInt(int value) {
        end = Coding.encodeVarInt32(data, end, value);
    }

    public void write(Slice src) {

        System.arraycopy(src.data, src.begin, data, end, src.readableBytes());
        end += src.readableBytes();
    }

    public void write(byte[] src) {
        System.arraycopy(src, 0, data, end, src.length);
        end += src.length;
    }

    public Slice(Slice slice) {
        this.data = slice.data;
        this.begin = slice.begin;
        this.end = slice.end;
    }

    public byte[] copyData() {
        byte[] newData = new byte[readableBytes()];
        System.arraycopy(data, begin, newData, 0, readableBytes());
        return newData;
    }

    /**
     * 当前slice剩余可读的字节数
     * @return
     */
    public int readableBytes() {
        return end - begin;
    }

    public Slice copy() {
        byte[] newData = copyData();
        return new Slice(newData, 0, newData.length);
    }

    public Slice duplicate() {
        return new Slice(this);
    }

    public byte[] bytes() {
        return copyData();
    }

    public boolean isEmpty() {
        return readableBytes() == 0;
    }

    public void readToBytes(byte[] targets, int offset, int len) {
        System.arraycopy(data, begin, targets, offset, len);
        begin += len;
    }

    public void readToByteBuffer(ByteBuffer targets, int len) {
        targets.put(data, begin, len);
        begin += len;
    }

    @Override
    public int compareTo(Slice o) {
        int i = begin;
        int j = o.begin;
        while (i < end && j < o.end) {
            if (data[i] != o.data[j]) {
                return data[i] < o.data[j] ? -1 : 1;
            }
            i++;
            j++;
        }
        int s1 = readableBytes();
        int s2 = o.readableBytes();
        if (s1 == s2) {
            return 0;
        }
        return s1 < s2 ? -1 : 1;
    }

    @Override
    public String toString() {
        return new String(data, begin, readableBytes());
    }
}
