package com.balatro;

import org.jetbrains.annotations.NotNull;

public record Coordinate(String value, byte[] data, int ante, int y) {

    public Coordinate(String value, int ante, int y) {
        this(value, value.getBytes(), ante, y);
    }

    public @NotNull String resample(int r) {
        return "%s_resample%s".formatted(value, r);
    }

    public double pseudohash(byte[] seed) {
        return Util.pseudohash(data, seed);
    }

    @Override
    public @NotNull String toString() {
        return value;
    }
}
