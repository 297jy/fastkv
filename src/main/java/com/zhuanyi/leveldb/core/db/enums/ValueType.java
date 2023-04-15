package com.zhuanyi.leveldb.core.db.enums;

/**
 * internal keys 的 值类型
 */
public enum ValueType {

    /**
     * 代表该key已经被删除
     */
    K_TYPE_DELETION,

    /**
     * 代表该key的值
     */
    K_TYPE_VALUE;

}
