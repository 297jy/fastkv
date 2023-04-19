package com.zhuanyi.leveldb.core.db;

import com.zhuanyi.leveldb.common.Result;
import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.db.enums.ValueType;
import com.zhuanyi.leveldb.core.db.format.DbFormat;

public interface MemTable {

    long approximateMemoryUsage();

    TableIterator<Slice> newIterator();

    void add(long seq, ValueType type, Slice key, Slice value);

    Result<Slice> get(DbFormat.LookupKey key);

    int cmpKey(Slice key1, Slice key2);

}
