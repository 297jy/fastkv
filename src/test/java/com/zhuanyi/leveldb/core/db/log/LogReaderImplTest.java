package com.zhuanyi.leveldb.core.db.log;

import com.zhuanyi.leveldb.core.common.Result;
import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.common.Status;
import com.zhuanyi.leveldb.core.store.MappedSequentialFile;
import com.zhuanyi.leveldb.core.store.MappedWritableFile;
import com.zhuanyi.leveldb.core.store.SequentialFile;
import com.zhuanyi.leveldb.core.store.WritableFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


@RunWith(MockitoJUnitRunner.class)
public class LogReaderImplTest {

    private SequentialFile readDest;

    private WritableFile writeDest;

    private LogReaderImpl logReader;

    private LogWriterImpl logWriter;

    @Before
    public void setUp() throws Exception {
        readDest = new MappedSequentialFile("C:\\project\\leveldb-java\\db\\test.bin");
        writeDest = new MappedWritableFile("C:\\project\\leveldb-java\\db\\test.bin", 1024 * 1024 * 128);
        logReader = new LogReaderImpl(readDest);
        logWriter = new LogWriterImpl(writeDest);
    }

    @Test
    public void testReadRecord() {
        List<String> tests = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            long num = System.currentTimeMillis();
            Slice old = new Slice(String.valueOf(num).getBytes());
            Status status = logWriter.addRecord(old);
            tests.add(num+"");
        }

        List<String> records = new ArrayList<>();
        while (true) {
            Result<Slice> result = logReader.readRecord();
            if (result.success()) {
                records.add(result.getData().value());
            } else {
                break;
            }
        }

        assertEquals(tests, records);
    }

}
