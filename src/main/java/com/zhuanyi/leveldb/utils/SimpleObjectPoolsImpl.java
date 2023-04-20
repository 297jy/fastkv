package com.zhuanyi.leveldb.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * 简单的对象池，避免频繁new对象，导致频繁GC，造成Stop The World
 *
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

    private final Class<E> c;

    private final Lock lock = new ReentrantLock();

    public SimpleObjectPoolsImpl(Class<E> c) {
        this.c = c;
    }

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
    }
}
