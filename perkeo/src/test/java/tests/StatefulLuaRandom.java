package tests;

import com.balatro.DoubleLong;

public class StatefulLuaRandom {

    public static final long MAX_UINT64 = Long.MAX_VALUE;
    public final long[] state = new long[4];

    public StatefulLuaRandom(double d) {
        long r = 0x11090601;

        for (int i = 0; i < 4; i++) {
            long m = 1L << (r & 255);

            r >>= 8;
            d = d * 3.14159265358979323846;
            d = d + 2.7182818284590452354;

            var u = new DoubleLong(d);

            if (u.getUlong() < m) {
                u.setUlong(u.getUlong() + m);
            }

            state[i] = u.getUlong();
        }

        for (int i = 0; i < 10; i++) {
            _randInt();
        }
    }

    public long _randInt() {
        long z;
        long r = 0;

        // For state[0]
        z = state[0];
        z = (((z << 31) ^ z) >>> 45) ^ ((z & (MAX_UINT64 << 1)) << 18);
        r ^= z;
        state[0] = z;

        // For state[1]
        z = state[1];
        z = (((z << 19) ^ z) >>> 30) ^ ((z & (MAX_UINT64 << 6)) << 28);
        r ^= z;
        state[1] = z;

        // For state[2]
        z = state[2];
        z = (((z << 24) ^ z) >>> 48) ^ ((z & (MAX_UINT64 << 9)) << 7);
        r ^= z;
        state[2] = z;

        // For state[3]
        z = state[3];
        z = (((z << 21) ^ z) >>> 39) ^ ((z & (MAX_UINT64 << 17)) << 8);
        r ^= z;
        state[3] = z;

        // Return the combined result
        return r;
    }

    private long randdblmem() {
        return (_randInt() & 4503599627370495L) | 4607182418800017408L;
    }

    public double random() {
        DoubleLong u = new DoubleLong(randdblmem());
        return u.getDouble() - 1.0;
    }

    public int randint(int min, int max) {
        return (int) (random() * (max - min + 1)) + min;
    }

}