package com.zhuanyi.leveldb.core.table;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class SkipListTest {

    private SkipList<String> skipListUnderTest;

    @Before
    public void setUp() {
        skipListUnderTest = new SkipList<>();
    }

    @Test
    public void testInsert() {
        // Setup
        // Run the test
        skipListUnderTest.insert("key");

        // Verify the results
        assertTrue(skipListUnderTest.contains("key"));

        Set<String> datas = prepareDataSet(10000000);
        long begin = System.currentTimeMillis();
        for (String data : datas) {
            skipListUnderTest.insert(data);
        }
        long end = System.currentTimeMillis();
        System.out.println("插入:" + 100000 + "个数据，耗时：" + (end - begin));

    }

    private Set<String> prepareDataSet(int setSize) {
        Set<String> data = new TreeSet<>();
        for (int i = 0; i < setSize; i++) {
            data.add("test" + new Random().nextInt());
        }
        return data;
    }

    @Test
    public void testContains() {
        assertFalse(skipListUnderTest.contains("key"));

        Set<String> datas = prepareDataSet(1000000);
        for (String data : datas) {
            skipListUnderTest.insert(data);
        }

        long begin = System.currentTimeMillis();
        for (String data : datas) {
            assertTrue(skipListUnderTest.contains(data));
        }
        long end = System.currentTimeMillis();
        System.out.println("包含:" + 1000000 + "个数据，耗时：" + (end - begin));
    }

    @Test
    public void testIterator() {
        // Setup
        // Run the test

        Set<String> datas = new TreeSet<>();
        datas.add("test1");
        datas.add("test2");
        datas.add("test3");
        for (String data : datas) {
            skipListUnderTest.insert(data);
        }

        TableIterator<String> result = skipListUnderTest.iterator();
        for (String tt : datas) {
            result.next();
            assertEquals(tt, result.key());
        }

        result.prev();
        assertEquals("test2", result.key());

        result.seek("test1");
        assertEquals("test1", result.key());

        result.seekToLast();
        assertEquals("test3", result.key());

        result.seekToFirst();
        assertEquals("test1", result.key());
        // Verify the results
    }

}
