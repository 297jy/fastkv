package com.zhuanyi.leveldb.core.db.format;

import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.db.enums.ValueType;

public class MemTableKey {

    private final int keyLen;

    private final InternalKey internalKey;

    public int getKeyLen() {
        return keyLen;
    }

    public InternalKey getInternalKey() {
        return internalKey;
    }

    public MemTableKey(int keyLen, InternalKey internalKey) {
        this.keyLen = keyLen;
        this.internalKey = internalKey;
    }

    public boolean valid() {
        return internalKey.valid();
    }

    public boolean deleted() {
        return internalKey.deleted();
    }

    public static MemTableKey readMemTableKey(Slice node) {
        int keyLen = node.readVarInt();
        InternalKey internalKey = InternalKey.readInternalKey(node, keyLen);
        return new MemTableKey(keyLen, internalKey);
    }

    public static void writeMemTableKey(Slice target, MemTableKey memTableKey) {
        target.writeVarInt(memTableKey.getKeyLen());
        InternalKey.writeInternalKey(target, memTableKey.getInternalKey());
    }

}
