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

        int dataSize = 10000000;
        Set<String> datas = prepareDataSet(dataSize);
        long begin = System.currentTimeMillis();
        for (String data : datas) {
            skipListUnderTest.insert(data);
        }
        long end = System.currentTimeMillis();
        System.out.println("插入:" + dataSize + "个数据，耗时：" + (end - begin));

    }

    private Set<String> prepareDataSet(int setSize) {
        Set<String> data = new TreeSet<>();
        for (int i = 0; i < setSize; i++) {
            data.add("test" + i);
        }
        return data;
    }

    @Test
    public void testContains() {
        assertFalse(skipListUnderTest.contains("key"));

        int dataSize = 10000000;
        Set<String> datas = prepareDataSet(dataSize);
        for (String data : datas) {
            skipListUnderTest.insert(data);
        }

        long begin = System.currentTimeMillis();
        for (String data : datas) {
            //datas.contains(data);
            assertTrue(skipListUnderTest.contains(data));
        }
        long end = System.currentTimeMillis();
        System.out.println("包含:" + dataSize + "个数据，耗时：" + (end - begin));

        TableIterator<String> result = skipListUnderTest.iterator();
        int[] heights = new int[13];
        while (true) {
            result.next();
            if (!result.valid()) {
                break;
            }
            heights[result.height()]++;
        }
        for (int i = 1; i <= 12; i++) {
            System.out.println("高度为：" + i + ",数量为:" + heights[i]);
        }
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

    @Test
    public void benchmark() {
        int dataSize = 10000000;
        Set<String> datas = prepareDataSet(dataSize);

        for (String data : datas) {
            skipListUnderTest.insert(data);
        }

        long begin = System.currentTimeMillis();
        datas.contains("test100000");
        //assertTrue(skipListUnderTest.contains("test100000"));
        long end = System.currentTimeMillis();
        System.out.println("查找:" + dataSize + "个数据，耗时：" + (end - begin));

    }

}
