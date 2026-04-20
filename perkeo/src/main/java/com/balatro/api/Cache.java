package com.balatro.api;

import com.balatro.Coordinate;
import org.jetbrains.annotations.NotNull;

public interface Cache {

    Double get(String id);

    void setGeneratedFirstPack(boolean generatedFirstPack);

    boolean isGeneratedFirstPack();

    void put(String id, double value);

    double get(@NotNull Coordinate c);

    void put(@NotNull Coordinate c, double value);

    /**
     * Get a cached resample value by long key (encoded from Coordinate.resampleKey).
     * Returns null if not present.
     */
    Double getResample(long key);

    /**
     * Store a resample value by long key (encoded from Coordinate.resampleKey).
     */
    void putResample(long key, double value);

    /**
     * Primitive-friendly variant of {@link #getResample(long)} that returns {@link Double#NaN}
     * when the key is absent. Hot-path callers should prefer this to avoid Double autoboxing.
     * Default impl bridges to the boxed accessor for custom implementations.
     */
    default double getResampleOrNaN(long key) {
        Double v = getResample(key);
        return v == null ? Double.NaN : v;
    }
}
