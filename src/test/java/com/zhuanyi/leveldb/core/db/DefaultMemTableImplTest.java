package com.zhuanyi.leveldb.core.db;

import com.zhuanyi.leveldb.common.Result;
import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.db.enums.ValueType;
import com.zhuanyi.leveldb.core.db.format.DbFormat;
import com.zhuanyi.leveldb.core.db.format.LookupKey;
import org.junit.Before;
import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.assertEquals;

public class DefaultMemTableImplTest {

    private DefaultMemTableImpl defaultMemTableImplUnderTest;

    @Before
    public void setUp() {
        defaultMemTableImplUnderTest = new DefaultMemTableImpl(
                new DbFormat.InternalKeyComparator(Comparator.comparing(Object::toString)));
    }

    @Test
    public void testApproximateMemoryUsage() {
        assertEquals(0L, defaultMemTableImplUnderTest.approximateMemoryUsage());
    }

    @Test
    public void testNewIterator() {
        // Setup
        // Run the test
        final MemTableIterator<Slice> result = defaultMemTableImplUnderTest.newIterator();

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
        final Result<Slice> result = defaultMemTableImplUnderTest.get(key);

        // Verify the results
    }
}
