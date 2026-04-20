package com.balatro.cache;

import com.balatro.api.Balatro;
import com.balatro.api.Item;
import com.balatro.enums.*;
import com.balatro.impl.SeedFinderImpl;
import com.balatro.structs.EditionItem;
import com.balatro.structs.ItemPosition;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.balatro.enums.LegendaryJoker.*;

public class PreProcessedSeeds {

    /**
     * Case-insensitive lookup table of every searchable item, built once from all supported enums.
     * Replaces the 10 linear scans that the original {@link #parseSearch} performed per query.
     * First-wins semantics (via putIfAbsent) mirror the original enum-iteration order.
     */
    private static final Map<String, Item> ITEM_LOOKUP = buildItemLookup();

    private static @NotNull Map<String, Item> buildItemLookup() {
        Map<String, Item> map = new HashMap<>(256);
        addAll(map, Spectral.values());
        addAll(map, CommonJoker.values());
        addAll(map, RareJoker.values());
        addAll(map, LegendaryJoker.values());
        addAll(map, UnCommonJoker.values());
        addAll(map, Tag.values());
        addAll(map, Boss.values());
        addAll(map, Planet.values());
        addAll(map, Tarot.values());
        addAll(map, Voucher.values());
        return map;
    }

    private static void addAll(Map<String, Item> map, Item @NotNull [] values) {
        for (Item v : values) {
            map.putIfAbsent(v.getName().toLowerCase(Locale.ROOT), v);
        }
    }

    private List<Data> dataList;

    public PreProcessedSeeds loadFile(@NotNull File file) {
        if (!file.exists()) {
            dataList = Collections.emptyList();
            return this;
        }
        dataList = JokerFile.readFile(file);
        return this;
    }

    public static void main(String[] args) {
        var p = new PreProcessedSeeds();
        p.start(Runtime.getRuntime().availableProcessors(), 2_000_000_000);

        var result = p.search(List.of(Perkeo, Triboulet, RareJoker.Blueprint, RareJoker.Brainstorm,
                UnCommonJoker.Sock_and_Buskin, CommonJoker.Hanging_Chad, RareJoker.Invisible_Joker));

        System.out.println("Found " + result.size() + " results");

        for (QueryResult queryResult : result) {
            System.out.println(queryResult.seed() + " " + queryResult.score());
        }
    }

    public void start(int parallelism, int seedsPerThread) {
        var decimalFormat = new DecimalFormat("#,##0");
        var file = new File("perkeo.jkr");
        System.out.println("Loading seeds from cache: " + file.getAbsolutePath());

        if (file.exists()) {
            long init = System.currentTimeMillis();
            dataList = JokerFile.readFile(file);
            System.out.println("Loaded " + decimalFormat.format(dataList.size()) + " seeds from cache in " + (System.currentTimeMillis() - init) + " ms");
        } else {
            var seeds = Balatro.search(parallelism, seedsPerThread)
                    .configuration(config -> config.maxAnte(1).disableShopQueue()
                            .disablePack(PackKind.Buffoon))
                    .filter(Perkeo.inPack(Edition.Negative)
                            .or(Triboulet.inPack(Edition.Negative))
                            .or(Yorick.inPack(Edition.Negative))
                            .or(Chicot.inPack(Edition.Negative))
                            .or(Canio.inPack(Edition.Negative)))
                    .find();

            System.out.println("Seeds found: " + decimalFormat.format(seeds.size()) + " Analyzing...");

            var start = LocalDateTime.now();

            dataList = seeds.parallelStream()
                    .map(seed -> Balatro.builder(seed, 8)
                            .enableAll()
                            .disablePack(PackKind.Standard)
                            .analyze())
                    .map(Data::new)
                    .toList();

            seeds.clear();

            System.out.println("Finished in: " + (start.until(LocalDateTime.now(), ChronoUnit.SECONDS)) + " Seconds");
            System.gc();
            System.out.println("Used Memory: " + SeedFinderImpl.getMemory());

            var baos = new ByteArrayOutputStream();

            for (Data data : dataList) {
                JokerFile.write(baos, data);
            }

            System.out.println("File size: " + decimalFormat.format(baos.size()));

            try {
                Files.write(file.toPath(), baos.toByteArray());
                baos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }


    public List<QueryResult> search(@NotNull List<? extends Item> items) {
        // Build the query list directly — streams add allocation overhead for a small, fixed-size input.
        final List<ItemPosition> editionItems = new ArrayList<>(items.size());
        for (Item i : items) {
            if (i instanceof EditionItem ei) {
                editionItems.add(new ItemPosition(ei, 8));
            } else {
                editionItems.add(new ItemPosition(i, 8));
            }
        }

        return runQuery(editionItems);
    }

    public Set<String> searchByName(@NotNull Set<String> tokens) {
        final List<Query> queries = new ArrayList<>(tokens.size());
        for (String token : tokens) {
            queries.add(new Query(token));
        }
        return find(queries).stream()
                .map(QueryResult::seed)
                .collect(Collectors.toSet());
    }

    public List<QueryResult> find(List<Query> tokens) {
        return runQuery(parseSearch(tokens));
    }

    /**
     * Scans {@code dataList} for every seed matching the given item positions.
     * Parallelized because each {@link Data#contains} call is pure/read-only and the list
     * can reach millions of entries; the fork-join pool handles small inputs gracefully.
     */
    private @NotNull List<QueryResult> runQuery(@NotNull List<ItemPosition> items) {
        final List<Data> source = dataList;
        if (source == null || source.isEmpty()) {
            return new ArrayList<>(0);
        }

        return source.parallelStream()
                .filter(d -> d.contains(items))
                .map(d -> new QueryResult(d.getSeed(), d.getScore()))
                .sorted((a, b) -> Integer.compare(b.score(), a.score()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static @NotNull List<ItemPosition> parseSearch(@NotNull List<Query> queries) {
        final List<ItemPosition> items = new ArrayList<>(queries.size());
        StringBuilder missing = null;

        for (Query query : queries) {
            final Item match = ITEM_LOOKUP.get(query.getItem().toLowerCase(Locale.ROOT));
            if (match != null) {
                items.add(new ItemPosition(match, query.getEdition()));
            } else {
                if (missing == null) {
                    missing = new StringBuilder();
                } else {
                    missing.append(',');
                }
                missing.append(query.getItem());
            }
        }

        if (missing != null) {
            throw new IllegalStateException("Failed to parse search, missing: " + missing
                    + ", tokens %s items %s".formatted(queries.size(), items.size()));
        }

        return items;
    }
}
