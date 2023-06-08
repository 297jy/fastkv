package com.zhuanyi.leveldb.core.db.log;

import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.common.Status;
import com.zhuanyi.leveldb.core.store.MappedWritableFile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LogWriterImplTest {

    @Test
    public void addRecord() throws Exception{

        LogWriterImpl logWriter = new LogWriterImpl(new MappedWritableFile("C:\\project\\leveldb-java\\db\\test.bin", 1024 * 1024 * 128));
        Status status = logWriter.addRecord(new Slice("123".getBytes()));
        assertTrue(status.isOk());

    }
}