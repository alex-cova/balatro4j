package com.balatro;


import com.balatro.api.Item;
import com.balatro.api.Lock;
import org.jetbrains.annotations.NotNull;

public class BooleanArrayLock implements Lock {

    private final boolean[][] locks;

    public BooleanArrayLock() {
        this.locks = new boolean[13][];

        //jagged array
        locks[0] = new boolean[61];
        locks[1] = new boolean[64];
        locks[2] = new boolean[20];
        locks[3] = new boolean[12];
        locks[4] = new boolean[22];
        locks[5] = new boolean[18];
        locks[6] = new boolean[28];
        locks[7] = new boolean[32];
        locks[8] = new boolean[24];
        locks[9] = new boolean[2];
        locks[10] = new boolean[52];
        locks[11] = new boolean[8];
        locks[12] = new boolean[5];
    }

    @Override
    public void unlock(@NotNull Item object) {
        locks[object.getYIndex()][object.ordinal()] = false;
    }

    @Override
    public void lock(@NotNull Item object) {
        locks[object.getYIndex()][object.ordinal()] = true;
    }

    @Override
    public boolean isLocked(@NotNull Item object) {
        return locks[object.getYIndex()][object.ordinal()];
    }
}
