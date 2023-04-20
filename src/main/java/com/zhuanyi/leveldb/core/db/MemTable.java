package com.zhuanyi.leveldb.core.db;

import com.zhuanyi.leveldb.common.Result;
import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.db.enums.ValueType;
import com.zhuanyi.leveldb.core.db.format.LookupKey;

public interface MemTable {

    long approximateMemoryUsage();

    MemTableIterator<Slice> newIterator();

    void add(long seq, ValueType type, Slice key, Slice value);

    Result<Slice> get(LookupKey key);

}
