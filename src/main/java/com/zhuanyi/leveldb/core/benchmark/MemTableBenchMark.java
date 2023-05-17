package com.zhuanyi.leveldb.core.benchmark;

import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.db.DefaultMemTableImpl;
import com.zhuanyi.leveldb.core.db.enums.ValueType;
import com.zhuanyi.leveldb.core.db.format.DbFormat;

public class MemTableBenchMark {

    public static void main(String[] args) {
        long s = System.currentTimeMillis();
        insert();
        System.out.println("耗时：" + (System.currentTimeMillis() - s) + "毫秒");
    }

    public static void insert() {
        DefaultMemTableImpl memTable = new DefaultMemTableImpl(new DbFormat.InternalKeyComparator(null));
        for (long i = 0; i < 40000000L; i++) {
            memTable.add(i, ValueType.K_TYPE_VALUE, new Slice(("key" + i).getBytes()), new Slice(("value" + i).getBytes()));
        }
        System.out.println("占用内存：" + memTable.approximateMemoryUsage() + "字节");
    }
}
