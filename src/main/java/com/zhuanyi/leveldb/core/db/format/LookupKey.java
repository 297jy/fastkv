package com.zhuanyi.leveldb.core.db.format;

/**
 * 用于memtable的Get接口，它是由User Key和Sequence Number组合而成的
 */
public class LookupKey {

    private byte[] data;

    public LookupKey(byte[] userKey, SequenceNumber seq) {

    }

    public byte[] memTableKey() {
        return null;
    }

    public byte[] internalKey() {
        return null;
    }

    public byte[] userKey() {
        return null;
    }

}
