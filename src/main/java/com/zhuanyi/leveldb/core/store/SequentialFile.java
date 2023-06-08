package com.zhuanyi.leveldb.core.store;

import com.zhuanyi.leveldb.core.common.Result;
import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.common.Status;

public interface SequentialFile {

    /**
     * 从这个文件读取n个字节
     * @param n
     * @return
     */
    Status read(int n, Slice buffer);

    /**
     * 从这个文件跳过n个字节
     * @param n
     * @return
     */
    Status skip(int n);

    Status close();

}
