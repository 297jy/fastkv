package com.zhuanyi.leveldb.core.table;


import java.util.Comparator;
import java.util.Random;

public class SkipTable<K extends Comparable<K>> {

    /**
     * 跳跃表最大高度
     */
    private static final int K_MAX_HEIGHT = 12;

    /**
     * 跳跃表增加层数的概率
     */
    private static final int K_BRANCHING = 4;

    private final Node<K> head;

    private volatile int maxHeight;

    /**
     * 用来计算跳跃表高度的随机数
     */
    private final Random random;

    private final Comparator<K> comparator;

    public SkipTable(Comparator<K> comparator) {
        maxHeight = 1;
        head = Node.newNode(null, K_MAX_HEIGHT);
        random = new Random(System.currentTimeMillis());
        this.comparator = comparator;
    }

    private static class Node<K extends Comparable<K>> {

        private K key;

        private volatile Node<K>[] next;

        public static <K extends Comparable<K>> Node<K> newNode(K key, int height) {
            Node<K> node = new Node<>();
            node.key = key;
            node.next = new Node[height];
            return node;
        }

    }

    private static class SkipTableIterator<K extends Comparable<K>> implements TableIterator<K> {
        /**
         * 跳跃表对象
         */
        private final SkipTable<K> list;

        /**
         * 当前遍历到的节点
         */
        private Node<K> nowNode;

        public SkipTableIterator(SkipTable<K> list) {
            this.list = list;
            this.nowNode = list.head;
        }

        @Override
        public boolean valid() {
            return nowNode != null;
        }

        @Override
        public K key() {
            return nowNode == null ? null : nowNode.key;
        }

        @Override
        public void next() {
            assert (valid());
            nowNode = nowNode.next[0];
        }

        @Override
        public void prev() {
            assert (valid());
            nowNode = list.findLessThan(nowNode.key);

            // head节点的前一个节点是null
            if (nowNode == list.head) {
                nowNode = null;
            }
        }

        @Override
        public void seek(K target) {
            nowNode = list.findGreaterOrEqual(target, null);
        }

        @Override
        public void seekToFirst() {
            nowNode = list.head.next[0];
        }

        @Override
        public void seekToLast() {
            nowNode = list.findLast();
        }

        public int height() {
            return nowNode.next.length;
        }
    }

    /**
     * 插入方法，需要外部加锁
     *
     * @param key 插入的key
     */
    public void insert(K key) {

        Node<K>[] prev = new Node[K_MAX_HEIGHT];
        Node<K> x = findGreaterOrEqual(key, prev);

        // 不能插入重复的 key
        assert (x == null || !x.key.equals(key));

        int height = getRandomHeight();
        if (height > getNowMaxHeight()) {
            for (int i = getNowMaxHeight(); i < height; i++) {
                prev[i] = head;
            }

            updateNowMaxHeight(height);
        }

        x = Node.newNode(key, height);
        for (int i = 0; i < height; i++) {
            x.next[i] = prev[i].next[i];
            prev[i].next[i] = x;
        }

    }

    /**
     * 找到第一个比key大的值，并填充前驱节点数组
     *
     * @param key
     * @param prev
     * @return
     */
    private Node<K> findGreaterOrEqual(K key, Node<K>[] prev) {
        Node<K> x = head;
        int level = getNowMaxHeight() - 1;
        while (true) {
            Node<K> next = x.next[level];
            if (keyIsAfterNode(key, next)) {
                x = next;
            } else {
                if (prev != null) {
                    prev[level] = x;
                }
                if (level == 0) {
                    return next;
                } else {
                    level--;
                }
            }
        }
    }

    /**
     * 找到第一个<=key的节点
     *
     * @param key
     * @return
     */
    private Node<K> findLessThan(K key) {
        Node<K> x = head;
        int level = getNowMaxHeight() - 1;
        while (true) {
            assert (x == head || cmp(x.key, key) < 0);
            Node<K> next = x.next[level];
            if (next == null || cmp(next.key, key) >= 0) {
                if (level == 0) {
                    return x;
                } else {
                    level--;
                }
            } else {
                x = next;
            }
        }
    }

    private Node<K> findLast() {
        if (head == null) {
            return null;
        }
        Node<K> x = head;
        int level = getNowMaxHeight() - 1;
        while (true) {
            Node<K> next = x.next[level];
            if (next == null) {
                if (level == 0) {
                    return x;
                } else {
                    level--;
                }
            } else {
                x = next;
            }
        }
    }

    private int getRandomHeight() {
        int height = 1;
        while (height < K_MAX_HEIGHT && (random.nextInt(4) % K_BRANCHING == 0)) {
            height++;
        }
        return height;
    }

    private int getNowMaxHeight() {
        return maxHeight;
    }

    private void updateNowMaxHeight(int height) {
        maxHeight = height;
    }

    private boolean keyIsAfterNode(K key, Node<K> n) {
        return n != null && cmp(n.key, key) < 0;
    }

    private int cmp(K key1, K key2) {
        if (this.comparator != null) {
            return comparator.compare(key1, key2);
        }
        return key1.compareTo(key2);
    }

    public boolean contains(K key) {
        Node<K> x = findGreaterOrEqual(key, null);
        if (x != null && x.key.equals(key)) {
            return true;
        }
        return false;
    }

    public TableIterator<K> iterator() {
        return new SkipTableIterator<>(this);
    }

    @Override
    public String toString() {
        SkipTableIterator<String> result = (SkipTableIterator<String>) iterator();
        int[] heights = new int[K_MAX_HEIGHT + 1];
        while (true) {
            result.next();
            if (!result.valid()) {
                break;
            }
            heights[result.height()]++;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 12; i++) {
            sb.append("高度为: ").append(i).append(",数量为: ").append(heights[i]).append("\n");
        }
        return sb.toString();
    }
}
