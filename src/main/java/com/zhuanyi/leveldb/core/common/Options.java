package com.zhuanyi.leveldb.core.common;

import lombok.Data;
import java.util.Comparator;


@Data
public class Options {

    private int blockRestartInterval = 16;

    private Comparator<Slice> comparator;

}
