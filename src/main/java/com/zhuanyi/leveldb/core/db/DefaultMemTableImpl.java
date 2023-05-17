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
//import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;
//import org.openjdk.jol.info.ClassLayout;

import java.util.Comparator;

public class DefaultMemTableImpl implements MemTable {

    private final SkipTable<Slice> table;

    private static class KeyComparator implements Comparator<Slice> {

        private final DbFormat.InternalKeyComparator comparator;

        public KeyComparator(DbFormat.InternalKeyComparator comparator) {

            this.comparator = comparator;
        }

        @Override
        public int compare(Slice o1, Slice o2) {
            Slice co1 = new Slice(o1);
            Slice co2 = new Slice(o2);
            return comparator.compare(co1, co2);
        }
    }

    private final DbFormat.InternalKeyComparator comparator;

    public DefaultMemTableImpl(DbFormat.InternalKeyComparator comparator) {
        this.comparator = comparator;
        this.table = new SkipTable<>(new KeyComparator(comparator));
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
        //return ClassLayout.parseInstance(table).instanceSize();
        //return ObjectSizeCalculator.getObjectSize(table);
        //return RamUsageEstimator.sizeOf(table);
        return table.approximateMemoryUsage();
    }


    private static class InnerMemTableIterator implements MemTableIterator<Slice> {

        private final TableIterator<Slice> it;

        private MemTableNode nowNode;

        public InnerMemTableIterator(TableIterator<Slice> it) {
            this.it = it;
        }

        @Override
        public boolean valid() {
            if (!it.valid()) {
                return false;
            }

            if (nowNode == null) {
                nowNode = MemTableNode.readMemTableNode(new Slice(it.key()));
            }
            return nowNode.valid();
        }

        @Override
        public Slice key() {
            if (nowNode == null) {
                nowNode = MemTableNode.readMemTableNode(new Slice(it.key()));
            }
            return nowNode.memTableKey.getInternalKey().getUserKey();
        }

        @Override
        public void next() {
            nowNode = null;
            it.next();
        }

        @Override
        public void prev() {
            nowNode = null;
            it.prev();
        }

        @Override
        public void seek(Slice target) {
            nowNode = null;
            it.seek(target);
        }

        @Override
        public void seekToFirst() {
            nowNode = null;
            it.seekToFirst();
        }

        @Override
        public void seekToLast() {
            nowNode = null;
            it.seekToLast();
        }

        @Override
        public Slice value() {
            if (nowNode == null) {
                nowNode = MemTableNode.readMemTableNode(new Slice(it.key()));
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
        MemTableIterator<Slice> it = newIterator();
        it.seek(memkey);
        if (it.valid()) {
            Slice node = it.key();
            // 查询到的key为>=lookupKey，所以还需要判断是否相等
            if (comparator.compareUserKey(key.userKey(), node) == 0) {
                return Result.success(it.value());
            }
        }

        return Result.fail(Status.notFound(key.userKey().toString()));
    }

}
