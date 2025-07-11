package com.balatro;

import com.balatro.api.Balatro;
import com.balatro.api.Filter;
import com.balatro.api.Run;
import com.balatro.api.SeedFinder;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SeedFinderImpl implements SeedFinder {

    private final int parallelism;
    private final int seedsPerThread;
    private final AtomicBoolean lock = new AtomicBoolean(false);
    private Filter filter;
    private final Set<String> foundSeeds = new HashSet<>();
    private Consumer<Balatro> configuration;
    private boolean autoconfigure;
    private BiConsumer<String, Integer> progressListener;

    public SeedFinderImpl() {
        this(Runtime.getRuntime().availableProcessors(), 1_000_000);
    }

    public SeedFinderImpl(int seedsPerThread) {
        this(Runtime.getRuntime().availableProcessors(), seedsPerThread);
    }

    public SeedFinderImpl(int parallelism, int seedsPerThread) {
        this.parallelism = parallelism;
        if (parallelism > Runtime.getRuntime().availableProcessors()) {
            throw new IllegalArgumentException("Parallelism cannot be greater than available processors");
        }

        this.seedsPerThread = seedsPerThread;
    }

    @Override
    public SeedFinder configuration(Consumer<Balatro> configuration) {
        this.configuration = configuration;
        return this;
    }

    @Override
    public SeedFinder progressListener(BiConsumer<String, Integer> progressListener) {
        this.progressListener = progressListener;
        return this;
    }

    @Override
    public SeedFinder filter(Filter filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public SeedFinder autoConfigure() {
        autoconfigure = true;
        return this;
    }

    @Override
    public Set<String> find() {
        search();
        return foundSeeds;
    }

    private int time = 0;

    private void search() {
        if (filter == null) {
            throw new IllegalStateException("No filters were added");
        }

        if (autoconfigure) {
            var builder = Balatro.builder("alex", 1);
            builder.disableAll();
            filter.configure(builder);
            builder.printConfigurations();
        }

        if (lock.get()) {
            return;
        }

        lock.set(true);
        count.reset();
        time = 0;
        var init = LocalDateTime.now();
        var format = new DecimalFormat("#,###");

        fork(format);

        System.out.println("--------------------------------------------------------------------------------------------");
        System.out.println("FINISHED: " + (init.until(LocalDateTime.now(), ChronoUnit.SECONDS)) + " seconds | "
                           + format.format(count.longValue() / Math.max(time, 1)) + " Seeds/sec, Seeds analyzed: " + format.format(count.longValue()));
        System.out.println(getMemory());
        System.out.println("Seeds found: " + format.format(foundSeeds.size()));
        System.out.println("--------------------------------------------------------------------------------------------");

    }

    private void fork(DecimalFormat format) {
        int divisor = 10;

        if (seedsPerThread <= 1_000_000) {
            divisor = 1;
        }

        if (seedsPerThread <= 100_000_000) {
            divisor = 4;
        }

        final int amount = seedsPerThread / divisor;
        final long total = (long) seedsPerThread * parallelism;

        System.out.println("Searching " + format.format(total) + " seeds with " + parallelism + " tasks, " + format.format(amount) + " seeds per task");
        System.out.println("Divisor: " + divisor);

        long last = 0;

        for (int k = 0; k < divisor; k++) {
            System.out.println("---------------------------------------------------------------------------------------");
            List<ForkJoinTask<?>> tasks = new ArrayList<>(parallelism);

            for (int i = 0; i < parallelism; i++) {
                var task = ForkJoinPool.commonPool().submit(() -> generate(amount));
                tasks.add(task);
            }

            long c;

            while (!tasks.stream().allMatch(ForkJoinTask::isDone)) {
                try {
                    Thread.sleep(1000);

                    time++;
                    c = SeedFinderImpl.count.longValue();
                    if (time % 2 == 0) {
                        var remainingTasks = tasks.stream()
                                .filter(a -> !a.isDone())
                                .count();
                        System.out.println(format.format(c - last) + " ops/s seeds analyzed: " + format.format(c) + " " + getMemory() + " remaining tasks: " + remainingTasks);

                        if (progressListener != null) {
                            progressListener.accept(format.format(c - last) + " ops/s", Math.round(((float) c / total) * 100.0f));
                        }

                        last = c;
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static @NotNull String getMemory() {
        Runtime runtime = Runtime.getRuntime();

        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        return "Used memory: " + usedMemory / 1024 / 1024 + " MB";
    }

    static LongAdder count = new LongAdder();

    private boolean checkFilters(Run run) {
        return filter.filter(run);
    }

    private void generate(int amount) {
        for (int i = 0; i < amount; i++) {
            try {
                count.increment();
                var seed = BalatroImpl.generateRandomSeed();

                var builder = Balatro.builder(seed, 1);

                if (autoconfigure) {
                    builder.disableAll();
                    filter.configure(builder);
                } else {
                    if (configuration != null) {
                        configuration.accept(builder);
                    }
                }

                var run = builder
                        .analyze();

                if (checkFilters(run)) {
                    foundSeeds.add(new String(seed));
                }
            } catch (Exception ex) {
                Logger.getLogger(SeedFinderImpl.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }
        }
    }

}
