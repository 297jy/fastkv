package com.zhuanyi.leveldb.core.db.log;

import com.zhuanyi.leveldb.core.common.Result;
import com.zhuanyi.leveldb.core.common.Slice;

public interface LogReader {

    Result<Slice> readRecord();

    LogFormat.RecordType readPhysicalRecord(Slice result);

}
