package com.zhuanyi.leveldb.core.db;

import com.zhuanyi.leveldb.common.Result;
import com.zhuanyi.leveldb.core.common.Coding;
import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.common.Status;
import com.zhuanyi.leveldb.core.db.enums.ValueType;
import com.zhuanyi.leveldb.core.db.format.DbFormat;
import com.zhuanyi.leveldb.core.db.format.InternalKey;
import com.zhuanyi.leveldb.core.db.format.LookupKey;
import com.zhuanyi.leveldb.core.db.format.MemTableKey;
import com.zhuanyi.leveldb.core.table.SkipTable;
import com.zhuanyi.leveldb.core.table.TableIterator;
import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;

import java.util.Comparator;

public class DefaultMemTableImpl implements MemTable {

    private final SkipTable<Slice> table;

    public static class KeyComparator implements Comparator<Slice> {

        private final DbFormat.InternalKeyComparator comparator;

        public KeyComparator(DbFormat.InternalKeyComparator comparator) {
            assert (comparator != null);

            this.comparator = comparator;
        }

        @Override
        public int compare(Slice o1, Slice o2) {
            Slice co1 = new Slice(o1);
            Slice co2 = new Slice(o2);

            getLengthPrefixedSlice(co1);
            getLengthPrefixedSlice(co2);
            return comparator.compare(co1, co2);
        }

        private void getLengthPrefixedSlice(Slice c) {
            int keyLen = c.readVarInt();
            c.cutAhead(keyLen - 8);
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


    private static class InnerMemTableIterator implements MemTableIterator<Slice> {

        private final TableIterator<Slice> it;

        private MemTableNode nowNode;

        public InnerMemTableIterator(TableIterator<Slice> it) {
            this.it = it;
        }

        @Override
        public boolean valid() {
            return it.valid();
        }

        @Override
        public Slice key() {
            if (nowNode == null) {
                nowNode = MemTableNode.readMemTableNode(it.key());
            }
            return nowNode.memTableKey.getInternalKey().getUserKey();
        }

        @Override
        public void next() {
            it.next();
        }

        @Override
        public void prev() {
            it.prev();
        }

        @Override
        public void seek(Slice target) {
            it.seek(target);
        }

        @Override
        public void seekToFirst() {
            it.seekToFirst();
        }

        @Override
        public void seekToLast() {
            it.seekToLast();
        }

        @Override
        public Slice value() {
            if (nowNode == null) {
                nowNode = MemTableNode.readMemTableNode(it.key());
            }
            return nowNode.userValue;
        }

        @Override
        public Status status() {
            return Status.ok();
        }
    }

    @Override
    public MemTableIterator<Slice> newIterator() {
        return new InnerMemTableIterator(table.iterator());
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
    public Result<Slice> get(LookupKey key) {
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

        return Result.fail(Status.unKnown(key.userKey().toString()));
    }

}
