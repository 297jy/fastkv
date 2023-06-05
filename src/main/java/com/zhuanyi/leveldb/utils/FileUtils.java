package com.zhuanyi.leveldb.utils;

import com.zhuanyi.leveldb.core.common.Status;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.io.*;

@Slf4j
public class FileUtils {

    public static String makeFileName(final String dirName, final String fileName, final String suffix) {
        return dirName + File.separator + fileName + "." + suffix;
    }

    public static void ensureDirExist(final String dirName, final String pathSplitter) {
        if (StringUtils.isNotEmpty(dirName)) {
            if (dirName.contains(pathSplitter)) {
                String[] dirs = dirName.trim().split(pathSplitter);
                for (String dir : dirs) {
                    createDirIfNotExist(dir);
                }
            } else {
                createDirIfNotExist(dirName);
            }
        }
    }

    public static String getDir(final String fileName) {
        File file = new File(fileName);
        return file.getParent();
    }

    private static void createDirIfNotExist(String dirName) {
        File f = new File(dirName);
        if (!f.exists()) {
            boolean result = f.mkdirs();
            log.info(dirName + " mkdir " + (result ? "OK" : "Failed"));
        }
    }

    public static Status syncFile(String filename) {
        try (FileInputStream fis = new FileInputStream(filename)) {
            FileDescriptor fd = fis.getFD();
            fd.sync();
            return Status.ok();
        } catch (IOException e) {
            log.error("syncFile,{1}", e);
            return Status.ioError(e.getLocalizedMessage());
        }
    }
}
