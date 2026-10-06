package com.balatro.vector;

import jdk.incubator.vector.DoubleVector;

public final class VectorPrngStream {

    private DoubleVector state;

    VectorPrngStream(DoubleVector state) {
        this.state = state;
    }

    DoubleVector state() {
        return state;
    }

    void state(DoubleVector state) {
        this.state = state;
    }
}
