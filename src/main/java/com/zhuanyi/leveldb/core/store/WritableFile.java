package com.zhuanyi.leveldb.core.store;

import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.common.Status;

public interface WritableFile {

    Status append(Slice data);

    Status close();

    Status flush();

    Status sync();

}
