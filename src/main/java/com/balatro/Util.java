package com.balatro;


public class Util {

    static double fract(double n) {
        return n - Math.floor(n);
    }

    static double pseudohash(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];

        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);

        return pseudohash(c);
    }

    static double pseudohash(byte[] s) {
        double num = 1;
        for (int i = s.length; i > 0; i--) {
            num = fract(1.1239285023 / num * s[i - 1] * 3.141592653589793 + 3.141592653589793 * i);
        }
        if (Double.isNaN(num)) return Double.NaN;
        return num;
    }

    private static final double inv_prec = Math.pow(10, 13);
    private static final double two_inv_prec = Math.pow(2, 13);
    private static final double five_inv_prec = Math.pow(5, 13);

    public static double round13(double x) {
        double tentative = Math.floor(x * inv_prec) / inv_prec;
        double truncated = ((x * two_inv_prec) % 1.0) * five_inv_prec;
        if (tentative != x && truncated % 1.0 >= 0.5 && tentative != nextAfter(x, 1)) {
            return (Math.floor(x * inv_prec) + 1) / inv_prec;
        }
        return tentative;
    }

    public static double nextAfter(double start, double direction) {
        if (direction < start) {
            if (start == 0.0) {
                // +-0.0
                return -Double.MIN_VALUE;
            }
            final long bits = Double.doubleToRawLongBits(start);
            return Double.longBitsToDouble(bits + ((bits > 0) ? -1 : 1));
        } else if (direction > start) {
            // Going towards +Infinity.
            // +0.0 to get rid of eventual -0.0
            final long bits = Double.doubleToRawLongBits(start + 0.0f);
            return Double.longBitsToDouble(bits + (bits >= 0 ? 1 : -1));
        } else if (start == direction) {
            return direction;
        } else {
            // Returning a NaN derived from the input NaN(s).
            return start + direction;
        }
    }
}