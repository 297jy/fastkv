package com.zhuanyi.leveldb.core.db.log;

import com.zhuanyi.leveldb.core.common.Slice;
import jakarta.annotation.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LogWriterImplTest {

    @Resource
    private LogWriter logWriter;
    @Test
    public void addRecord() {

        logWriter.addRecord(new Slice("123".getBytes()));
    }
}