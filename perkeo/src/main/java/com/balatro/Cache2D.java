package com.balatro;

import com.balatro.api.Cache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class Cache2D implements Cache {

    // Empty-slot sentinel for the primitive resample map. resampleKey is always >= 0
    // (id is a non-negative counter, count is masked to 16 bits), so -1 cannot collide.
    private static final long EMPTY_KEY = -1L;
    private static final int INITIAL_RESAMPLE_CAP = 16;

    private final double[][] nodes;
    private final double[] specials;
    private boolean generatedFirstPack;
    private Map<String, Double> resampleCache;

    // Primitive open-addressed long->double cache — replaces HashMap<Long,Double> to avoid
    // per-access boxing on the resample hot path.
    private long[] rKeys;
    private double[] rVals;
    private int rSize;
    private int rMask;

    public Cache2D(int maxAnte) {
        generatedFirstPack = false;
        specials = new double[5];
        nodes = new double[maxAnte][44];
    }

    @Override
    public boolean isGeneratedFirstPack() {
        return generatedFirstPack;
    }

    @Override
    public void setGeneratedFirstPack(boolean generatedFirstPack) {
        this.generatedFirstPack = generatedFirstPack;
    }

    @Override
    public @Nullable Double get(String id) {
        if (resampleCache == null) {
            return null;
        }

        return resampleCache.get(id);
    }

    @Override
    public void put(String id, double value) {
        if (resampleCache == null) {
            resampleCache = new HashMap<>();
        }
        resampleCache.put(id, value);
    }

    @Override
    public @Nullable Double getResample(long key) {
        if (rKeys == null) return null;
        final int idx = findSlot(key);
        final long k = rKeys[idx];
        return k == EMPTY_KEY ? null : rVals[idx];
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
        // splitmix64 finalizer — cheap and spreads low-entropy ids uniformly.
        x ^= x >>> 30;
        x *= 0xbf58476d1ce4e5b7L;
        x ^= x >>> 27;
        x *= 0x94d049bb133111ebL;
        x ^= x >>> 31;
        return x;
    }

    @Override
    public double get(@NotNull Coordinate c) {
        if (c.ante() == -1) {
            return specials[c.y()];
        }
        try {
            return nodes[c.ante() - 1][c.y()];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Unable to get value for coordinate: " + c + " node: "
                    + nodes.length + " x " + nodes[c.y()].length);
        }
    }

    @Override
    public void put(@NotNull Coordinate c, double value) {
        if (c.ante() == -1) {
            specials[c.y()] = value;
            return;
        }
        nodes[c.ante() - 1][c.y()] = value;
    }
}
