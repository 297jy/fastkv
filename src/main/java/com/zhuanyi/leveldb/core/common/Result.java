package com.zhuanyi.leveldb.core.common;

public class Result<T> {

    private T data;

    private Status status;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean success() {
        return status != null && status.isOk();
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setData(data);
        result.setStatus(Status.ok());
        return result;
    }

    public static <T> Result<T> fail(Status status) {
        Result<T> result = new Result<>();
        result.setStatus(status);
        return result;
    }
}
