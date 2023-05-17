package com.zhuanyi.leveldb.core.utils;

import com.zhuanyi.leveldb.core.common.RequestContext;

import java.util.function.Consumer;

public interface ObjectPools<E> {

    E allocateObject(RequestContext requestContext);

    void releaseObjects(RequestContext requestContext, E... objs);

}
