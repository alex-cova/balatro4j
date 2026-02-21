package com.balatro.cache;

import org.jetbrains.annotations.NotNull;

/**
 * 0   | Chars: 32,
 * 32  | Voucher: 32
 * 64  | UnCommonJoker: 64
 * 128 | CommonJoker: 60
 * 188 | Special: 2
 * 190 | LegendaryJoker: 5
 * 195 | Spectral: 16
 * 211 | RareJoker: 20
 * 231 | Tag.values: 24
 */
class Memory256Long {
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

    public long generateChecksum() {
        long checksum = 0;

        for (int i = 1; i < 4; i++) {
            checksum ^= memory[i];
        }

        return checksum;
    }

    public boolean equals(@NotNull Memory256Long another) {
        int last32Bits = (int) (memory[0] & 0xFFFFFFFFL);
        int last32Bits_2 = (int) (another.memory[0] & 0xFFFFFFFFL);

        return last32Bits == last32Bits_2 &&
                memory[1] == another.memory[1] &&
                memory[2] == another.memory[2] &&
                memory[3] == another.memory[3];
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
