package com.zhuanyi.leveldb.core.table;

import com.zhuanyi.leveldb.core.db.enums.ValueType;
import com.zhuanyi.leveldb.core.db.format.SequenceNumber;

public interface MemTable<K extends Comparable<K>> {

     long approximateMemoryUsage();

     TableIterator<K> newIterator();

     void add(SequenceNumber seq, ValueType type, String key, String value);


}
