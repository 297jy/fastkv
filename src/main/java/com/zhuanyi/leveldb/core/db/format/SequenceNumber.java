package com.zhuanyi.leveldb.core.db.format;

/**
 * leveldb操作的序列号，判断执行顺序的
 * 每一个操作都会被赋予一个sequence number。该计时器是在leveldb内部维护，每进行一次操作就做一个累加。
 * 由于在leveldb中，一次更新或者一次删除，采用的是append的方式，并非直接更新原数据。
 * 因此对应同样一个key，会有多个版本的数据记录，而最大的sequence number对应的数据记录就是最新的。
 */
public class SequenceNumber {
}
