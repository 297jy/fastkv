package com.zhuanyi.leveldb.core.db;

import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.common.Status;
import com.zhuanyi.leveldb.core.table.TableIterator;

public interface MemTableIterator<K extends Comparable<K>> extends TableIterator<K> {

    Slice value();

    Status status();

}
