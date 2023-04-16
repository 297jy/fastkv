package com.zhuanyi.leveldb.core.common;

import java.util.Iterator;

public class Slice implements Comparable<Slice>, Iterable<Byte> {

    private byte[] data;

    private int begin;

    private int end;

    private int size;

    public Slice() {
    }

    public Slice(byte[] data, int begin, int size) {
        this.data = data;
        this.begin = begin;
        this.size = size;
        this.end = begin + size;
    }

    public Slice(Slice slice) {
        this.data = slice.data;
        this.begin = slice.begin;
        this.size = slice.size;
        this.end = slice.end;
    }

    public void refresh(byte[] data, int begin, int size) {
        this.data = data;
        this.begin = begin;
        this.size = size;
        this.end = begin + size;
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

    public Slice subSlice(int subBegin, int subEnd) {
        assert (subBegin >= begin && subEnd <= end);

        Slice slice = new Slice(this);
        slice.size = subEnd - subBegin;
        slice.begin = subBegin;
        slice.end = subEnd;

        return slice;
    }

    public boolean empty() {
        return size == 0;
    }

    public byte[] copyData() {
        byte[] newData = new byte[size];
        System.arraycopy(data, begin, newData, 0, size);
        return newData;
    }

    public int getSize() {
        return size;
    }

    public byte[] getData() {
        return data;
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
        if (size == o.size) {
            return 0;
        }
        return size < o.size ? -1 : 1;
    }

    @Override
    public String toString() {
        return new String(data, begin, size);
    }
}
