package com.balatro.vector;

import com.balatro.enums.Tag;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorOperators;

import java.nio.charset.StandardCharsets;

public final class VectorFilters {

    private VectorFilters() {
    }

    public static VectorSeedFilter findAll() {
        return context -> context.validLaneMask();
    }

    public static VectorSeedFilter negativeTagRange(int minAnte, int maxAnte) {
        return new VectorSeedFilter() {
            @Override
            public void configure(VectorFilterCreationContext context) {
                for (int ante = minAnte; ante <= maxAnte; ante++) {
                    context.cachePseudoHash("Tag" + ante);
                }
            }

            @Override
            public VectorMaskBits filter(VectorSearchContext context) {
                VectorMaskBits mask = context.validLaneMask();
                IntVector negativeTag = IntVector.broadcast(VectorSearchContext.INT_SPECIES, Tag.Negative_Tag.ordinal());

                for (int ante = minAnte; ante <= maxAnte; ante++) {
                    VectorPrngStream tagStream = context.createPrngStream("Tag" + ante);
                    IntVector first = context.getNextRandomIntInclusive(tagStream, Tag.values().length - 1);
                    mask = mask.and(VectorMaskBits.from(first.compare(VectorOperators.EQ, negativeTag)));
                    if (mask.allFalse()) return mask;

                    IntVector second = context.getNextRandomIntInclusive(tagStream, Tag.values().length - 1);
                    mask = mask.and(VectorMaskBits.from(second.compare(VectorOperators.EQ, negativeTag)));
                    if (mask.allFalse()) return mask;
                }
                return mask;
            }
        };
    }

    public static VectorSeedFilter negativeLegendaryInAnteOnePacks() {
        return new ScalarSeedPrefilter() {
            private static final int ANTE = 1;
            private static final long RANDOM_MANTISSA_MASK = 4503599627370495L;
            private static final long RANDOM_SCALE = 1L << 52;
            private static final long SOUL_THRESHOLD_BITS = (long) Math.floor(0.997 * RANDOM_SCALE);
            private static final long MAX_UINT64 = Long.MAX_VALUE;
            private static final long PACK_ARCANA_SIZE3_BITS = packRandomThreshold(4.0);
            private static final long PACK_ARCANA_END_BITS = packRandomThreshold(6.5);
            private static final long PACK_BEFORE_SPECTRAL_BITS = packRandomThreshold(21.45);
            private static final long PACK_SPECTRAL_SIZE2_BITS = packRandomThreshold(22.05);
            private static final double[] PACK_THRESHOLDS = packThresholds();
            private static final int[] PACK_SIZES = packSizes();
            private static final boolean[] ARCANA_PACKS = arcanaPacks();
            private static final boolean[] SPECTRAL_PACKS = spectralPacks();
            private static final byte[] SEED_HASH_KEY = new byte[0];
            private static final byte[] SHOP_PACK_KEY = ("shop_pack" + ANTE).getBytes(StandardCharsets.US_ASCII);
            private static final byte[] SOUL_TAROT_KEY = ("soul_Tarot" + ANTE).getBytes(StandardCharsets.US_ASCII);
            private static final byte[] SOUL_SPECTRAL_KEY = ("soul_Spectral" + ANTE).getBytes(StandardCharsets.US_ASCII);
            private static final byte[] EDITION_KEY = ("edisou" + ANTE).getBytes(StandardCharsets.US_ASCII);

            @Override
            public void configure(VectorFilterCreationContext context) {
                context.cachePseudoHash(0);
                context.cachePseudoHash("shop_pack" + ANTE);
                context.cachePseudoHash("soul_Tarot" + ANTE);
                context.cachePseudoHash("soul_Spectral" + ANTE);
                context.cachePseudoHash("edisou" + ANTE);
            }

            @Override
            public VectorMaskBits filter(VectorSearchContext context) {
                VectorPrngStream packStream = context.createPrngStream("shop_pack" + ANTE);
                VectorPrngStream tarotSoulStream = context.createPrngStream("soul_Tarot" + ANTE);
                VectorPrngStream spectralSoulStream = context.createPrngStream("soul_Spectral" + ANTE);
                VectorPrngStream editionStream = context.createPrngStream("edisou" + ANTE);

                VectorMaskBits valid = context.validLaneMask();
                long foundBits = 0L;
                long soulLockedBits = 0L;
                long blackholeLockedBits = 0L;

                double[] packPolls = context.doubleScratch();

                // Ante 1 pack 1 is forced Buffoon Pack in the scalar implementation and is disabled
                // in the Performance search configuration. Packs 2-4 come from shop_pack1.
                for (int pack = 2; pack <= 4; pack++) {
                    context.getNextRandomTimesInto(packStream, 22.42, packPolls);

                    long arcanaBits = 0L;
                    long spectralBits = 0L;
                    long[] sizeBits = context.zeroedLongScratch();

                    for (int lane = 0; lane < context.laneCount(); lane++) {
                        long laneBit = 1L << lane;
                        if (!valid.laneIsSet(lane) || (foundBits & laneBit) != 0L) continue;

                        int packOrdinal = choosePack(packPolls[lane]);
                        int packSize = PACK_SIZES[packOrdinal];
                        sizeBits[packSize] |= laneBit;

                        if (ARCANA_PACKS[packOrdinal]) {
                            arcanaBits |= laneBit;
                        } else if (SPECTRAL_PACKS[packOrdinal]) {
                            spectralBits |= laneBit;
                        }
                    }

                    for (int option = 0; option < 5; option++) {
                        long hasOption = lanesWithOption(sizeBits, option);
                        long activeBits = arcanaBits & hasOption & ~foundBits & ~soulLockedBits;
                        if (activeBits != 0L) {
                            VectorMaskBits active = new VectorMaskBits(activeBits);
                            VectorMaskBits soulHits = VectorMaskBits.from(context.getNextRandom(tarotSoulStream, active)
                                    .compare(VectorOperators.GT, 0.997)).and(active);
                            soulLockedBits |= soulHits.bits();

                            if (soulHits.anyTrue()) {
                                VectorMaskBits negativeEditions = VectorMaskBits.from(context.getNextRandom(editionStream, soulHits)
                                        .compare(VectorOperators.GT, 0.997)).and(soulHits);
                                foundBits |= negativeEditions.bits();
                            }
                        }
                    }

                    for (int option = 0; option < 4; option++) {
                        long hasOption = lanesWithOption(sizeBits, option);
                        long soulActiveBits = spectralBits & hasOption & ~foundBits & ~soulLockedBits;
                        if (soulActiveBits != 0L) {
                            VectorMaskBits soulActive = new VectorMaskBits(soulActiveBits);
                            VectorMaskBits soulHits = VectorMaskBits.from(context.getNextRandom(spectralSoulStream, soulActive)
                                    .compare(VectorOperators.GT, 0.997)).and(soulActive);
                            soulLockedBits |= soulHits.bits();

                            if (soulHits.anyTrue()) {
                                VectorMaskBits negativeEditions = VectorMaskBits.from(context.getNextRandom(editionStream, soulHits)
                                        .compare(VectorOperators.GT, 0.997)).and(soulHits);
                                foundBits |= negativeEditions.bits();
                            }
                        }

                        long blackholeActiveBits = spectralBits & hasOption & ~foundBits & ~blackholeLockedBits;
                        if (blackholeActiveBits != 0L) {
                            VectorMaskBits blackholeActive = new VectorMaskBits(blackholeActiveBits);
                            VectorMaskBits blackholeHits = VectorMaskBits.from(context.getNextRandom(spectralSoulStream, blackholeActive)
                                    .compare(VectorOperators.GT, 0.997)).and(blackholeActive);
                            blackholeLockedBits |= blackholeHits.bits();
                        }
                    }

                    if (foundBits == valid.bits()) {
                        break;
                    }
                }

                return new VectorMaskBits(foundBits);
            }

            @Override
            public boolean filterScalar(byte[] seed) {
                double hashedSeed = pseudoHash(SEED_HASH_KEY, seed);
                double packState = pseudoHash(SHOP_PACK_KEY, seed);
                double tarotSoulState = 0.0;
                double spectralSoulState = 0.0;
                double editionState = 0.0;
                boolean soulLocked = false;
                boolean blackholeLocked = false;
                boolean tarotSoulInitialized = false;
                boolean spectralSoulInitialized = false;
                boolean editionInitialized = false;

                for (int pack = 2; pack <= 4; pack++) {
                    packState = nextState(packState);
                    int packCode = classifyInterestingPack(packState, hashedSeed);

                    if (packCode > 0) {
                        if (!tarotSoulInitialized) {
                            tarotSoulState = pseudoHash(SOUL_TAROT_KEY, seed);
                            tarotSoulInitialized = true;
                        }
                        for (int option = 0; option < packCode; option++) {
                            if (soulLocked) break;

                            tarotSoulState = nextState(tarotSoulState);
                            if (randomGreaterThanSoulThreshold(tarotSoulState, hashedSeed)) {
                                soulLocked = true;
                                if (!editionInitialized) {
                                    editionState = pseudoHash(EDITION_KEY, seed);
                                    editionInitialized = true;
                                }
                                editionState = nextState(editionState);
                                if (randomGreaterThanSoulThreshold(editionState, hashedSeed)) {
                                    return true;
                                }
                            }
                        }
                    } else if (packCode < 0) {
                        if (!spectralSoulInitialized) {
                            spectralSoulState = pseudoHash(SOUL_SPECTRAL_KEY, seed);
                            spectralSoulInitialized = true;
                        }
                        for (int option = 0; option < -packCode; option++) {
                            if (!soulLocked) {
                                spectralSoulState = nextState(spectralSoulState);
                                if (randomGreaterThanSoulThreshold(spectralSoulState, hashedSeed)) {
                                    soulLocked = true;
                                    if (!editionInitialized) {
                                        editionState = pseudoHash(EDITION_KEY, seed);
                                        editionInitialized = true;
                                    }
                                    editionState = nextState(editionState);
                                    if (randomGreaterThanSoulThreshold(editionState, hashedSeed)) {
                                        return true;
                                    }
                                }
                            }

                            if (!blackholeLocked) {
                                spectralSoulState = nextState(spectralSoulState);
                                if (randomGreaterThanSoulThreshold(spectralSoulState, hashedSeed)) {
                                    blackholeLocked = true;
                                }
                            }
                        }
                    }
                }

                return false;
            }

            @Override
            public long filterScalarBatch(byte[] suffixSeed, int firstDigitOffset, int batchSize, byte[] seedDigits) {
                double seedSuffix = partialSuffixHash(SEED_HASH_KEY.length, suffixSeed);
                double packSuffix = partialSuffixHash(SHOP_PACK_KEY.length, suffixSeed);
                double tarotSuffix = 0.0;
                double spectralSuffix = 0.0;
                double editionSuffix = 0.0;
                boolean tarotSuffixInitialized = false;
                boolean spectralSuffixInitialized = false;
                boolean editionSuffixInitialized = false;
                long matches = 0L;

                for (int lane = 0; lane < batchSize; lane++) {
                    byte firstDigit = seedDigits[firstDigitOffset + lane];
                    double hashedSeed = finishSeedPartial(firstDigit, seedSuffix, SEED_HASH_KEY.length);
                    double packState = hashShopPack(finishSeedPartial(firstDigit, packSuffix, SHOP_PACK_KEY.length));
                    double tarotSoulState = 0.0;
                    double spectralSoulState = 0.0;
                    double editionState = 0.0;
                    boolean soulLocked = false;
                    boolean blackholeLocked = false;
                    boolean tarotSoulInitialized = false;
                    boolean spectralSoulInitialized = false;
                    boolean editionInitialized = false;

                    for (int pack = 2; pack <= 4; pack++) {
                        packState = nextState(packState);
                        int packCode = classifyInterestingPack(packState, hashedSeed);

                        if (packCode > 0) {
                            if (!tarotSoulInitialized) {
                                if (!tarotSuffixInitialized) {
                                    tarotSuffix = partialSuffixHash(SOUL_TAROT_KEY.length, suffixSeed);
                                    tarotSuffixInitialized = true;
                                }
                                tarotSoulState = hashSoulTarot(finishSeedPartial(firstDigit, tarotSuffix,
                                        SOUL_TAROT_KEY.length));
                                tarotSoulInitialized = true;
                            }
                            for (int option = 0; option < packCode; option++) {
                                if (soulLocked) break;

                                tarotSoulState = nextState(tarotSoulState);
                                if (randomGreaterThanSoulThreshold(tarotSoulState, hashedSeed)) {
                                    soulLocked = true;
                                    if (!editionInitialized) {
                                        if (!editionSuffixInitialized) {
                                            editionSuffix = partialSuffixHash(EDITION_KEY.length, suffixSeed);
                                            editionSuffixInitialized = true;
                                        }
                                        editionState = hashEdition(finishSeedPartial(firstDigit, editionSuffix,
                                                EDITION_KEY.length));
                                        editionInitialized = true;
                                    }
                                    editionState = nextState(editionState);
                                    if (randomGreaterThanSoulThreshold(editionState, hashedSeed)) {
                                        matches |= 1L << lane;
                                        break;
                                    }
                                }
                            }
                        } else if (packCode < 0) {
                            if (!spectralSoulInitialized) {
                                if (!spectralSuffixInitialized) {
                                    spectralSuffix = partialSuffixHash(SOUL_SPECTRAL_KEY.length, suffixSeed);
                                    spectralSuffixInitialized = true;
                                }
                                spectralSoulState = hashSoulSpectral(finishSeedPartial(firstDigit, spectralSuffix,
                                        SOUL_SPECTRAL_KEY.length));
                                spectralSoulInitialized = true;
                            }
                            for (int option = 0; option < -packCode; option++) {
                                if (!soulLocked) {
                                    spectralSoulState = nextState(spectralSoulState);
                                    if (randomGreaterThanSoulThreshold(spectralSoulState, hashedSeed)) {
                                        soulLocked = true;
                                        if (!editionInitialized) {
                                            if (!editionSuffixInitialized) {
                                                editionSuffix = partialSuffixHash(EDITION_KEY.length, suffixSeed);
                                                editionSuffixInitialized = true;
                                            }
                                            editionState = hashEdition(finishSeedPartial(firstDigit, editionSuffix,
                                                    EDITION_KEY.length));
                                            editionInitialized = true;
                                        }
                                        editionState = nextState(editionState);
                                        if (randomGreaterThanSoulThreshold(editionState, hashedSeed)) {
                                            matches |= 1L << lane;
                                            break;
                                        }
                                    }
                                }

                                if (!blackholeLocked) {
                                    spectralSoulState = nextState(spectralSoulState);
                                    if (randomGreaterThanSoulThreshold(spectralSoulState, hashedSeed)) {
                                        blackholeLocked = true;
                                    }
                                }
                            }
                        }

                        if ((matches & (1L << lane)) != 0L) {
                            break;
                        }
                    }
                }

                return matches;
            }

            private static int choosePack(double poll) {
                for (int i = 0; i < PACK_THRESHOLDS.length; i++) {
                    if (poll <= PACK_THRESHOLDS[i]) return i;
                }
                return PACK_THRESHOLDS.length - 1;
            }

            private static int classifyInterestingPack(double state, double hashedSeed) {
                long random = randomBits((state + hashedSeed) / 2.0);
                if (random <= PACK_ARCANA_SIZE3_BITS) return 3;
                if (random <= PACK_ARCANA_END_BITS) return 5;
                if (random <= PACK_BEFORE_SPECTRAL_BITS) return 0;
                if (random <= PACK_SPECTRAL_SIZE2_BITS) return -2;
                return -4;
            }

            private static boolean randomGreaterThanSoulThreshold(double state, double hashedSeed) {
                return randomBits((state + hashedSeed) / 2.0) > SOUL_THRESHOLD_BITS;
            }

            private static long lanesWithOption(long[] sizeBits, int option) {
                long bits = 0L;
                for (int size = option + 1; size < sizeBits.length; size++) {
                    bits |= sizeBits[size];
                }
                return bits;
            }

            private static double[] packThresholds() {
                double[] weights = {
                        4, 2, 0.5,
                        4, 2, 0.5,
                        4, 2, 0.5,
                        1.2, 0.6, 0.15,
                        0.6, 0.3, 0.07
                };
                double[] thresholds = new double[weights.length];
                double sum = 0.0;
                for (int i = 0; i < weights.length; i++) {
                    sum += weights[i];
                    thresholds[i] = sum;
                }
                return thresholds;
            }

            private static long packRandomThreshold(double threshold) {
                return (long) Math.floor((threshold / 22.42) * RANDOM_SCALE);
            }

            private static int[] packSizes() {
                return new int[]{
                        3, 5, 5,
                        3, 5, 5,
                        3, 5, 5,
                        2, 4, 4,
                        2, 4, 4
                };
            }

            private static boolean[] arcanaPacks() {
                boolean[] result = new boolean[15];
                result[0] = result[1] = result[2] = true;
                return result;
            }

            private static boolean[] spectralPacks() {
                boolean[] result = new boolean[15];
                result[12] = result[13] = result[14] = true;
                return result;
            }

            private static double nextState(double state) {
                double value = state * 1.72431234 + 2.134453429141;
                return round13(value - Math.floor(value));
            }

            private static double pseudoHash(byte[] key, byte[] seed) {
                int totalLen = key.length + seed.length;
                double num = 1.0;
                for (int i = totalLen; i > 0; i--) {
                    byte value = (i > key.length) ? seed[i - key.length - 1] : key[i - 1];
                    double term = 1.1239285023 / num * value * Math.PI + Math.PI * i;
                    num = term - Math.floor(term);
                }
                return num;
            }

            private static double partialSuffixHash(int keyLength, byte[] suffixSeed) {
                double num = 1.0;
                for (int i = 7; i >= 1; i--) {
                    double term = 1.1239285023 / num * suffixSeed[i] * Math.PI + (i + keyLength + 1) * Math.PI;
                    num = term - Math.floor(term);
                }
                return num;
            }

            private static double finishSeedPartial(byte firstDigit, double suffixPartial, int keyLength) {
                double term = 1.1239285023 / suffixPartial * firstDigit * Math.PI + (keyLength + 1) * Math.PI;
                return term - Math.floor(term);
            }

            private static double hashShopPack(double num) {
                num = keyStep(num, '1', 10);
                num = keyStep(num, 'k', 9);
                num = keyStep(num, 'c', 8);
                num = keyStep(num, 'a', 7);
                num = keyStep(num, 'p', 6);
                num = keyStep(num, '_', 5);
                num = keyStep(num, 'p', 4);
                num = keyStep(num, 'o', 3);
                num = keyStep(num, 'h', 2);
                return keyStep(num, 's', 1);
            }

            private static double hashSoulTarot(double num) {
                num = keyStep(num, '1', 11);
                num = keyStep(num, 't', 10);
                num = keyStep(num, 'o', 9);
                num = keyStep(num, 'r', 8);
                num = keyStep(num, 'a', 7);
                num = keyStep(num, 'T', 6);
                num = keyStep(num, '_', 5);
                num = keyStep(num, 'l', 4);
                num = keyStep(num, 'u', 3);
                num = keyStep(num, 'o', 2);
                return keyStep(num, 's', 1);
            }

            private static double hashSoulSpectral(double num) {
                num = keyStep(num, '1', 14);
                num = keyStep(num, 'l', 13);
                num = keyStep(num, 'a', 12);
                num = keyStep(num, 'r', 11);
                num = keyStep(num, 't', 10);
                num = keyStep(num, 'c', 9);
                num = keyStep(num, 'e', 8);
                num = keyStep(num, 'p', 7);
                num = keyStep(num, 'S', 6);
                num = keyStep(num, '_', 5);
                num = keyStep(num, 'l', 4);
                num = keyStep(num, 'u', 3);
                num = keyStep(num, 'o', 2);
                return keyStep(num, 's', 1);
            }

            private static double hashEdition(double num) {
                num = keyStep(num, '1', 7);
                num = keyStep(num, 'u', 6);
                num = keyStep(num, 'o', 5);
                num = keyStep(num, 's', 4);
                num = keyStep(num, 'i', 3);
                num = keyStep(num, 'd', 2);
                return keyStep(num, 'e', 1);
            }

            private static double keyStep(double num, int keyCharacter, int position) {
                double term = 1.1239285023 / num * keyCharacter * Math.PI + position * Math.PI;
                return term - Math.floor(term);
            }

            private static final double INV_PREC = Math.pow(10, 13);
            private static final double TWO_INV_PREC = Math.pow(2, 13);
            private static final double FIVE_INV_PREC = Math.pow(5, 13);

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

            private static long randomBits(double seed) {
                return randInt(seed) & RANDOM_MANTISSA_MASK;
            }

            private static long randInt(double seed) {
                long state;
                long random = 0L;
                int r = 0x11090601;

                long minimum = 1L << (r & 255);
                r >>= 8;
                seed = seed * Math.PI + Math.E;
                state = Double.doubleToLongBits(seed);
                if (state < minimum) state += minimum;
                state = warmAndStep31(state);
                random ^= state;

                minimum = 1L << (r & 255);
                r >>= 8;
                seed = seed * Math.PI + Math.E;
                state = Double.doubleToLongBits(seed);
                if (state < minimum) state += minimum;
                state = warmAndStep19(state);
                random ^= state;

                minimum = 1L << (r & 255);
                r >>= 8;
                seed = seed * Math.PI + Math.E;
                state = Double.doubleToLongBits(seed);
                if (state < minimum) state += minimum;
                state = warmAndStep24(state);
                random ^= state;

                minimum = 1L << (r & 255);
                seed = seed * Math.PI + Math.E;
                state = Double.doubleToLongBits(seed);
                if (state < minimum) state += minimum;
                state = warmAndStep21(state);
                return random ^ state;
            }

            private static long warmAndStep31(long state) {
                for (int i = 0; i < 5; i++) {
                    state = step31(state);
                    state = step31(state);
                }
                return step31(state);
            }

            private static long warmAndStep19(long state) {
                for (int i = 0; i < 5; i++) {
                    state = step19(state);
                    state = step19(state);
                }
                return step19(state);
            }

            private static long warmAndStep24(long state) {
                for (int i = 0; i < 5; i++) {
                    state = step24(state);
                    state = step24(state);
                }
                return step24(state);
            }

            private static long warmAndStep21(long state) {
                for (int i = 0; i < 5; i++) {
                    state = step21(state);
                    state = step21(state);
                }
                return step21(state);
            }

            private static long step31(long state) {
                return (((state << 31) ^ state) >>> 45) ^ ((state & (MAX_UINT64 << 1)) << 18);
            }

            private static long step19(long state) {
                return (((state << 19) ^ state) >>> 30) ^ ((state & (MAX_UINT64 << 6)) << 28);
            }

            private static long step24(long state) {
                return (((state << 24) ^ state) >>> 48) ^ ((state & (MAX_UINT64 << 9)) << 7);
            }

            private static long step21(long state) {
                return (((state << 21) ^ state) >>> 39) ^ ((state & (MAX_UINT64 << 17)) << 8);
            }
        };
    }
}
