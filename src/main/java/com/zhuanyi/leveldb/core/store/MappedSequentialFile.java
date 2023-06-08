package com.zhuanyi.leveldb.core.store;

import com.zhuanyi.leveldb.core.common.Result;
import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.common.Status;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;


public class MappedSequentialFile implements SequentialFile {

    private final BufferedInputStream inputStream;

    public MappedSequentialFile(final String fileName) throws IOException {
        inputStream = new BufferedInputStream(new FileInputStream(fileName));
    }

    @Override
    public Status read(int n, Slice buffer) {
        int writeBytes = buffer.writeFromInputStream(inputStream, n);
        if (writeBytes <= 0) {
            return Status.eof();
        }
        return Status.ok();
    }

    @Override
    public Status skip(int n) {
        try {
            inputStream.skip(n);
            return Status.ok();
        } catch (IOException e) {
            return Status.ioError(e.getLocalizedMessage());
        }
    }

    @Override
    public Status close() {
        try {
            inputStream.close();
            return Status.ok();
        } catch (IOException e) {
            return Status.ioError(e.getLocalizedMessage());
        }
    }
}
