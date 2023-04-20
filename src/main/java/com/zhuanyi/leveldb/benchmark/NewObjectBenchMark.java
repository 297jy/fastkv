package com.zhuanyi.leveldb.benchmark;

import com.zhuanyi.leveldb.core.common.Slice;

public class NewObjectBenchMark {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        for (long i = 0; i < 10000000000L; i++) {
            new Slice();
        }
        System.out.println(System.currentTimeMillis() - start);
    }
}
