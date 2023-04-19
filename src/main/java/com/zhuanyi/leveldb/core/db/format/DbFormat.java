package com.zhuanyi.leveldb.core.db.format;

import com.zhuanyi.leveldb.core.common.Coding;
import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.db.enums.ValueType;

import java.util.Comparator;
import java.util.Iterator;

public class DbFormat {

    public static final long K_MAX_SEQUENCE_NUMBER = (((long) 1 << 55) - 1);

    public static long packSequenceAndType(long seq, ValueType t) {
        assert (seq <= K_MAX_SEQUENCE_NUMBER);
        return (seq << 8) | t.getCode();
    }

    public static class InternalKeyComparator {

        private final Comparator<Slice> userComparator;

        private final Comparator<InternalKey> userInternalComparator;

        public InternalKeyComparator(Comparator<Slice> userComparator) {
            this.userComparator = userComparator;
            this.userInternalComparator = (o1, o2) -> {
                int cmp = userComparator == null ? o1.compareTo(o2) : userComparator.compare(o1.userKey, o2.userKey);
                if (cmp != 0) {
                    return cmp;
                }
                if (o1.sequenceAndType == o2.sequenceAndType) {
                    return 0;
                }
                return o1.sequenceAndType < o2.sequenceAndType ? 1 : -1;
            };
        }

        public int compare(Slice a, Slice b) {
            if (userComparator != null) {
                return userComparator.compare(a, b);
            }
            return a.compareTo(b);
        }

        public int compare(InternalKey a, InternalKey b) {
            return userInternalComparator.compare(a, b);
        }
    }

    public static class InternalKey implements Comparable<InternalKey> {

        private Slice userKey;

        private long sequenceAndType;

        public InternalKey() {
        }

        public InternalKey(ParsedInternalKey parsedInternalKey) {
            reset(parsedInternalKey);
        }

        private void reset(ParsedInternalKey parsedInternalKey) {
            this.userKey = parsedInternalKey.userKey;
            this.sequenceAndType = packSequenceAndType(parsedInternalKey.sequenceNumber, parsedInternalKey.type);
        }

        /**
        public boolean decodeFrom(Slice s) {
            // 根据internalKey内存布局：@todo 后续补充
            userKey = s.subSlice(s.getBegin(), s.getEnd() - 8);

            long sat = 0;
            Iterator<Byte> it = s.subSlice(s.getBegin() - 8, s.getEnd()).iterator();
            sat |= it.next();
            sat |= it.next() << 8;
            sat |= it.next() << 16;
            sat |= it.next() << 24;
            sat |= (long) it.next() << 32;
            sat |= (long) it.next() << 40;
            sat |= (long) it.next() << 48;
            sat |= (long) it.next() << 56;
            sequenceAndType = sat;

            return true;
        }**/

        public Slice userKey() {
            return userKey;
        }

        public void setFrom(ParsedInternalKey parsedInternalKey) {
            reset(parsedInternalKey);
        }

        @Override
        public int compareTo(InternalKey o) {
            int cmp = userKey.compareTo(o.userKey);
            if (cmp != 0) {
                return cmp;
            }
            return sequenceAndType < o.sequenceAndType ? 1 : -1;
        }

        public ParsedInternalKey toParsedInternalKey() {
            long sat = sequenceAndType;
            int valueTypeNum = (int) (sat & 0xff);
            long sequenceNumber = sat >> 8;
            return new ParsedInternalKey(userKey, sequenceNumber, ValueType.valueOf(valueTypeNum));
        }

        public String debugString() {
            ParsedInternalKey parsedInternalKey = toParsedInternalKey();
            return parsedInternalKey.debugString();
        }
    }

    public static class ParsedInternalKey {

        private Slice userKey;

        private long sequenceNumber;

        private ValueType type;

        public ParsedInternalKey() {
        }

        public ParsedInternalKey(Slice userKey, long sequenceNumber, ValueType type) {
            this.userKey = userKey;
            this.sequenceNumber = sequenceNumber;
            this.type = type;
        }

        public String debugString() {
            return "'" + userKey.toString() + "' @ " + sequenceNumber + " : " + type;
        }
    }

    /**
     * 用于memtable的Get接口，它是由User Key和Sequence Number组合而成的
     * LookupKey的格式为：| Size (int32变长)| User key (string) | sequence number (7 bytes) | value type (1 byte) |
     */
    public static class LookupKey {

        private final int kstart;

        private final int end;

        private final byte[] data;

        public LookupKey(byte[] userKey, long sequenceNumber) {
            int usize = userKey.length;
            int needed = usize + 13;// 13 = Size + userKey + sequenceNumber + valueType 最大内存大小
            data = new byte[needed];

            kstart = Coding.encodeVarInt32(data, 0, usize + 8);
            System.arraycopy(userKey, 0, data, kstart, usize);

            Coding.encodeFixed64(data, kstart + usize, DbFormat.packSequenceAndType(sequenceNumber, ValueType.K_TYPE_VALUE));
            end = kstart + usize + 8;
        }

        public Slice memTableKey() {
            return new Slice(data, 0, end);
        }

        public Slice internalKey() {
            return new Slice(data, kstart, end - kstart);
        }

        public Slice userKey() {
            return new Slice(data, kstart, end - kstart - 8);
        }

    }
}
