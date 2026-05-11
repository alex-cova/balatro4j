package com.balatro.vector;

import jdk.incubator.vector.VectorMask;

public final class VectorMaskBits {

    private final long bits;

    public VectorMaskBits(long bits) {
        this.bits = bits & laneMask();
    }

    public static VectorMaskBits all() {
        return new VectorMaskBits(laneMask());
    }

    public static VectorMaskBits none() {
        return new VectorMaskBits(0L);
    }

    public static VectorMaskBits from(VectorMask<?> mask) {
        return new VectorMaskBits(mask.toLong());
    }

    public long bits() {
        return bits;
    }

    public boolean laneIsSet(int lane) {
        return (bits & (1L << lane)) != 0L;
    }

    public boolean anyTrue() {
        return bits != 0L;
    }

    public boolean allFalse() {
        return bits == 0L;
    }

    public VectorMaskBits and(VectorMaskBits other) {
        return new VectorMaskBits(bits & other.bits);
    }

    public VectorMaskBits or(VectorMaskBits other) {
        return new VectorMaskBits(bits | other.bits);
    }

    public VectorMaskBits not() {
        return new VectorMaskBits(~bits);
    }

    public VectorMaskBits andNot(VectorMaskBits other) {
        return new VectorMaskBits(bits & ~other.bits);
    }

    static long laneMask() {
        int lanes = VectorLuaRandom.laneCount();
        return lanes == Long.SIZE ? -1L : (1L << lanes) - 1L;
    }
}
