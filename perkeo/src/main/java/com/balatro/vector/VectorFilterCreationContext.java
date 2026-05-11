package com.balatro.vector;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class VectorFilterCreationContext {

    private final Set<Integer> cachedPseudoHashLengths = new HashSet<>();

    public void cachePseudoHash(String key) {
        cachePseudoHash(key.length());
    }

    public void cachePseudoHash(int keyLength) {
        cachedPseudoHashLengths.add(keyLength);
    }

    Set<Integer> cachedPseudoHashLengths() {
        return Collections.unmodifiableSet(cachedPseudoHashLengths);
    }
}
