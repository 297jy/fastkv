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

    public static <E> Result<E> fail(Status status) {
        Result<E> result = new Result<>();
        result.setStatus(status);
        return result;
    }

    public static <E> Result<E> success(E value) {
        Result<E> result = new Result<>();
        result.setValue(value);
        result.setStatus(Status.ok());
        return result;
    }
}
