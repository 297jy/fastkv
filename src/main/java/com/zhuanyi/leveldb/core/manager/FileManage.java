package com.zhuanyi.leveldb.core.manager;

import com.zhuanyi.leveldb.core.config.LogConfiguration;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

@Component
public class FileManage {

    @Resource
    private LogConfiguration logConfiguration;

    public FileChannel getLogFileChannel() {
        try {
            FileOutputStream inputStream = new FileOutputStream(logConfiguration.getPath());
            return inputStream.getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
