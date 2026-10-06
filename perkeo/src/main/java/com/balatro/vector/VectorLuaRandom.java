package com.balatro.vector;

import jdk.incubator.vector.*;

public final class VectorLuaRandom {

    public static final VectorSpecies<Double> DOUBLE_SPECIES = DoubleVector.SPECIES_PREFERRED;
    public static final VectorSpecies<Long> LONG_SPECIES = LongVector.SPECIES_PREFERRED;

    private static final long MAX_UINT64 = Long.MAX_VALUE;
    private static final DoubleVector PI = DoubleVector.broadcast(DOUBLE_SPECIES, Math.PI);
    private static final DoubleVector E = DoubleVector.broadcast(DOUBLE_SPECIES, 2.7182818284590452354);
    private static final LongVector DOUBLE_MANTISSA_MASK = LongVector.broadcast(LONG_SPECIES, 4503599627370495L);
    private static final LongVector DOUBLE_ONE_BITS = LongVector.broadcast(LONG_SPECIES, 4607182418800017408L);

    private VectorLuaRandom() {
    }

    public static int laneCount() {
        return DOUBLE_SPECIES.length();
    }

    public static LongVector randInt(DoubleVector seed) {
        DoubleVector d = seed;
        int r = 0x11090601;
        LongVector random = LongVector.zero(LONG_SPECIES);

        long m = 1L << (r & 255);
        r >>= 8;
        d = d.mul(PI).add(E);
        LongVector state = d.reinterpretAsLongs();
        state = atLeast(state, m);
        state = warm(state, 31, 45, 1, 18);
        state = step(state, 31, 45, 1, 18);
        random = random.lanewise(VectorOperators.XOR, state);

        m = 1L << (r & 255);
        r >>= 8;
        d = d.mul(PI).add(E);
        state = d.reinterpretAsLongs();
        state = atLeast(state, m);
        state = warm(state, 19, 30, 6, 28);
        state = step(state, 19, 30, 6, 28);
        random = random.lanewise(VectorOperators.XOR, state);

        m = 1L << (r & 255);
        r >>= 8;
        d = d.mul(PI).add(E);
        state = d.reinterpretAsLongs();
        state = atLeast(state, m);
        state = warm(state, 24, 48, 9, 7);
        state = step(state, 24, 48, 9, 7);
        random = random.lanewise(VectorOperators.XOR, state);

        m = 1L << (r & 255);
        d = d.mul(PI).add(E);
        state = d.reinterpretAsLongs();
        state = atLeast(state, m);
        state = warm(state, 21, 39, 17, 8);
        state = step(state, 21, 39, 17, 8);
        random = random.lanewise(VectorOperators.XOR, state);

        return random;
    }

    public static DoubleVector random(DoubleVector seed) {
        return randInt(seed).and(DOUBLE_MANTISSA_MASK).or(DOUBLE_ONE_BITS).reinterpretAsDoubles().sub(1.0);
    }

    private static LongVector atLeast(LongVector state, long minimum) {
        LongVector min = LongVector.broadcast(LONG_SPECIES, minimum);
        VectorMask<Long> tooSmall = state.compare(VectorOperators.LT, min);
        return state.blend(state.add(min), tooSmall);
    }

    private static LongVector warm(LongVector state, int leftA, int right, int maskShift, int leftB) {
        for (int i = 0; i < 5; i++) {
            state = step(state, leftA, right, maskShift, leftB);
            state = step(state, leftA, right, maskShift, leftB);
        }
        return state;
    }

    private static LongVector step(LongVector state, int leftA, int right, int maskShift, int leftB) {
        LongVector a = state.lanewise(VectorOperators.LSHL, leftA).lanewise(VectorOperators.XOR, state)
                .lanewise(VectorOperators.LSHR, right);
        LongVector b = state.and(LongVector.broadcast(LONG_SPECIES, MAX_UINT64 << maskShift))
                .lanewise(VectorOperators.LSHL, leftB);
        return a.lanewise(VectorOperators.XOR, b);
    }
}
