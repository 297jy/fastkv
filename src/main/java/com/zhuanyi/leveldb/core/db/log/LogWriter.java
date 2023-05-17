package com.zhuanyi.leveldb.core.db.log;

import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.common.Status;

import java.nio.channels.FileChannel;

public interface LogWriter {

    Status addRecord(Slice slice);

}
