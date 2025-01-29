package com.balatro;

import com.balatro.api.*;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class SeedFinderImpl implements SeedFinder {

    private final int parallelism;
    private final int seedsPerThread;
    private final AtomicBoolean lock = new AtomicBoolean(false);
    private Filter filter;
    private final List<Run> foundSeeds = new ArrayList<>();
    private Consumer<BalatroBuilder> configuration;

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
    public SeedFinder configuration(Consumer<BalatroBuilder> configuration) {
        this.configuration = configuration;
        return this;
    }

    @Override
    public SeedFinder filter(Filter filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public List<Run> find() {
        search();
        return foundSeeds;
    }

    private void search() {
        if (filter == null) {
            throw new IllegalStateException("No filters were added");
        }

        if (lock.get()) {
            return;
        }

        lock.set(true);
        count.set(0);

        List<ForkJoinTask<?>> tasks = new ArrayList<>(parallelism);

        for (int i = 0; i < parallelism; i++) {
            var task = ForkJoinPool.commonPool().submit(this::generate);
            tasks.add(task);
        }

        int time = 0;

        var format = new DecimalFormat("#,###");

        System.out.println("Searching " + format.format((long) parallelism * seedsPerThread) + " seeds with " + parallelism + " tasks");

        while (!tasks.stream().allMatch(ForkJoinTask::isDone)) {
            try {
                Thread.sleep(1000);

                time++;

                if (time % 10 == 0) {
                    System.out.println(format.format(count.get() / time) + " ops/s seeds analyzed: " + format.format(count.get()) + " " + getMemory());
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.print("Finished! " + format.format(count.get() / time) + " Seeds/Sec, Seeds analyzed: " + format.format(count.get()) + "\n");
        System.out.println(getMemory());
    }

    private @NotNull String getMemory() {
        Runtime runtime = Runtime.getRuntime();

        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        return "Used memory: " + usedMemory / 1024 / 1024 + " MB";
    }

    static AtomicInteger count = new AtomicInteger(0);

    private boolean checkFilters(Run run) {
        return filter.filter(run);
    }

    private void generate() {
        for (int i = 0; i < seedsPerThread; i++) {
            try {
                count.incrementAndGet();
                var seed = BalatroImpl.generateRandomSeed();

                var builder = Balatro.builder(seed);

                if (configuration != null) {
                    configuration.accept(builder);
                }

                var run = builder
                        .build();

                if (checkFilters(run)) {
                    foundSeeds.add(run);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                break;
            }
        }
    }

}
