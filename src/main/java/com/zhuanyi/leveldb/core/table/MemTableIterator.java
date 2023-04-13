package com.zhuanyi.leveldb.core.table;

import java.util.ListIterator;

public interface MemTableIterator<E> extends ListIterator<E> {

    boolean valid();

}
