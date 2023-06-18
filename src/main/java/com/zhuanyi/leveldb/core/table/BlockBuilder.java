package com.zhuanyi.leveldb.core.table;

import com.zhuanyi.leveldb.core.common.Slice;

public interface BlockBuilder {

    void reset();

    void add(Slice key, Slice value);

    Slice finish();

    int currentSizeEstimate();

    boolean isEmpty();

}
