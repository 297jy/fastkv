package com.zhuanyi.leveldb.core.db.enums;

import java.util.Arrays;

/**
 * internal keys 的 值类型
 */
public enum ValueType {

    /**
     * 代表该key已经被删除
     */
    K_TYPE_DELETION(0),

    /**
     * 代表该key的值
     */
    K_TYPE_VALUE(1);

    private final int code;

    ValueType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ValueType valueOf(int code) {
        return Arrays.stream(values()).filter(v -> v.code == code).findFirst().orElse(null);
    }
}
