package com.balatro.cache;

public class Memory256Long {
    private final long[] memory; // 4 x 64 = 256 bits

    public Memory256Long(long seed) {
        memory = new long[4];
        memory[0] = seed;
    }

    public Memory256Long(long[] memory) {
        this.memory = memory;
    }

    public long[] getMemory() {
        return memory;
    }

    public void setBit(int index) {
        checkIndex(index);
        int word = index / 64;
        int bit = index % 64;
        memory[word] |= (1L << bit);
    }

    public void clearBit(int index) {
        checkIndex(index);
        int word = index / 64;
        int bit = index % 64;
        memory[word] &= ~(1L << bit);
    }

    public boolean getBit(int index) {
        checkIndex(index);
        int word = index / 64;
        int bit = index % 64;
        return (memory[word] & (1L << bit)) != 0;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= 256) {
            throw new IndexOutOfBoundsException("Index must be between 0 and 255.");
        }
    }
}
