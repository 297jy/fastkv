package com.zhuanyi.leveldb.core.table;

import com.zhuanyi.leveldb.core.common.Options;
import com.zhuanyi.leveldb.core.common.Slice;
import com.zhuanyi.leveldb.core.db.buffer.BytesBuffer;

import java.util.List;

public class BlockBuilderImpl implements BlockBuilder {

    private BytesBuffer buffer;

    private List<Integer> restarts;

    private int counter;

    private boolean finish;

    private Slice lastKey;

    private Options options;

    public BlockBuilderImpl(Options options) {
        this.options = options;
    }

    @Override
    public void reset() {
        buffer.clear();
        restarts.clear();
        restarts.add(0);
        counter = 0;
        finish = false;
        lastKey.clear();
    }

    @Override
    public void add(Slice key, Slice value) {
        assert (!finish);
        assert (counter <= options.getBlockRestartInterval());
        // 后面的key肯定比前一个key大
        assert (buffer.isEmpty()
                || options.getComparator().compare(key, lastKey) > 0);

        int shared = 0;
        if (counter < options.getBlockRestartInterval()) {
            shared = key.sharedPrefixLen(lastKey);
        } else {
            restarts.add(buffer.len());
            counter = 0;
        }

        int nonShared = key.readableBytes() - shared;
        buffer.appendVarInt(shared);
        buffer.appendVarInt(nonShared);
        buffer.appendVarInt(value.readableBytes());

        buffer.append(key, shared);
        buffer.append(value);
        counter++;
    }

    @Override
    public Slice finish() {
        for (int restart : restarts) {
            buffer.appendVarInt(restart);
        }
        buffer.appendVarInt(restarts.size());
        finish = true;
        return buffer.toSlice();
    }

    @Override
    public int currentSizeEstimate() {
        int ib = Integer.SIZE / 8;
        return buffer.len() + (restarts.size() * ib) + ib;
    }

    @Override
    public boolean isEmpty() {
        return buffer.isEmpty();
    }
}
