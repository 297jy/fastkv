package com.zhuanyi.leveldb.core.db.log;

import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.common.Status;
import com.zhuanyi.leveldb.core.store.WritableFile;
import lombok.Data;

@Data
public class LogWriterImpl implements LogWriter {

    private int blockOffset;
    private WritableFile dest;

    public LogWriterImpl(WritableFile dest) {
        this.dest = dest;
    }

    @Override
    public Status addRecord(Slice slice) {
        Slice data = slice.duplicate();
        int left = data.readableBytes();

        Status s;
        boolean begin = true;
        do {
            // 计算当前数据页剩余的可用空间
            int leftOver = LogFormat.K_BLOCK_SIZE - blockOffset;
            if (leftOver < LogFormat.K_HEADER_SIZE) {
                if (leftOver > 0) {
                    dest.append(Slice.allocate(leftOver));
                }
                blockOffset = 0;
            }

            int avail = LogFormat.K_BLOCK_SIZE - blockOffset - LogFormat.K_HEADER_SIZE;
            int fragmentLen = Math.min(left, avail);

            LogFormat.RecordType type;
            boolean end = left == fragmentLen;
            if (begin && end) {
                type = LogFormat.RecordType.K_FULL_TYPE;
            } else if (begin) {
                type = LogFormat.RecordType.K_FIRST_TYPE;
            } else if (end) {
                type = LogFormat.RecordType.K_LAST_TYPE;
            } else {
                type = LogFormat.RecordType.K_MIDDLE_TYPE;
            }

            s = emitPhysicalRecord(type, data.read(fragmentLen));
            left -= fragmentLen;
            begin = false;
        } while (s.isOk() && left > 0);
        return s;
    }

    private Status emitPhysicalRecord(LogFormat.RecordType t, Slice data) {
        int len = data.readableBytes();
        int totalLen = LogFormat.K_HEADER_SIZE + len;
        int crc32 = data.crc32();
        // 一个数据帧的长度为：数据头+数据部分的长度
        Slice fragment = new Slice(totalLen);
        System.out.println("emitPhysicalRecordc32:" + crc32);
        fragment.writeInt(crc32);
        // 写入数据头部分的数据
        fragment.write(new byte[] {
                (byte) (len & 0xff),
                (byte) (len >> 8),
                (byte) t.getCode()
        });
        fragment.write(data);
        System.out.println("writeFragmentLen:"+len);
        System.out.println("writeFragmentType:"+t);

        Status s = dest.append(fragment);
        if (s.isOk()) {
            s = dest.flush();
        }
        blockOffset += totalLen;
        return s;
    }
}
