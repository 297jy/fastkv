package com.zhuanyi.leveldb.core.utils;

import com.zhuanyi.leveldb.core.common.RequestContext;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 简单的对象池，避免频繁new对象，导致频繁GC，造成Stop The World
 * 保证是线程安全的，后续用cas机制优化，否则加锁的开销也很大
 * @param <E>
 */
public class SimpleObjectPoolsImpl<E> implements ObjectPools<E> {

    private static class FastList<E> {
        private int cap;
        private int tair;
        private Object[] arrays;

        FastList() {
            cap = 2;
            tair = 0;
            arrays = new Object[cap];
        }

        void add(E e) {
            // 容量满了，扩容
            if (tair == cap) {
                cap <<= 1;
                Object[] newArrays = new Object[cap];
                System.arraycopy(arrays, 0, newArrays, 0, tair);
                // 帮助gc正常释放数组
                Arrays.fill(arrays, 0, tair, null);
                arrays = newArrays;
            }

            arrays[tair++] = e;
        }

        E get() {
            if (isEmpty()) {
                return null;
            }
            return (E) arrays[--tair];
        }

        boolean isEmpty() {
            return tair == 0;
        }
    }

    private final FastList<E> emptyNodes = new FastList<>();

    private Map<Long,FastList<E>> busyListMap;

    private final Lock lock = new ReentrantLock();

    private final Supplier<E> buildFun;

    private final Consumer<E> initFun;


    public SimpleObjectPoolsImpl(Supplier<E> buildFun, Consumer<E> initFun) {
        this.buildFun = buildFun;
        this.initFun = initFun;
    }

    @Override
    public E allocateObject(RequestContext requestContext) {
        lock.lock();
        try {
            E res = null;
            if (emptyNodes.isEmpty()) {
                res = buildFun.get();
            } else {
                res = emptyNodes.get();
            }
            if (res != null) {
                initFun.accept(res);
            }
            return res;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void releaseObjects(RequestContext requestContext, E... objs) {
        lock.lock();
        try {
            for (E obj : objs) {
                emptyNodes.add(obj);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
    @Override
    public E allocateObject(Consumer<E> initFun) {
        lock.lock();
        try {
            E res = null;
            if (emptyNodes.isEmpty()) {
                try {
                    res = c.newInstance();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                res = emptyNodes.get();
            }
            if (res != null) {
                initFun.accept(res);
            }
            return res;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void releaseObjects(E... objs) {
        lock.lock();
        try {
            for (E obj : objs) {
                emptyNodes.add(obj);
            }
        } finally {
            lock.unlock();
        }
    }**/
}
