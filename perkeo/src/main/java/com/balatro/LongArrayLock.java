package com.balatro;

import com.balatro.api.Item;
import com.balatro.api.Lock;
import org.jetbrains.annotations.NotNull;

public class LongArrayLock implements Lock {

    private final long[] data;

    public LongArrayLock() {
        this.data = new long[13];
    }

    @Override
    public void unlock(@NotNull Item item) {
        data[item.getYIndex()] = data[item.getYIndex()] & ~(1L << item.ordinal());
    }

    @Override
    public void lock(@NotNull Item item) {
        data[item.getYIndex()] = data[item.getYIndex()] | (1L << item.ordinal());
    }

    @Override
    public boolean isLocked(@NotNull Item item) {
        return (data[item.getYIndex()] & (1L << item.ordinal())) != 0;
    }
}
