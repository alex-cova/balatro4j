package com.balatro;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Cache2D implements Cache {

    private final double[][] nodes;
    private final double[] specials;
    private boolean generatedFirstPack;
    private Map<String, Double> resampleCache;

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
            resampleCache = new LinkedHashMap<>();
        }
        resampleCache.put(id, value);
    }

    @Override
    public double get(@NotNull Coordinate c) {
        if (c.ante() == -1) {
            return specials[c.y()];
        }
        try {
            return nodes[c.ante() - 1][c.y()];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Unable to get value for coordinate: " + c + " node: " + nodes.length + " x " + nodes[c.y()].length);
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
