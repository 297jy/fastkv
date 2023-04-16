package com.zhuanyi.leveldb.common;

import com.zhuanyi.leveldb.core.common.Status;
import lombok.Data;

@Data
public class Result<E> {

    private Status status;

    private E value;

    public boolean success() {
        return status != null && status.isOk();
    }
}
