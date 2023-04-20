package com.zhuanyi.leveldb.core.db.format;

import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.db.enums.ValueType;

public class LookupKey {

    private final MemTableKey memTableKey;

    public LookupKey(byte[] userKeyBytes, long sequenceNumber) {
        //int usize = userKeyBytes.length;
        // 8 = userKey + sequenceNumber + valueType 内存大小
        //int needed = usize + 8;
        int internalKeySize = userKeyBytes.length + 8;
        Slice key = new Slice(userKeyBytes);
        long sat = (sequenceNumber << 8) | ValueType.K_TYPE_VALUE.getCode();
        InternalKey internalKey = new InternalKey(key, sat);
        memTableKey = new MemTableKey(internalKeySize, internalKey);
    }

    public Slice memTableKey() {
        // 13 = Size + userKey + sequenceNumber + valueType 最大内存大小
        Slice memTableKeySlice = new Slice(memTableKey.getKeyLen() + 13);
        MemTableKey.writeMemTableKey(memTableKeySlice, memTableKey);
        return memTableKeySlice;
    }

    public Slice internalKey() {
        Slice internalKeySlice = new Slice(memTableKey.getKeyLen() + 8);
        InternalKey.writeInternalKey(internalKeySlice, memTableKey.getInternalKey());
        return internalKeySlice;
    }

    public Slice userKey() {
        return memTableKey.getInternalKey().getUserKey();
    }
}
