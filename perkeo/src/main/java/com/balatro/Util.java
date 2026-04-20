package com.balatro;


public class Util {

    static double fract(double n) {
        return n - Math.floor(n);
    }

    /**
     * Computes pseudohash over the logical concatenation [a | b] without allocating
     * a temporary byte array. Iterates backwards over the combined virtual array.
     */
    static double pseudohash(byte[] a, byte[] b) {
        int totalLen = a.length + b.length;
        double num = 1;
        for (int i = totalLen; i > 0; i--) {
            byte val = (i > a.length) ? b[i - a.length - 1] : a[i - 1];
            num = fract(1.1239285023 / num * val * 3.141592653589793 + 3.141592653589793 * i);
        }
        if (Double.isNaN(num)) return Double.NaN;
        return num;
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
        // Callers always pass x from (c % 1) which lies in (-1, 1), so Math.nextUp(x) matches
        // the old nextAfter(x, 1) semantics and is a HotSpot intrinsic on x86/aarch64.
        final double floored = Math.floor(x * inv_prec);
        final double tentative = floored / inv_prec;
        final double truncated = ((x * two_inv_prec) % 1.0) * five_inv_prec;
        if (tentative != x && truncated % 1.0 >= 0.5 && tentative != Math.nextUp(x)) {
            return (floored + 1) / inv_prec;
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