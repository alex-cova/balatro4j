package com.balatro;

import com.balatro.api.Cache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

final class CacheMap implements Cache {

    private static final long EMPTY_KEY = -1L;
    private static final int INITIAL_RESAMPLE_CAP = 16;

    private final Map<String, Double> nodes;
    private boolean generatedFirstPack;

    private long[] rKeys;
    private double[] rVals;
    private int rSize;
    private int rMask;

    public CacheMap() {
        nodes = new HashMap<>();
        generatedFirstPack = false;
    }

    public boolean isGeneratedFirstPack() {
        return generatedFirstPack;
    }

    @Override
    public void put(String id, double value) {
        nodes.put(id, value);
    }

    @Override
    public double get(@NotNull Coordinate c) {
        return nodes.getOrDefault(c.value(), -1.0);
    }

    @Override
    public void put(@NotNull Coordinate c, double value) {
        nodes.put(c.value(), value);
    }

    public void setGeneratedFirstPack(boolean generatedFirstPack) {
        this.generatedFirstPack = generatedFirstPack;
    }

    public @Nullable Double get(String key) {
        return nodes.get(key);
    }

    public void put(String key, Double value) {
        nodes.put(key, value);
    }

    @Override
    public @Nullable Double getResample(long key) {
        if (rKeys == null) return null;
        final int idx = findSlot(key);
        return rKeys[idx] == EMPTY_KEY ? null : rVals[idx];
    }

    @Override
    public double getResampleOrNaN(long key) {
        if (rKeys == null) return Double.NaN;
        final int idx = findSlot(key);
        return rKeys[idx] == EMPTY_KEY ? Double.NaN : rVals[idx];
    }

    @Override
    public void putResample(long key, double value) {
        if (rKeys == null) initResample(INITIAL_RESAMPLE_CAP);
        final int idx = findSlot(key);
        if (rKeys[idx] == EMPTY_KEY) {
            rKeys[idx] = key;
            rVals[idx] = value;
            if (++rSize > ((rMask + 1) * 3) >>> 2) growResample();
        } else {
            rVals[idx] = value;
        }
    }

    private int findSlot(long key) {
        final long[] keys = rKeys;
        final int m = rMask;
        int i = (int) (mix(key) & m);
        while (true) {
            final long k = keys[i];
            if (k == EMPTY_KEY || k == key) return i;
            i = (i + 1) & m;
        }
    }

    private void initResample(int cap) {
        rKeys = new long[cap];
        rVals = new double[cap];
        Arrays.fill(rKeys, EMPTY_KEY);
        rMask = cap - 1;
        rSize = 0;
    }

    private void growResample() {
        final long[] oldK = rKeys;
        final double[] oldV = rVals;
        initResample((rMask + 1) << 1);
        for (int i = 0; i < oldK.length; i++) {
            final long k = oldK[i];
            if (k != EMPTY_KEY) {
                int idx = findSlot(k);
                rKeys[idx] = k;
                rVals[idx] = oldV[i];
                rSize++;
            }
        }
    }

    private static long mix(long x) {
        x ^= x >>> 30;
        x *= 0xbf58476d1ce4e5b7L;
        x ^= x >>> 27;
        x *= 0x94d049bb133111ebL;
        x ^= x >>> 31;
        return x;
    }
}
