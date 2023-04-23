package com.zhuanyi.leveldb.test;

import com.zhuanyi.leveldb.core.common.Coding;

import java.util.*;

public class TestMain {

    public static void main(String[]args) {

        String abc = new String("test");
        System.out.println(Arrays.toString(abc.getBytes()));
        String abd = abc;

        Stack<String> ss = new Stack<>();

        byte[] x = new byte[8];
        Coding.encodeFixed64(x, 0, (1<<8) | 12);
        System.out.println(Arrays.toString(x));

        /**
        int c = 100;
        while(c > 0) {
            System.out.println(c % 2);
            c /= 2;
        }**/
    }
}
