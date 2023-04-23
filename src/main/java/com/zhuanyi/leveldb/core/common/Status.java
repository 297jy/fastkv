package com.zhuanyi.leveldb.core.common;

import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于记录leveldb中状态信息
 */
public class Status {

    private enum Code {
        K_OK,
        K_NOT_FOUND,
        K_CORRUPTION,
        K_NOT_SUPPORTED,
        K_INVALID_ARGUMENT,
        K_IO_ERROR,
        K_UNKNOWN
    }

    private final static Map<Integer, String> CODE_MSG_MAP = new HashMap<>(16);

    static {
        CODE_MSG_MAP.put(Code.K_OK.ordinal(), "OK");
        CODE_MSG_MAP.put(Code.K_NOT_FOUND.ordinal(), "NOT_FOUND:");
        CODE_MSG_MAP.put(Code.K_CORRUPTION.ordinal(), "CORRUPTION:");
        CODE_MSG_MAP.put(Code.K_NOT_SUPPORTED.ordinal(), "Not implemented: ");
        CODE_MSG_MAP.put(Code.K_INVALID_ARGUMENT.ordinal(), "Invalid argument: ");
        CODE_MSG_MAP.put(Code.K_IO_ERROR.ordinal(), "IO error: ");
        CODE_MSG_MAP.put(Code.K_UNKNOWN.ordinal(), "Unknown msg: ");
    }

    private final int code;

    private String state;

    private Status() {
        code = Code.K_OK.ordinal();
    }

    private Status(Code code, String... msg) {
        this.code = code.ordinal();
        state = code + ": " + StringUtils.join(msg, ",");
    }

    public static Status ok() {
        return new Status();
    }

    public static Status notFound(String... msg) {
        return new Status(Code.K_NOT_FOUND, msg);
    }

    public static Status unKnown(String... msg) {
        return new Status(Code.K_UNKNOWN, msg);
    }

    public static Status corruption(String... msg) {
        return new Status(Code.K_CORRUPTION, msg);
    }

    public static Status notSupported(String... msg) {
        return new Status(Code.K_NOT_SUPPORTED, msg);
    }

    public static Status invalidArgument(String... msg) {
        return new Status(Code.K_INVALID_ARGUMENT, msg);
    }

    public static Status iOError(String... msg) {
        return new Status(Code.K_IO_ERROR, msg);
    }

    public boolean isOk() {
        return code == Code.K_OK.ordinal();
    }

    public boolean isNotFound() {
        return code == Code.K_NOT_FOUND.ordinal();
    }

    public boolean isCorruption() {
        return code == Code.K_CORRUPTION.ordinal();
    }

    public boolean isIOError() {
        return code == Code.K_IO_ERROR.ordinal();
    }

    public boolean isNotSupportedError() {
        return code == Code.K_NOT_SUPPORTED.ordinal();
    }

    public boolean isInvalidArgument() {
        return code == Code.K_INVALID_ARGUMENT.ordinal();
    }

    private Code code() {
        for (Code c : Code.values()) {
            if (c.ordinal() == this.code) {
                return c;
            }
        }
        return Code.K_UNKNOWN;
    }

    @Override
    public String toString() {
        String type = CODE_MSG_MAP.getOrDefault(code, CODE_MSG_MAP.get(Code.K_UNKNOWN.ordinal()));
        return type + state;
    }

    public static void main(String[] args) {
        System.out.println(Code.K_CORRUPTION.ordinal());
        System.out.println(Code.K_CORRUPTION);
        Code code = Code.K_CORRUPTION;
        System.out.println(ObjectSizeCalculator.getObjectSize(code));
        int x = 4;
        System.out.println(ObjectSizeCalculator.getObjectSize(x));
        String s = new String("test111111111");
        StringBuffer sb = new StringBuffer("test111111111");
        char[] cs = new char[10];
        System.out.println(ObjectSizeCalculator.getObjectSize(sb));
        System.out.println(ObjectSizeCalculator.getObjectSize(new Status()));

    }
}
