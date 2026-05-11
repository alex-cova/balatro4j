package com.balatro.vector;

public interface ScalarSeedPrefilter extends VectorSeedFilter {

    boolean filterScalar(byte[] seed);

    default long filterScalarBatch(byte[] suffixSeed, int firstDigitOffset, int batchSize, byte[] seedDigits) {
        long matches = 0L;
        byte[] seed = suffixSeed.clone();
        for (int i = 0; i < batchSize; i++) {
            seed[0] = seedDigits[firstDigitOffset + i];
            if (filterScalar(seed)) {
                matches |= 1L << i;
            }
        }
        return matches;
    }
}
