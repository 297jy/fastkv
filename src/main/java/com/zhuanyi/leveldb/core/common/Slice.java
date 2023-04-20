package com.zhuanyi.leveldb.core.common;

import javafx.util.Pair;

import java.util.Iterator;

public class Slice implements Comparable<Slice>, Iterable<Byte> {

    private byte[] data;

    private int begin;

    private int end;

    public Slice() {
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

    public void cutAhead(int n) {
        end = begin + n;
    }

    public void resize(int n) {
        end = begin + n;
    }

    public Integer readVarInt() {

        Pair<Integer, Integer> beginNumPair = Coding.getVarInt32Ptr(data, begin, end);
        if (beginNumPair == null) {
            return null;
        }
        begin = beginNumPair.getKey();
        return beginNumPair.getValue();
    }

    public Integer getVarInt() {
        Pair<Integer, Integer> beginNumPair = Coding.getVarInt32Ptr(data, begin, end);
        if (beginNumPair == null) {
            return null;
        }
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
        System.arraycopy(src.data, src.begin, data, end, src.getSize());
        end += src.getSize();
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

    public void refresh(Slice s) {
        this.data = s.data;
        this.begin = s.begin;
        this.end = s.begin;
    }

    public boolean empty() {
        return getSize() == 0;
    }

    public byte[] copyData() {
        byte[] newData = new byte[getSize()];
        System.arraycopy(data, begin, newData, 0, getSize());
        return newData;
    }

    public int getSize() {
        return end - begin;
    }

    public Slice copy() {
        byte[] newData = copyData();
        return new Slice(newData, 0, newData.length);
    }

    @Override
    public Iterator<Byte> iterator() {
        return new SliceIterator(data, begin, end);
    }

    private static class SliceIterator implements Iterator<Byte> {

        private final byte[] data;

        private int begin;

        private final int end;

        public SliceIterator(byte[] data, int begin, int end) {
            this.data = data;
            this.begin = begin;
            this.end = end;
        }

        @Override
        public boolean hasNext() {
            return begin < end;
        }

        @Override
        public Byte next() {
            return data[begin++];
        }
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
        int s1 = getSize();
        int s2 = o.getSize();
        if (s1 == s2) {
            return 0;
        }
        return s1 < s2 ? -1 : 1;
    }

    @Override
    public String toString() {
        return new String(data, begin, getSize());
    }
}
