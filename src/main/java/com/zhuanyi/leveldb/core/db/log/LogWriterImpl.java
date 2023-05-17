package com.zhuanyi.leveldb.core.db.log;

import com.zhuanyi.leveldb.core.common.Coding;
import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.common.Status;
import com.zhuanyi.leveldb.core.manager.FileManage;
import com.zhuanyi.leveldb.core.utils.ObjectPools;
import com.zhuanyi.leveldb.core.utils.SimpleObjectPoolsImpl;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Component
@Scope("singleton")
public class LogWriterImpl implements LogWriter {

    private final static ObjectPools<ByteBuffer> pools = new SimpleObjectPoolsImpl<>(() -> ByteBuffer.allocateDirect(LogFormat.K_BLOCK_SIZE), ByteBuffer::clear);

    private int blockOffset;

    //private FileChannel fileWriteChannel;

    @Resource
    private FileManage fileManage;

    public LogWriterImpl() {
        //fileWriteChannel = fileManage.getLogFileChannel();
    }

    @Override
    public Status addRecord(Slice slice) {
        ByteBuffer dest = pools.allocateObject(null);

        FileChannel fileWriteChannel = fileManage.getLogFileChannel();
        boolean begin = true;
        while (slice.getSize() > 0) {
            int avail = LogFormat.K_BLOCK_SIZE - blockOffset - LogFormat.K_HEADER_SIZE;
            int fragmentLen = Math.min(slice.getSize(), avail);

            LogFormat.RecordType type;
            boolean end = slice.getSize() == fragmentLen;
            if (begin && end) {
                type = LogFormat.RecordType.K_FULL_TYPE;
            } else if (begin) {
                type = LogFormat.RecordType.K_FIRST_TYPE;
            } else if (end) {
                type = LogFormat.RecordType.K_LAST_TYPE;
            } else {
                type = LogFormat.RecordType.K_MIDDLE_TYPE;
            }

            Status s = emitPhysicalRecord(fileWriteChannel, dest, type, slice, fragmentLen);
            if (!s.isOk()) {
                return s;
            }
            begin = false;
        }

        try {
            fileWriteChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Status.ok();
    }

    private Status emitPhysicalRecord(FileChannel fileWriteChannel, ByteBuffer dest, LogFormat.RecordType t, Slice slice, int len) {
        long crc32 = slice.crc32(len);
        Coding.encodeFixed32ToBuffer(dest, (int) crc32);

        // 设置 log record数据部分的长度
        dest.put((byte) (len & 0xff));
        dest.put((byte) (len >> 8));
        // 设置记录类型
        dest.put((byte) t.getCode());

        slice.readToByteBuffer(dest, len);

        blockOffset += LogFormat.K_HEADER_SIZE + len;

        int leftover = LogFormat.K_BLOCK_SIZE - blockOffset;
        // 如果这个块剩余的空间连log record的头部都放不下，那么就把剩余的空间都填充成0
        if (leftover < LogFormat.K_HEADER_SIZE) {
            fillZero(leftover, dest);
        }

        if (write(fileWriteChannel, dest)) {
            return Status.ok();
        }
        return Status.iOError();
    }

    private void fillZero(int leftover, ByteBuffer dest) {
        for (int i = 0; i < leftover; i++) {
            dest.put((byte) 0);
        }
    }

    private boolean write(FileChannel fileWriteChannel, ByteBuffer dest) {
        try {
            fileWriteChannel.write(dest);
            //dest.clear();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
