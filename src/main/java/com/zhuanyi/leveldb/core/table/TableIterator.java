package com.zhuanyi.leveldb.core.table;


/**
 * 实现表迭代器：例如：跳跃表、MemTable
 * @param <K> 键
 */
public interface TableIterator<K extends Comparable<K>> {

    /**
     * 返回迭代器当前所在的位置是否有效
     * @return
     */
    boolean valid();

    /**
     * 返回当前节点的key
     * @return
     */
    K key();

    /**
     * 迭代器移动到下一个位置
     */
    void next();

    /**
     * 迭代器移动到前一个位置
     */
    void prev();

    /**
     * 迭代器移动到第一个 key >= target 所在的entry
     * @param target
     */
    void seek(K target);

    /**
     * 迭代器移动到表中第一个元素
     */
    void seekToFirst();

    /**
     * 迭代器移动到表中最后一个元素
     */
    void seekToLast();

}
