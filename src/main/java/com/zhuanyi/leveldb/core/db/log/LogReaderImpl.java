package com.zhuanyi.leveldb.core.db.log;

import com.zhuanyi.leveldb.core.common.Result;
import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.common.Status;
import com.zhuanyi.leveldb.core.store.SequentialFile;

import java.util.ArrayList;
import java.util.List;

public class LogReaderImpl implements LogReader {

    private SequentialFile dest;

    private Slice buffer;

    private boolean eof;

    /**
     * 是否检查crc循环校验和
     */
    private boolean checkSum;

    public LogReaderImpl(SequentialFile dest) {
        this.dest = dest;
        this.buffer = new Slice(LogFormat.K_BLOCK_SIZE);
        this.checkSum = false;
        this.eof = false;
    }

    @Override
    public Result<Slice> readRecord() {

        List<Slice> allFragments = new ArrayList<>();
        Slice fragment = new Slice();
        while (true) {
            LogFormat.RecordType recordType = readPhysicalRecord(fragment);
            if (recordType == LogFormat.RecordType.K_FULL_TYPE) {
                return Result.success(fragment.copy());
            }
            if (recordType == LogFormat.RecordType.K_FIRST_TYPE || recordType == LogFormat.RecordType.K_MIDDLE_TYPE) {
                allFragments.add(fragment.copy());
                continue;
            }
            if (recordType == LogFormat.RecordType.K_LAST_TYPE) {
                allFragments.add(fragment.copy());
                return Result.success(Slice.merge(allFragments));
            }
            if (recordType == LogFormat.RecordType.K_EOF) {
                return Result.fail(Status.eof());
            }
            if (recordType == LogFormat.RecordType.K_BAD_RECORD) {
                return Result.fail(Status.badRecord());
            }
            return Result.fail(Status.unKnown());
        }
    }

    @Override
    public LogFormat.RecordType readPhysicalRecord(Slice result) {
        while (true) {
            // 如果当前缓存区中存在的字节数少于数据头，说明当前数据块已经读取完毕，需要读取新的数据块
            if (buffer.readableBytes() < LogFormat.K_HEADER_SIZE) {
                if (!eof) {
                    buffer.clear();
                    Status status = dest.read(LogFormat.K_BLOCK_SIZE, buffer);
                    System.out.println(buffer);
                    if (!status.isOk()) {
                        buffer.clear();
                        eof = true;
                        return LogFormat.RecordType.K_EOF;
                    }else {
                        if (buffer.readableBytes() < LogFormat.K_MAX_RECORD_TYPE) {
                            eof = true;
                        }
                    }
                    continue;
                } else {
                    buffer.clear();
                    return LogFormat.RecordType.K_EOF;
                }
            }

            long actualCrc = buffer.readInt();
            byte a = buffer.readByte();
            byte b = buffer.readByte();
            LogFormat.RecordType recordType = LogFormat.RecordType.valueOf(buffer.readByte());
            int length = a | (b << 8);
            if (buffer.readableBytes() < length) {
                buffer.clear();
                if (!eof) {
                    return LogFormat.RecordType.K_BAD_RECORD;
                }
                return LogFormat.RecordType.K_EOF;
            }
            System.out.println("readFragmentLen:"+length);
            System.out.println("readFragmentType:"+recordType);
            if (LogFormat.RecordType.K_ZERO_TYPE == recordType && length == 0) {
                buffer.clear();
                return LogFormat.RecordType.K_BAD_RECORD;
            }

            result.reload(buffer.read(length));
            if (checkSum) {
                long expectCrc = result.crc32();
                if (expectCrc != actualCrc) {
                    buffer.clear();
                    return LogFormat.RecordType.K_BAD_RECORD;
                }
            }

            return recordType;
        }
    }

}
