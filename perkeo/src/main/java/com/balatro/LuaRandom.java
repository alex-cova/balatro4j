package com.balatro;

public final class LuaRandom {

    public static final long MAX_UINT64 = Long.MAX_VALUE;

    public static void main(String[] args) {
        System.out.println(random(0.0));
    }

    @SuppressWarnings("all")
    private static long _randInt(double seed) {
        long state;
        long randint = 0;

        int r = 0x11090601;

        long m = 1L << (r & 255);
        r >>= 8;
        seed = seed * 3.14159265358979323846;
        seed = seed + 2.7182818284590452354;

        long bits = Double.doubleToLongBits(seed);

        if (bits < m) {
            bits += m;
        }

        state = bits;

        for (int i = 0; i < 5; i++) {
            state = (((state << 31) ^ state) >>> 45) ^ ((state & (MAX_UINT64 << 1)) << 18);
            state = (((state << 31) ^ state) >>> 45) ^ ((state & (MAX_UINT64 << 1)) << 18);
        }

        state = (((state << 31) ^ state) >>> 45) ^ ((state & (MAX_UINT64 << 1)) << 18);

        randint ^= state;

        //State[1]
        m = 1L << (r & 255);
        r >>= 8;
        seed = seed * 3.14159265358979323846;
        seed = seed + 2.7182818284590452354;

        bits = Double.doubleToLongBits(seed);

        if (bits < m) {
            bits += m;
        }

        state = bits;

        for (int i = 0; i < 5; i++) {
            state = (((state << 19) ^ state) >>> 30) ^ ((state & (MAX_UINT64 << 6)) << 28);
            state = (((state << 19) ^ state) >>> 30) ^ ((state & (MAX_UINT64 << 6)) << 28);
        }

        state = (((state << 19) ^ state) >>> 30) ^ ((state & (MAX_UINT64 << 6)) << 28);

        randint ^= state;

        //State[2]
        m = 1L << (r & 255);
        r >>= 8;
        seed = seed * 3.14159265358979323846;
        seed = seed + 2.7182818284590452354;

        bits = Double.doubleToLongBits(seed);

        if (bits < m) {
            bits += m;
        }

        state = bits;


        for (int i = 0; i < 5; i++) {
            state = (((state << 24) ^ state) >>> 48) ^ ((state & (MAX_UINT64 << 9)) << 7);
            state = (((state << 24) ^ state) >>> 48) ^ ((state & (MAX_UINT64 << 9)) << 7);
        }

        state = (((state << 24) ^ state) >>> 48) ^ ((state & (MAX_UINT64 << 9)) << 7);

        randint ^= state;

        //state 3
        m = 1L << (r & 255);
        //r >>= 8;
        seed = seed * 3.14159265358979323846;
        seed = seed + 2.7182818284590452354;

        bits = Double.doubleToLongBits(seed);

        if (bits < m) {
            bits += m;
        }

        state = bits;

        for (int i = 0; i < 5; i++) {
            state = (((state << 21) ^ state) >>> 39) ^ ((state & (MAX_UINT64 << 17)) << 8);
            state = (((state << 21) ^ state) >>> 39) ^ ((state & (MAX_UINT64 << 17)) << 8);
        }

        state = (((state << 21) ^ state) >>> 39) ^ ((state & (MAX_UINT64 << 17)) << 8);

        randint ^= state;

        return randint;
    }

    private static long randdblmem(double seed) {
        return (_randInt(seed) & 4503599627370495L) | 4607182418800017408L;
    }

    public static double random(double seed) {
        return Double.longBitsToDouble(randdblmem(seed)) - 1.0;
    }

    public static int randint(double seed, int max) {
        return (int) (random(seed) * (max + 1));
    }

}