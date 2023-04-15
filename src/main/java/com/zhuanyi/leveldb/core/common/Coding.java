package com.zhuanyi.leveldb.core.common;

import javafx.util.Pair;

import java.util.Arrays;

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
