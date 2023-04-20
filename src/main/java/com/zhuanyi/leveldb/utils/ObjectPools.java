package com.zhuanyi.leveldb.utils;

import java.util.function.Consumer;

public interface ObjectPools<E> {

    E allocateObject(Consumer<E> initFun);

    void releaseObjects(E... objs);

}
