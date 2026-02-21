package com.balatro;

import org.jetbrains.annotations.NotNull;

public record Coordinate(String value, byte[] data, int ante, int y, int id) {

    private static int ID_COUNTER = 0;

    public Coordinate(String value, int ante, int y) {
        this(value, value.getBytes(), ante, y, ID_COUNTER++);
    }

    public Coordinate(String value, byte[] data, int ante, int y) {
        this(value, data, ante, y, ID_COUNTER++);
    }

    /**
     * Returns a long key encoding (coordinateId, resampleCount) for use as a cache key.
     * Avoids String allocation entirely in the resample hot path.
     */
    public long resampleKey(int resampleCount) {
        return ((long) id << 16) | (resampleCount & 0xFFFF);
    }

    public @NotNull String resample(int resampleCount) {
        return value + "_resample" + resampleCount;
    }

    public double pseudohash(byte[] seed) {
        return Util.pseudohash(data, seed);
    }

    @Override
    public @NotNull String toString() {
        return value;
    }
}
