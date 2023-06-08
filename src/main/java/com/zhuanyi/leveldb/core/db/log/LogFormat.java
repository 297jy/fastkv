package com.zhuanyi.leveldb.core.db.log;


import java.util.Arrays;

public class LogFormat {

    public static final int K_MAX_RECORD_TYPE = RecordType.K_LAST_TYPE.getCode();

    /**
     * 一个页的大小：32KB
     */
    public static final int K_BLOCK_SIZE = 32768;

    /**
     *  一个完整的 log record 分为 header 和 data部分
     *  checksum：校验和
     *  len:log record 中 data部分的字节长度
     *  type: log record的类型
     *  log record的头部占用字节数，checksum (4 bytes), length (2 bytes), type (1 byte).
     */
    public static final int K_HEADER_SIZE = 4 + 2 + 1;

    /**
     * RecordType是日志记录类型，一个user record可以由多个log record构成
     */
    public enum RecordType {

        /**
         * 说明该record还未分配使用，目前只是预分配状态
         */
        K_ZERO_TYPE(0),

        /**
         *说明该log record包含一个完整的user record
         */
        K_FULL_TYPE(1),

        /**
         * 说明是user record的第一条log record
         */
        K_FIRST_TYPE(2),

        /**
         * 说明是user record中间的log record
         */
        K_MIDDLE_TYPE(3),

        /**
         * 说明是user record最后的一条log record
         */
        K_LAST_TYPE(4),

        K_EOF(5),

        K_BAD_RECORD(6);

        private final int code;

        RecordType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static RecordType valueOf(int code) {
            return Arrays.stream(values()).filter(v -> v.code == code).findFirst().orElse(null);
        }
    }
}
