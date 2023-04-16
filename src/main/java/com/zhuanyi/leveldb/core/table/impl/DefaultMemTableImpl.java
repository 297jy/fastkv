package com.zhuanyi.leveldb.core.table.impl;

import com.zhuanyi.leveldb.common.Result;
import com.zhuanyi.leveldb.core.common.Coding;
import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.db.enums.ValueType;
import com.zhuanyi.leveldb.core.db.format.DbFormat;
import com.zhuanyi.leveldb.core.table.MemTable;
import com.zhuanyi.leveldb.core.table.TableIterator;
import javafx.util.Pair;

import java.util.Comparator;

public class DefaultMemTableImpl implements MemTable {

    public DefaultMemTableImpl() {
    }

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

    private KeyComparator comparator;


    @Override
    public long approximateMemoryUsage() {
        return 0;
    }

    @Override
    public TableIterator<Slice> newIterator() {
        return null;
    }

    @Override
    public void add(long seq, ValueType type, Slice key, Slice value) {

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
