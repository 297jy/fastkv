package com.zhuanyi.leveldb.core.db;

import com.zhuanyi.leveldb.common.Result;
import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.db.enums.ValueType;
import com.zhuanyi.leveldb.core.db.format.DbFormat;
import com.zhuanyi.leveldb.core.db.format.LookupKey;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DefaultMemTableImplTest {

    private DefaultMemTableImpl defaultMemTableImplUnderTest;

    @Before
    public void setUp() {
        defaultMemTableImplUnderTest = new DefaultMemTableImpl(
                new DbFormat.InternalKeyComparator(null));
        Slice keySlice = new Slice("key1".getBytes());
        Slice value = new Slice("value1".getBytes());
        defaultMemTableImplUnderTest.add(1L, ValueType.K_TYPE_VALUE, keySlice, value);

        keySlice = new Slice("key2".getBytes());
        value = new Slice("value2".getBytes());
        defaultMemTableImplUnderTest.add(2L, ValueType.K_TYPE_VALUE, keySlice, value);

        keySlice = new Slice("key3".getBytes());
        value = new Slice("value3".getBytes());
        defaultMemTableImplUnderTest.add(3L, ValueType.K_TYPE_VALUE, keySlice, value);
    }

    @Test
    public void testApproximateMemoryUsage() {
        System.out.println(defaultMemTableImplUnderTest.approximateMemoryUsage());
    }

    @Test
    public void testNewIterator() {
        // Setup
        // Run the test
        final MemTableIterator<Slice> it = defaultMemTableImplUnderTest.newIterator();
        it.seekToFirst();
        while (it.valid()) {
            System.out.println(it.key() + "_" + it.value());
            it.next();
        }
        // Verify the results
    }

    @Test
    public void testAdd() {
        // Setup
        final Slice key = new Slice("key".getBytes());
        final Slice value = new Slice("value".getBytes());

        // Run the test
        defaultMemTableImplUnderTest.add(0L, ValueType.K_TYPE_VALUE, key, value);

        Result<Slice> sliceResult = defaultMemTableImplUnderTest.get(new LookupKey("key".getBytes(), 1L));
        System.out.println(sliceResult.getValue().toString());
        // Verify the results
    }

    @Test
    public void testGet() {
        // Setup
        final LookupKey key = new LookupKey("content".getBytes(), 0L);

        // Run the test
        Result<Slice> result = defaultMemTableImplUnderTest.get(key);
        assertFalse(result.success());
        System.out.println(result.getStatus());

        final Slice keySlice = new Slice("key".getBytes());
        final Slice value = new Slice("value1".getBytes());
        defaultMemTableImplUnderTest.add(1L, ValueType.K_TYPE_VALUE, keySlice, value);
        result = defaultMemTableImplUnderTest.get(new LookupKey("key".getBytes(), 2L));
        assertEquals(value.toString(), "value1");
        System.out.println(result.getValue());

        // 先删除后查询
        defaultMemTableImplUnderTest.add(3L, ValueType.K_TYPE_DELETION, keySlice, new Slice());
        result = defaultMemTableImplUnderTest.get(new LookupKey("key".getBytes(), 4L));
        assertFalse(result.success());
        System.out.println(result);

        // Verify the results
    }

    @Test
    public void testLookUpKey() {
        final LookupKey key = new LookupKey("key".getBytes(), 2L);
        System.out.println(key.memTableKey());
    }
}
