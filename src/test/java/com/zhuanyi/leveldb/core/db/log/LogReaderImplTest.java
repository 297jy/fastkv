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
import org.springframework.boot.web.server.Ssl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LogReaderImplTest {

    private SequentialFile readDest;

    private WritableFile writeDest;

    private LogReaderImpl logReader;

    private LogWriterImpl logWriter;

    @Before
    public void setUp() throws Exception{
        readDest = new MappedSequentialFile("C:\\project\\leveldb-java\\db\\test.bin");
        writeDest = new MappedWritableFile("C:\\project\\leveldb-java\\db\\test.bin", 1024 * 1024 * 128);
        logReader = new LogReaderImpl(readDest);
        logWriter = new LogWriterImpl(writeDest);
    }

    @Test
    public void testReadRecord() {

        Slice old = new Slice("123".getBytes());
        Status status = logWriter.addRecord(old);
        Result<Slice> result = logReader.readRecord();
        System.out.println(result.getStatus());
        assertTrue(result.success());
        assertEquals(result.getData().compareTo(old), 0);
        System.out.println(result.getData());
    }

}
