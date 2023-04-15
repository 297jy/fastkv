package com.zhuanyi.leveldb.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

public class TestMain {

    public static void main(String[]args) {

        String abc = new String("test");
        String abd = abc;

        Stack<String> ss = new Stack<>();

        int x = 1;
        System.out.println((x >> 31) & 1);
    }
}
