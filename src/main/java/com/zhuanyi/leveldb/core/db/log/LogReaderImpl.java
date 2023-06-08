package com.zhuanyi.leveldb.core.db.log;

import com.zhuanyi.leveldb.core.common.Result;
import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.common.Status;
import com.zhuanyi.leveldb.core.store.SequentialFile;

import java.util.ArrayList;
import java.util.List;

public class LogReaderImpl implements LogReader {

    private SequentialFile dest;

    private List<Slice> buffers;

    private Slice buffer;

    private long endOfBufferOffset;

    private boolean eof;

    /**
     * 是否检查crc循环校验和
     */
    private boolean checkSum;

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
                    endOfBufferOffset += status.isOk() ? buffer.readableBytes() : 0;
                    if (!status.isOk()) {
                        buffers.clear();
                        eof = true;
                        return LogFormat.RecordType.K_EOF;
                    }else {
                        if (buffer.readableBytes() < LogFormat.K_MAX_RECORD_TYPE) {
                            eof = true;
                        }
                    }
                    continue;
                } else {
                    buffers.clear();
                    return LogFormat.RecordType.K_EOF;
                }
            }

            long actualCrc = buffer.readLong();
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
            if (LogFormat.RecordType.K_ZERO_TYPE == recordType && length == 0) {
                buffer.clear();
                return LogFormat.RecordType.K_BAD_RECORD;
            }

            if (checkSum) {
                long expectCrc = buffer.crc32();
                if (expectCrc != actualCrc) {
                    buffer.clear();
                    return LogFormat.RecordType.K_BAD_RECORD;
                }
            }

            result.reload(buffer);
            return recordType;
        }
    }

}
