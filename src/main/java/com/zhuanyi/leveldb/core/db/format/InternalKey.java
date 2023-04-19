package com.zhuanyi.leveldb.core.db.format;

import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.db.enums.ValueType;

public class InternalKey {

    private final Slice userKey;

    private final long sequenceAndType;

    public InternalKey(Slice userKey, long sequenceAndType) {
        this.userKey = userKey;
        this.sequenceAndType = sequenceAndType;
    }

    public Slice getUserKey() {
        return userKey;
    }

    public long getSequenceAndType() {
        return sequenceAndType;
    }

    public boolean valid() {
        int tag = (int) (sequenceAndType & 0xff);
        ValueType type = ValueType.valueOf(tag);
        return ValueType.K_TYPE_VALUE.equals(type);
    }

    public boolean deleted() {
        int tag = (int) (sequenceAndType & 0xff);
        ValueType type = ValueType.valueOf(tag);
        return ValueType.K_TYPE_DELETION.equals(type);
    }

    public static InternalKey readInternalKey(Slice node, int userKeyLen) {
        Slice userKey = node.read(userKeyLen - 8);
        long sequenceAndType = node.readLong();
        return new InternalKey(userKey, sequenceAndType);
    }

    public static void writeInternalKey(Slice target, InternalKey key) {
        target.write(key.userKey);
        target.writeLong(key.sequenceAndType);
    }

}
