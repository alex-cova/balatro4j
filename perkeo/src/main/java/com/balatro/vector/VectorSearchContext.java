package com.balatro.vector;

import com.balatro.enums.Tag;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorSpecies;

import java.util.HashMap;
import java.util.Map;

public final class VectorSearchContext {

    public static final int MAX_SEED_LENGTH = 8;
    public static final VectorSpecies<Double> DOUBLE_SPECIES = VectorLuaRandom.DOUBLE_SPECIES;
    public static final VectorSpecies<Integer> INT_SPECIES = IntVector.SPECIES_PREFERRED;

    private static final DoubleVector HASH_MULTIPLIER = DoubleVector.broadcast(DOUBLE_SPECIES, 1.1239285023);
    private static final DoubleVector PRNG_MULTIPLIER = DoubleVector.broadcast(DOUBLE_SPECIES, 1.72431234);
    private static final DoubleVector PRNG_INCREMENT = DoubleVector.broadcast(DOUBLE_SPECIES, 2.134453429141);
    private static final DoubleVector TWO = DoubleVector.broadcast(DOUBLE_SPECIES, 2.0);
    private static final double INV_PREC = Math.pow(10, 13);
    private static final double TWO_INV_PREC = Math.pow(2, 13);
    private static final double FIVE_INV_PREC = Math.pow(5, 13);

    private final DoubleVector[] seedCharacters;
    private final boolean[] validLanes;
    private final int seedLength;
    private final Map<Integer, DoubleVector> partialSeedHashes = new HashMap<>();
    private final int[] intBuffer = new int[Math.max(INT_SPECIES.length(), DOUBLE_SPECIES.length())];
    private final double[] doubleBuffer = new double[DOUBLE_SPECIES.length()];
    private final long[] longBuffer = new long[6];

    VectorSearchContext(DoubleVector[] seedCharacters, boolean[] validLanes, int seedLength,
                        Map<Integer, DoubleVector> precomputedHashes) {
        this.seedCharacters = seedCharacters;
        this.validLanes = validLanes;
        this.seedLength = seedLength;
        partialSeedHashes.putAll(precomputedHashes);
    }

    public int laneCount() {
        return DOUBLE_SPECIES.length();
    }

    public boolean isLaneValid(int lane) {
        return validLanes[lane];
    }

    public String seed(int lane) {
        if (!isLaneValid(lane)) {
            throw new IllegalArgumentException("Invalid vector lane: " + lane);
        }

        char[] chars = new char[seedLength];
        for (int i = 0; i < seedLength; i++) {
            chars[i] = (char) seedCharacters[i].lane(lane);
        }
        return new String(chars);
    }

    public VectorPrngStream createPrngStream(String key) {
        return new VectorPrngStream(pseudoHash(key));
    }

    public DoubleVector pseudoHash(String key) {
        DoubleVector partial = partialSeedHashes.computeIfAbsent(key.length(), this::partialSeedHash);
        return hashKey(key, partial);
    }

    public DoubleVector getNextRandom(VectorPrngStream stream) {
        DoubleVector pseudoSeed = getNextPseudoSeed(stream);
        return VectorLuaRandom.random(pseudoSeed);
    }

    public DoubleVector getNextRandom(VectorPrngStream stream, VectorMaskBits activeLanes) {
        if (activeLanes.allFalse()) {
            return DoubleVector.zero(DOUBLE_SPECIES);
        }
        DoubleVector pseudoSeed = getNextPseudoSeed(stream, activeLanes);
        return VectorLuaRandom.random(pseudoSeed);
    }

    public IntVector getNextRandomIntInclusive(VectorPrngStream stream, int max) {
        DoubleVector random = getNextRandom(stream).mul(max + 1);
        random.intoArray(doubleBuffer, 0);
        for (int i = 0; i < DOUBLE_SPECIES.length(); i++) {
            intBuffer[i] = (int) doubleBuffer[i];
        }
        for (int i = DOUBLE_SPECIES.length(); i < INT_SPECIES.length(); i++) {
            intBuffer[i] = 0;
        }
        return IntVector.fromArray(INT_SPECIES, intBuffer, 0);
    }

    public void getNextRandomTimesInto(VectorPrngStream stream, double multiplier, double[] output) {
        getNextRandom(stream).mul(multiplier).intoArray(output, 0);
    }

    double[] doubleScratch() {
        return doubleBuffer;
    }

    long[] zeroedLongScratch() {
        for (int i = 0; i < longBuffer.length; i++) {
            longBuffer[i] = 0L;
        }
        return longBuffer;
    }

    public IntVector getNextTagOrdinal(int ante) {
        VectorPrngStream stream = createPrngStream("Tag" + ante);
        return getNextRandomIntInclusive(stream, Tag.values().length - 1);
    }

    VectorMaskBits validLaneMask() {
        long bits = 0L;
        for (int i = 0; i < validLanes.length; i++) {
            if (validLanes[i]) bits |= 1L << i;
        }
        return new VectorMaskBits(bits);
    }

    DoubleVector seedPartialHash(int keyLength) {
        return partialSeedHash(keyLength);
    }

    private DoubleVector getNextPseudoSeed(VectorPrngStream stream) {
        DoubleVector nextState = iteratePrng(stream.state());
        stream.state(nextState);
        return nextState.add(partialSeedHashes.computeIfAbsent(0, this::partialSeedHash)).div(TWO);
    }

    private DoubleVector getNextPseudoSeed(VectorPrngStream stream, VectorMaskBits activeLanes) {
        DoubleVector current = stream.state();
        DoubleVector next = iteratePrng(current);
        VectorMask<Double> mask = VectorMask.fromLong(DOUBLE_SPECIES, activeLanes.bits());
        DoubleVector blended = current.blend(next, mask);
        stream.state(blended);
        return blended.add(partialSeedHashes.computeIfAbsent(0, this::partialSeedHash)).div(TWO);
    }

    private DoubleVector partialSeedHash(int keyLength) {
        DoubleVector num = DoubleVector.broadcast(DOUBLE_SPECIES, 1.0);
        for (int i = seedLength - 1; i >= 0; i--) {
            DoubleVector term = HASH_MULTIPLIER.div(num)
                    .mul(seedCharacters[i])
                    .mul(Math.PI)
                    .add((i + keyLength + 1) * Math.PI);
            num = term.sub(floor(term));
        }
        return num;
    }

    private DoubleVector hashKey(String key, DoubleVector partial) {
        DoubleVector num = partial;
        for (int i = key.length() - 1; i >= 0; i--) {
            DoubleVector term = HASH_MULTIPLIER.div(num)
                    .mul(key.charAt(i))
                    .mul(Math.PI)
                    .add((i + 1) * Math.PI);
            num = term.sub(floor(term));
        }
        return num;
    }

    private DoubleVector iteratePrng(DoubleVector state) {
        state = state.mul(PRNG_MULTIPLIER).add(PRNG_INCREMENT);
        state = state.sub(floor(state));
        return round13(state);
    }

    private DoubleVector floor(DoubleVector value) {
        value.intoArray(doubleBuffer, 0);
        for (int i = 0; i < doubleBuffer.length; i++) {
            doubleBuffer[i] = Math.floor(doubleBuffer[i]);
        }
        return DoubleVector.fromArray(DOUBLE_SPECIES, doubleBuffer, 0);
    }

    private DoubleVector round13(DoubleVector value) {
        value.intoArray(doubleBuffer, 0);
        for (int i = 0; i < doubleBuffer.length; i++) {
            doubleBuffer[i] = round13(doubleBuffer[i]);
        }
        return DoubleVector.fromArray(DOUBLE_SPECIES, doubleBuffer, 0);
    }

    private static double round13(double x) {
        double floored = Math.floor(x * INV_PREC);
        double tentative = floored / INV_PREC;
        double twoScaled = x * TWO_INV_PREC;
        double truncated = (twoScaled - Math.floor(twoScaled)) * FIVE_INV_PREC;
        double truncatedFraction = truncated - Math.floor(truncated);
        if (tentative != x && truncatedFraction >= 0.5 && tentative != Math.nextUp(x)) {
            return (floored + 1) / INV_PREC;
        }
        return tentative;
    }
}
