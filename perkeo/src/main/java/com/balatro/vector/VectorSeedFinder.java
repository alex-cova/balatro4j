package com.balatro.vector;

import com.balatro.api.Balatro;
import com.balatro.api.Filter;
import com.balatro.api.SeedFinder;
import jdk.incubator.vector.DoubleVector;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class VectorSeedFinder implements SeedFinder {

    private static final byte[] SEED_DIGITS = {
            '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
            'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
            'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    private final int parallelism;
    private final int seedsPerThread;
    private final Set<String> foundSeeds = ConcurrentHashMap.newKeySet();
    private final LongAdder count = new LongAdder();

    private VectorSeedFilter vectorFilter = VectorFilters.findAll();
    private Filter scalarVerifier;
    private Consumer<Balatro> configuration;
    private BiConsumer<String, Integer> progressListener;
    private boolean autoconfigure;

    public VectorSeedFinder() {
        this(Runtime.getRuntime().availableProcessors(), 1_000_000);
    }

    public VectorSeedFinder(int seedsPerThread) {
        this(Runtime.getRuntime().availableProcessors(), seedsPerThread);
    }

    public VectorSeedFinder(int parallelism, int seedsPerThread) {
        this.parallelism = parallelism;
        this.seedsPerThread = seedsPerThread;
    }

    public VectorSeedFinder vectorFilter(VectorSeedFilter vectorFilter) {
        this.vectorFilter = vectorFilter;
        return this;
    }

    @Override
    public VectorSeedFinder filter(Filter filter) {
        this.scalarVerifier = filter;
        return this;
    }

    @Override
    public VectorSeedFinder autoConfigure() {
        this.autoconfigure = true;
        return this;
    }

    @Override
    public VectorSeedFinder configuration(Consumer<Balatro> configuration) {
        this.configuration = configuration;
        return this;
    }

    @Override
    public VectorSeedFinder progressListener(BiConsumer<String, Integer> progressListener) {
        this.progressListener = progressListener;
        return this;
    }

    @Override
    public Set<String> find() {
        VectorFilterCreationContext creationContext = new VectorFilterCreationContext();
        creationContext.cachePseudoHash(0);
        vectorFilter.configure(creationContext);

        DecimalFormat format = new DecimalFormat("#,###");
        long total = (long) seedsPerThread * parallelism;
        long started = System.currentTimeMillis();
        int lanes = VectorSearchContext.DOUBLE_SPECIES.length();
        int chunkSize = Math.min(seedsPerThread, 1_000_000);
        long chunkCount = (total + chunkSize - 1L) / chunkSize;

        System.out.println("Searching " + format.format(total) + " seeds with " + parallelism
                + " workers, " + format.format(chunkCount) + " chunks of up to " + format.format(chunkSize)
                + " seeds");
        System.out.println("Vector lanes: " + lanes);

        ForkJoinPool pool = new ForkJoinPool(parallelism);
        List<ForkJoinTask<?>> tasks = new ArrayList<>(parallelism);
        Set<Integer> cachedPseudoHashLengths = creationContext.cachedPseudoHashLengths();
        AtomicLong nextChunkStart = new AtomicLong(0L);
        for (int i = 0; i < parallelism; i++) {
            ForkJoinTask<?> task = pool.submit(() -> {
                while (true) {
                    long chunkStartIndex = nextChunkStart.getAndAdd(chunkSize);
                    if (chunkStartIndex >= total) {
                        return;
                    }
                    int amount = (int) Math.min(chunkSize, total - chunkStartIndex);
                    generate(chunkStartIndex, amount, cachedPseudoHashLengths);
                }
            });
            tasks.add(task);
        }

        long last = 0;
        int seconds = 0;
        while (true) {
            boolean done = true;
            int remainingTasks = 0;
            for (ForkJoinTask<?> task : tasks) {
                if (!task.isDone()) {
                    done = false;
                    remainingTasks++;
                }
            }
            if (done) break;

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }

            seconds++;
            long analyzed = count.longValue();
            if (seconds % 2 == 0) {
                long ops = analyzed - last;
                String message = format.format(ops) + " ops/s seeds analyzed: " + format.format(analyzed)
                        + " " + getMemory() + " remaining tasks: " + remainingTasks
                        + ", seeds found: " + format.format(foundSeeds.size());
                System.out.println(message);

                if (progressListener != null) {
                    progressListener.accept(format.format(ops) + " ops/s, found: " + format.format(foundSeeds.size()),
                            Math.round(((float) analyzed / total) * 100.0f));
                }

                last = analyzed;
            }
        }

        for (ForkJoinTask<?> task : tasks) {
            task.join();
        }
        pool.shutdown();

        long elapsed = Math.max(1L, System.currentTimeMillis() - started);
        System.out.println("Vector search finished: " + format.format(count.longValue() * 1000L / elapsed)
                + " seeds/sec, seeds analyzed: " + format.format(count.longValue())
                + ", seeds found: " + format.format(foundSeeds.size()));
        return foundSeeds;
    }

    private void generate(long startIndex, int amount, Set<Integer> cachedPseudoHashLengths) {
        if (vectorFilter instanceof ScalarSeedPrefilter scalarSeedPrefilter
                && VectorSearchContext.DOUBLE_SPECIES.length() < 4) {
            generateScalar(startIndex, amount, scalarSeedPrefilter);
            return;
        }

        int lanes = VectorSearchContext.DOUBLE_SPECIES.length();
        byte[][] seeds = new byte[lanes][VectorSearchContext.MAX_SEED_LENGTH];
        double[][] chars = new double[VectorSearchContext.MAX_SEED_LENGTH][lanes];
        DoubleVector[] seedVectors = new DoubleVector[VectorSearchContext.MAX_SEED_LENGTH];
        boolean[] valid = new boolean[lanes];
        double[] vectorScratch = new double[lanes];
        long endIndex = startIndex + amount;
        long cursor = startIndex;

        while (cursor < endIndex) {
            long suffixOrdinal = cursor / SEED_DIGITS.length;
            int firstDigitOffset = (int) (cursor % SEED_DIGITS.length);
            seedSuffixFromOrdinal(suffixOrdinal, seeds[0]);

            while (firstDigitOffset < SEED_DIGITS.length && cursor < endIndex) {
                int batchSize = (int) Math.min(Math.min(lanes, SEED_DIGITS.length - firstDigitOffset),
                        endIndex - cursor);

                for (int lane = 0; lane < lanes; lane++) {
                    valid[lane] = lane < batchSize;
                    if (valid[lane]) {
                        seeds[lane][0] = SEED_DIGITS[firstDigitOffset + lane];
                        for (int i = 1; i < VectorSearchContext.MAX_SEED_LENGTH; i++) {
                            seeds[lane][i] = seeds[0][i];
                        }
                    }
                    for (int i = 0; i < VectorSearchContext.MAX_SEED_LENGTH; i++) {
                        chars[i][lane] = valid[lane] ? seeds[lane][i] : 0;
                    }
                }

                for (int i = 0; i < seedVectors.length; i++) {
                    seedVectors[i] = DoubleVector.fromArray(VectorSearchContext.DOUBLE_SPECIES, chars[i], 0);
                }

                Map<Integer, DoubleVector> precomputedHashes = precomputePartialHashes(seedVectors[0], seeds[0],
                        cachedPseudoHashLengths, vectorScratch);
                VectorSearchContext context = new VectorSearchContext(seedVectors, valid,
                        VectorSearchContext.MAX_SEED_LENGTH, precomputedHashes);
                VectorMaskBits matches = vectorFilter.filter(context);
                count.add(batchSize);

                if (!matches.allFalse()) {
                    for (int lane = 0; lane < lanes; lane++) {
                        if (!matches.laneIsSet(lane) || !valid[lane]) continue;

                        String seed = new String(seeds[lane]);
                        if (scalarVerifier == null || verifyScalar(seed)) {
                            foundSeeds.add(seed);
                        }
                    }
                }

                cursor += batchSize;
                firstDigitOffset += batchSize;
            }
        }
    }

    private void generateScalar(long startIndex, int amount, ScalarSeedPrefilter scalarSeedPrefilter) {
        byte[] seed = new byte[VectorSearchContext.MAX_SEED_LENGTH];
        long pendingCount = 0L;
        long endIndex = startIndex + amount;
        long cursor = startIndex;

        while (cursor < endIndex) {
            long suffixOrdinal = cursor / SEED_DIGITS.length;
            int firstDigitOffset = (int) (cursor % SEED_DIGITS.length);
            int batchSize = (int) Math.min(SEED_DIGITS.length - firstDigitOffset, endIndex - cursor);

            seedSuffixFromOrdinal(suffixOrdinal, seed);
            long matches = scalarSeedPrefilter.filterScalarBatch(seed, firstDigitOffset, batchSize, SEED_DIGITS);

            while (matches != 0L) {
                int lane = Long.numberOfTrailingZeros(matches);
                seed[0] = SEED_DIGITS[firstDigitOffset + lane];
                String seedString = new String(seed);
                if (scalarVerifier == null || verifyScalar(seedString)) {
                    foundSeeds.add(seedString);
                }
                matches &= matches - 1;
            }

            pendingCount += batchSize;
            if (pendingCount >= 4096) {
                count.add(pendingCount);
                pendingCount = 0L;
            }

            cursor += batchSize;
        }

        if (pendingCount != 0L) {
            count.add(pendingCount);
        }
    }

    private static void seedSuffixFromOrdinal(long ordinal, byte[] seed) {
        for (int i = VectorSearchContext.MAX_SEED_LENGTH - 1; i >= 1; i--) {
            seed[i] = SEED_DIGITS[(int) (ordinal % SEED_DIGITS.length)];
            ordinal /= SEED_DIGITS.length;
        }
    }

    private static Map<Integer, DoubleVector> precomputePartialHashes(DoubleVector firstCharacterVector,
                                                                      byte[] suffixSeed,
                                                                      Set<Integer> keyLengths,
                                                                      double[] scratch) {
        Map<Integer, DoubleVector> hashes = new java.util.HashMap<>(keyLengths.size() * 2);
        for (int keyLength : keyLengths) {
            double num = 1.0;
            for (int i = VectorSearchContext.MAX_SEED_LENGTH - 1; i >= 1; i--) {
                double term = 1.1239285023 / num * suffixSeed[i] * Math.PI + (i + keyLength + 1) * Math.PI;
                num = term - Math.floor(term);
            }

            DoubleVector vector = DoubleVector.broadcast(VectorSearchContext.DOUBLE_SPECIES, 1.1239285023 / num)
                    .mul(firstCharacterVector)
                    .mul(Math.PI)
                    .add((keyLength + 1) * Math.PI);
            hashes.put(keyLength, fract(vector, scratch));
        }
        return hashes;
    }

    private static DoubleVector fract(DoubleVector vector, double[] scratch) {
        vector.intoArray(scratch, 0);
        for (int i = 0; i < scratch.length; i++) {
            scratch[i] -= Math.floor(scratch[i]);
        }
        return DoubleVector.fromArray(VectorSearchContext.DOUBLE_SPECIES, scratch, 0);
    }

    private boolean verifyScalar(String seed) {
        Balatro builder = Balatro.builder(seed, 1);

        if (autoconfigure) {
            builder.disableAll();
            scalarVerifier.configure(builder);
        } else if (configuration != null) {
            configuration.accept(builder);
        }

        return scalarVerifier.filter(builder.analyze());
    }

    private static String getMemory() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        return "Used memory: " + usedMemory / 1024 / 1024 + " MB";
    }
}
