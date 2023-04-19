package com.zhuanyi.leveldb.core.db;

import com.zhuanyi.leveldb.common.Result;
import com.zhuanyi.leveldb.core.common.Coding;
import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.common.Status;
import com.zhuanyi.leveldb.core.db.enums.ValueType;
import com.zhuanyi.leveldb.core.db.format.DbFormat;
import com.zhuanyi.leveldb.core.db.format.InternalKey;
import com.zhuanyi.leveldb.core.db.format.MemTableKey;
import com.zhuanyi.leveldb.core.table.SkipTable;
import javafx.util.Pair;
import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;

import java.util.Comparator;

public class DefaultMemTableImpl implements MemTable {

    private final SkipTable<Slice> table;

    private final Slice tmp = new Slice();

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

    private static class MemTableNode {

        private final MemTableKey memTableKey;

        private final int valueLen;

        private final Slice userValue;

        public MemTableNode(MemTableKey memTableKey, int valueLen, Slice userValue) {
            this.memTableKey = memTableKey;
            this.valueLen = valueLen;
            this.userValue = userValue;
        }

        public static MemTableNode readMemTableNode(Slice node) {
            MemTableKey memTableKey = MemTableKey.readMemTableKey(node);
            int valueLen = node.readVarInt();
            Slice userValue = node.read(valueLen);
            return new MemTableNode(memTableKey, valueLen, userValue);
        }

        public static void writeMemTableNode(Slice target, MemTableNode memTableNode) {
            MemTableKey.writeMemTableKey(target, memTableNode.memTableKey);
            target.writeVarInt(memTableNode.valueLen);
            target.write(memTableNode.userValue);
        }

        public boolean valid() {
            return memTableKey.valid();
        }

        public boolean deleted() {
            return memTableKey.deleted();
        }
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
        long sat = (seq << 8) | type.getCode();
        InternalKey internalKey = new InternalKey(key, sat);
        MemTableKey memTableKey = new MemTableKey(internalKeySize, internalKey);
        MemTableNode memTableNode = new MemTableNode(memTableKey, valueSize, value);
        Slice node = new Slice(encodedLen);
        MemTableNode.writeMemTableNode(node, memTableNode);

        table.insert(node);
    }

    @Override
    public Result<Slice> get(DbFormat.LookupKey key) {
        Slice memkey = key.memTableKey();
        TableIterator<Slice> it = table.iterator();
        it.seek(memkey);
        if (it.valid()) {
            Slice node = it.key();
            // 查询到的key为>=lookupKey，所以还需要判断是否相等
            if (comparator.compare(key.memTableKey(), node) == 0) {
                MemTableNode memTableNode = MemTableNode.readMemTableNode(node);
                if (memTableNode.valid()) {
                    return Result.success(memTableNode.userValue.copy());
                } else {
                    if (memTableNode.deleted()) {
                        return Result.success(null);
                    }
                }
            }
        }
        return Result.fail(Status.unKnown(new String(key.userKey().getData())));
    }

    @Override
    public int cmpKey(Slice key1, Slice key2) {
        return 0;
    }
}
