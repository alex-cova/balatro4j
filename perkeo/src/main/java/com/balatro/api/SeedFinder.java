package com.balatro.api;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface SeedFinder {

    SeedFinder filter(Filter filter);

    SeedFinder autoConfigure();

    SeedFinder configuration(Consumer<Balatro> configuration);

    SeedFinder progressListener(BiConsumer<String, Integer> progressListener);

    Set<String> find();
}
