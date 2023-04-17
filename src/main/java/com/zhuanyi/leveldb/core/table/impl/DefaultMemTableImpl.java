package com.zhuanyi.leveldb.core.table.impl;

import com.zhuanyi.leveldb.common.Result;
import com.zhuanyi.leveldb.core.common.Coding;
import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.db.enums.ValueType;
import com.zhuanyi.leveldb.core.db.format.DbFormat;
import com.zhuanyi.leveldb.core.table.MemTable;
import com.zhuanyi.leveldb.core.table.SkipTable;
import com.zhuanyi.leveldb.core.table.TableIterator;
import javafx.util.Pair;
import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;

import java.util.Comparator;

public class DefaultMemTableImpl implements MemTable {

    private final SkipTable<Slice> table;

    public static class KeyComparator implements Comparator<Slice> {

        private final DbFormat.InternalKeyComparator comparator;

        /**
         * 提前缓存好，要比较的2个slice，避免重复new造成性能开销
         */
        private final Slice cmpO1 = new Slice();
        private final Slice cmpO2 = new Slice();

        public KeyComparator(DbFormat.InternalKeyComparator comparator) {
            assert (comparator != null);

            this.comparator = comparator;
        }

        @Override
        public int compare(Slice o1, Slice o2) {
            getLengthPrefixedSlice(o1, cmpO1);
            getLengthPrefixedSlice(o2, cmpO2);
            return comparator.compare(cmpO1, cmpO2);
        }

        private void getLengthPrefixedSlice(Slice s, Slice dst) {
            Pair<Integer, Integer> beginSizePair = Coding.getVarInt32Ptr(s.getData(), 0, 5);
            dst.refresh(s.getData(), beginSizePair.getKey(), beginSizePair.getValue());
        }
    }

    private final KeyComparator comparator;

    public DefaultMemTableImpl(DbFormat.InternalKeyComparator comparator) {
        this.comparator = new KeyComparator(comparator);
        table = new SkipTable<>(this.comparator);
    }

    @Override
    public long approximateMemoryUsage() {
        return ObjectSizeCalculator.getObjectSize(table);
    }

    @Override
    public TableIterator<Slice> newIterator() {
        return null;
    }

    @Override
    public void add(long seq, ValueType type, Slice key, Slice value) {
        int keySize = key.getSize();
        int valueSize = value.getSize();
        int internalKeySize = keySize + 8;
        // 数据格式:internalKeySizeByteLen + internalKeySize + valueByteLen + valueSize
        int encodedLen = Coding.varIntLength(internalKeySize) + internalKeySize + Coding.varIntLength(valueSize) + valueSize;

        byte[] dst = new byte[encodedLen];
        int begin = encodeKey(dst, key, seq, type);
        begin = encodeValue(dst, begin, value);

        table.insert(new Slice(dst, 0, begin));
    }

    private int encodeKey(byte[] dst, Slice key, long seq, ValueType type) {
        int keySize = key.getSize();
        int internalKeySize = keySize + 8;

        int begin = Coding.encodeVarInt32(dst, 0, internalKeySize);
        System.arraycopy(key.getData(), key.getBegin(), dst, begin, key.getSize());
        begin += key.getSize();

        Coding.encodeFixed64(dst, begin, (seq << 8) | type.getCode());
        begin += 8;

        return begin;
    }

    private int encodeValue(byte[] dst, int begin, Slice value) {
        int valSize = value.getSize();

        begin = Coding.encodeVarInt32(dst, begin, valSize);
        System.arraycopy(value.getData(), value.getBegin(), dst, begin, value.getSize());
        begin += value.getSize();

        return begin;
    }

    private int encodeValue(byte[] dst, int begin, Slice key, long seq, ValueType type) {
        System.arraycopy(key.getData(), key.getBegin(), dst, begin, key.getSize());
        begin += key.getSize();

        Coding.encodeFixed64(dst, begin, (seq << 8) | type.getCode());
        begin += 8;

        return begin;
    }

    @Override
    public Result<Slice> get(DbFormat.LookupKey key) {
        return null;
    }

    @Override
    public int cmpKey(Slice key1, Slice key2) {
        return 0;
    }
}
