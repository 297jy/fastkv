package com.zhuanyi.leveldb.core.benchmark;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LogBenchMark {

    public static void main(String[] args) {
        /**
         FileChannel fileChannelInput = null;
         fileChannelInput.write()
         ByteBuffer buffer = ByteBuffer.allocateDirect(48);
         BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream("test"));
         stream.write(buffer);
         buffer.put("123".getBytes(StandardCharsets.UTF_8));
         buffer.flip();
         buffer.clear();
         buffer.reset();**/

        long start = System.currentTimeMillis();
        List<ByteBuffer> buffers = new ArrayList<>();
        for (int i = 0; i < 10000000; i++) {
            /**
            try {
                FileOutputStream outputStream = new FileOutputStream("C:\\project\\leveldb-java\\db\\test.bin");
                FileChannel channel = outputStream.getChannel();
                channel.close();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }**/
            buffers.add(ByteBuffer.allocateDirect(100));
        }
        System.out.println("耗时：" + (System.currentTimeMillis() - start));

    }
}
