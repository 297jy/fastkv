package com.zhuanyi.leveldb.core.table;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class SkipTableTest {

    private SkipTable<String> skipTableUnderTest;

    @Before
    public void setUp() {
        skipTableUnderTest = new SkipTable<>(null);
    }

    @Test
    public void testInsert() {
        // Setup
        // Run the test
        skipTableUnderTest.insert("key");

        // Verify the results
        assertTrue(skipTableUnderTest.contains("key"));

        int dataSize = 10000000;
        Set<String> datas = prepareDataSet(dataSize);
        long begin = System.currentTimeMillis();
        for (String data : datas) {
            skipTableUnderTest.insert(data);
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
        assertFalse(skipTableUnderTest.contains("key"));

        int dataSize = 10000000;
        Set<String> datas = prepareDataSet(dataSize);
        for (String data : datas) {
            skipTableUnderTest.insert(data);
        }

        long begin = System.currentTimeMillis();
        for (String data : datas) {
            //datas.contains(data);
            assertTrue(skipTableUnderTest.contains(data));
        }
        long end = System.currentTimeMillis();
        System.out.println("包含:" + dataSize + "个数据，耗时：" + (end - begin));
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
            skipTableUnderTest.insert(data);
        }

        TableIterator<String> result = skipTableUnderTest.iterator();
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
            skipTableUnderTest.insert(data);
        }

        long begin = System.currentTimeMillis();
        datas.contains("test100000");
        //assertTrue(skipListUnderTest.contains("test100000"));
        long end = System.currentTimeMillis();
        System.out.println("查找:" + dataSize + "个数据，耗时：" + (end - begin));

    }

}
