package com.balatro.vector;

public interface VectorSeedFilter {

    default void configure(VectorFilterCreationContext context) {
    }

    VectorMaskBits filter(VectorSearchContext context);
}
