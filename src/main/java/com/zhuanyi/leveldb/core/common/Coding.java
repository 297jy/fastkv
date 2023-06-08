package com.zhuanyi.leveldb.core.common;


import com.zhuanyi.leveldb.common.Pair;

import java.nio.ByteBuffer;

public class Coding {

    private static final int B = 128;

    public static int encodeVarInt32(byte[] dst, int begin, int value) {
        // 这个编码方案是没法对负数编码的，确保value值是正数
        assert (value >= 0);

        if (value < (1 << 7)) {
            dst[begin++] = (byte) value;
        } else if (value < (1 << 14)) {
            dst[begin++] = (byte) (value | B);
            dst[begin++] = (byte) (value >> 7);
        } else if (value < (1 << 21)) {
            dst[begin++] = (byte) (value | B);
            dst[begin++] = (byte) ((value >> 7) | B);
            dst[begin++] = (byte) (value >> 14);
        } else if (value < (1 << 28)) {
            dst[begin++] = (byte) (value | B);
            dst[begin++] = (byte) ((value >> 7) | B);
            dst[begin++] = (byte) ((value >> 14) | B);
            dst[begin++] = (byte) (value >> 21);
        } else {
            dst[begin++] = (byte) (value | B);
            dst[begin++] = (byte) ((value >> 7) | B);
            dst[begin++] = (byte) ((value >> 14) | B);
            dst[begin++] = (byte) ((value >> 21) | B);
            dst[begin++] = 0;
        }
        return begin;
    }

    public static int varIntLength(int value) {
        int len = 1;
        while (value >= B) {
            value >>= 7;
            len++;
        }
        return len;
    }

    public static void encodeFixed64(byte[] dst, int begin, long value) {
        dst[begin++] = (byte) value;
        dst[begin++] = (byte) (value >> 8);
        dst[begin++] = (byte) (value >> 16);
        dst[begin++] = (byte) (value >> 24);
        dst[begin++] = (byte) (value >> 32);
        dst[begin++] = (byte) (value >> 40);
        dst[begin++] = (byte) (value >> 48);
        dst[begin] = (byte) (value >> 56);
    }

    public static void encodeFixed32(byte[] dst, int begin, long value) {
        dst[begin++] = (byte) value;
        dst[begin++] = (byte) (value >> 8);
        dst[begin++] = (byte) (value >> 16);
        dst[begin] = (byte) (value >> 24);
    }

    public static long decodeFixed64(byte[] dst, int begin) {
        return dst[begin++]
                | (dst[begin++] << 8)
                | (dst[begin++] << 16)
                | (dst[begin++] << 24)
                | ((long) dst[begin++] << 32)
                | ((long) dst[begin++] << 40)
                | ((long) dst[begin++] << 48)
                | ((long) dst[begin] << 56);
    }

    public static long decodeFixed32(byte[] dst, int begin) {
        return dst[begin++]
                | (dst[begin++] << 8)
                | (dst[begin++] << 16)
                | (dst[begin] << 24);
    }

    public static void encodeFixed32ToBuffer(ByteBuffer dst, int num) {
        dst.put((byte) (num & 0xff));
        dst.put((byte) ((num >> 8) & 0xff));
        dst.put((byte) ((num >> 16) & 0xff));
        dst.put((byte) ((num >> 24) & 0xff));
    }

    public static void putFixed64(byte[] dst, int begin, long value) {
        byte[] buffer = new byte[8];
        encodeFixed64(buffer, 0, value);
        System.arraycopy(dst, begin, buffer, 0, 8);
    }

    public static Pair<Integer, Integer> getVarInt32Ptr(byte[] dst, int begin, int end) {
        if (begin < end) {
            if ((dst[begin] & 128) == 0) {
                return new Pair<>(begin + 1, (int) dst[begin]);
            }
        }
        return getVarInt32PtrFallback(dst, begin, end);
    }

    public static Pair<Integer, Integer> getVarInt32PtrFallback(byte[] dst, int begin, int end) {
        int result = 0;
        for (int shift = 0; shift <= 28 && begin < end; shift += 7) {
            byte b = dst[begin++];
            if ((b & B) > 0) {
                result |= ((b & 127) << shift);
            } else {
                result |= (b << shift);
                return new Pair<>(begin, result);
            }
        }
        return null;
    }

    public static void main(String[] args) {
        byte[] dst = new byte[10];
        int end = encodeVarInt32(dst, 0, 1);
        System.out.println(getVarInt32PtrFallback(dst, 0, end));
        //System.out.println(Arrays.toString(dst));
    }
}
