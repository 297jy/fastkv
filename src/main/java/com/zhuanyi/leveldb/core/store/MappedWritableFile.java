package com.zhuanyi.leveldb.core.store;

import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.common.Status;
import com.zhuanyi.leveldb.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class MappedWritableFile implements WritableFile {

    /**
     * 页面大小32KB
     */
    public static final int OS_PAGE_SIZE = 1024 * 32;

    public static final int BUFFER_SIZE = 1024 * 64;

    private static final AtomicLong TOTAL_MAPPED_VIRTUAL_MEMORY = new AtomicLong(0);

    private static final AtomicInteger TOTAL_MAPPED_FILES = new AtomicInteger(0);
    protected final AtomicInteger wrotePosition = new AtomicInteger(0);

    /**
     * 简单的一个缓冲区
     */
    private byte[] buffer = new byte[BUFFER_SIZE];
    /**
     * 指向当前缓冲区空闲位置指针
     */
    private int pos;

    private String fileName;

    private String dirName;

    protected int fileSize;
    protected FileChannel fileChannel;
    private MappedByteBuffer mappedByteBuffer;
    private boolean manifestFlag;
    private File file;

    public MappedWritableFile(final String fileName, final int fileSize) throws IOException {
        init(fileName, fileSize);
    }

    private void init(final String fileName, final int fileSize) throws IOException {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.file = new File(fileName);
        this.dirName = file.getParent();

        boolean ok = false;
        FileUtils.ensureDirExist(dirName, ",");

        try {
            this.fileChannel = new RandomAccessFile(this.file, "rw").getChannel();
            this.mappedByteBuffer = this.fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
            TOTAL_MAPPED_VIRTUAL_MEMORY.addAndGet(fileSize);
            TOTAL_MAPPED_FILES.incrementAndGet();
            ok = true;
        } catch (FileNotFoundException e) {
            log.error("Failed to create file " + this.fileName, e);
            throw e;
        } catch (IOException e) {
            log.error("Failed to map file " + this.fileName, e);
            throw e;
        } finally {
            if (!ok && this.fileChannel != null) {
                this.fileChannel.close();
            }
        }

    }

    public static void clean(final ByteBuffer buffer) {
        if (buffer == null || !buffer.isDirect() || buffer.capacity() == 0)
            return;
        invoke(invoke(viewed(buffer), "cleaner"), "clean");
    }

    @SuppressWarnings("removal")
    private static Object invoke(final Object target, final String methodName, final Class<?>... args) {
        return AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                Method method = method(target, methodName, args);
                method.setAccessible(true);
                return method.invoke(target);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private static Method method(Object target, String methodName, Class<?>[] args)
            throws NoSuchMethodException {
        try {
            return target.getClass().getMethod(methodName, args);
        } catch (NoSuchMethodException e) {
            return target.getClass().getDeclaredMethod(methodName, args);
        }
    }

    private static ByteBuffer viewed(ByteBuffer buffer) {
        String methodName = "viewedBuffer";
        Method[] methods = buffer.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("attachment")) {
                methodName = "attachment";
                break;
            }
        }

        ByteBuffer viewedBuffer = (ByteBuffer) invoke(buffer, methodName);
        if (viewedBuffer == null)
            return buffer;
        else
            return viewed(viewedBuffer);
    }

    @Override
    public Status append(Slice data) {
        int writeSize = data.readableBytes();
        // 计算本次可写入缓冲区的数据长度
        int copySize = Math.min(data.readableBytes(), BUFFER_SIZE - pos);
        data.readToBytes(buffer, pos, copySize);
        pos += copySize;
        writeSize -= copySize;
        if (data.isEmpty()) {
            return Status.ok();
        }

        Status status = flushBuffer();
        if (!status.isOk()) {
            return status;
        }

        // 需要写入的数据长度小于缓冲区，则优先写入缓冲区
        if (writeSize < BUFFER_SIZE) {
            data.readToBytes(buffer, 0, writeSize);
            pos += writeSize;
            return Status.ok();
        }

        // 否则直接将全部数据写入到PageCache中
        return writeUnbuffered(data);
    }

    /**
     * 刷新缓冲区数据到PageCache中
     *
     * @return
     */
    private Status flushBuffer() {
        Status status = writeUnbuffered(buffer, pos);
        // 不需要重新new一个新数组，只需要将缓冲区的数据长度设置为0
        pos = 0;
        return status;
    }

    private Status writeUnbuffered(byte[] data, int len) {
        int currentPos = this.wrotePosition.get();

        if ((currentPos + len) <= this.fileSize) {
            try {
                ByteBuffer buf = this.mappedByteBuffer.slice();
                buf.position(currentPos);
                buf.put(data, 0, len);
            } catch (Throwable e) {
                log.error("Error occurred when append message to mappedFile.", e);
            }
            this.wrotePosition.addAndGet(len);
            return Status.ok();
        }
        return Status.ioError("");
    }

    private Status writeUnbuffered(Slice data) {
        int currentPos = this.wrotePosition.get();
        int dataSize = data.readableBytes();
        if ((currentPos + dataSize) <= this.fileSize) {
            try {
                ByteBuffer buf = this.mappedByteBuffer.slice();
                buf.position(currentPos);
                data.readToByteBuffer(buf, dataSize);
            } catch (Throwable e) {
                log.error("Error occurred when append message to mappedFile.", e);
            }
            this.wrotePosition.addAndGet(dataSize);
            return Status.ok();
        }
        return Status.ioError("");
    }

    @Override
    public Status close() {
        Status status = flushBuffer();
        clean(this.mappedByteBuffer);
        TOTAL_MAPPED_VIRTUAL_MEMORY.addAndGet(this.fileSize * (-1));
        TOTAL_MAPPED_FILES.decrementAndGet();
        log.info("unmap file: " + this.fileName + " OK");
        return status;
    }

    @Override
    public Status flush() {
        return flushBuffer();
    }

    @Override
    public Status sync() {

        Status status = syncDirIfManifest();
        if (!status.isOk()) {
            return status;
        }

        this.mappedByteBuffer.force();
        return Status.ok();
    }

    /**
     * fsync()通常会涉及至少两个I/O操作: 一是回写修改的数据，而是更新索引节点的修改时间戳。
     * 因为索引节点和文件数据在磁盘上可能不是紧挨着的–因此会带来很高的seek操作–在很多场景下，关注正确的事务顺序，
     * 但是不包括那些对于以后访问文件无关紧要的元数据(比如修改时间戳)，使用fdatasync()是提高性能的简单方式。
     * 这两个函数都不保证任何已经更新的包含该文件的目录项会同步到磁盘上。这意味着如果文件链接最近刚更新，文件数据可能会成功写入磁盘，
     * 但是却没有更新的相关的目录中，导致文件不可用。为了保证对目录项的更新也都同步到磁盘上，必须对文件目录也调用fsync()进行同步。
     *
     * @return
     */
    private Status syncDirIfManifest() {
        if (!manifestFlag) {
            return Status.ok();
        }

        return FileUtils.syncFile(dirName);
    }

    public static void main(String[]args) {

    }

}
