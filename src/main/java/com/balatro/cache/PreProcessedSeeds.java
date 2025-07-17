package com.balatro.cache;

import com.balatro.SeedFinderImpl;
import com.balatro.api.Balatro;
import com.balatro.api.Item;
import com.balatro.enums.*;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.balatro.enums.LegendaryJoker.*;

public class PreProcessedSeeds {

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
        List<QueryResult> found = new ArrayList<>();

        var editionItems = items.stream()
                .map(i -> {
                    if (i instanceof EditionItem ei) {
                        return new ItemPosition(ei, 8);
                    }

                    return new ItemPosition(i, 8);
                }).toList();

        for (Data data : dataList) {
            if (data.contains(editionItems)) {
                found.add(new QueryResult(data.getSeed(), data.getScore()));
            }
        }

        found.sort((a, b) -> Integer.compare(b.score(), a.score()));

        return found;
    }

    public Set<String> searchByName(@NotNull Set<String> tokens) {
        return find(tokens.stream()
                .map(Query::new)
                .toList())
                .stream()
                .map(QueryResult::seed)
                .collect(Collectors.toSet());
    }

    public List<QueryResult> find(List<Query> tokens) {
        List<QueryResult> found = new ArrayList<>();

        var items = parseSearch(tokens);

        for (Data data : dataList) {
            if (data.contains(items)) {
                found.add(new QueryResult(data.getSeed(), data.getScore()));
            }
        }

        return found;
    }

    public static @NotNull List<ItemPosition> parseSearch(@NotNull List<Query> queries) {
        List<ItemPosition> items = new ArrayList<>(queries.size());

        for (Query query : queries) {
            for (Spectral value : Spectral.values()) {
                if (query.getItem().equalsIgnoreCase(value.getName())) {
                    items.add(new ItemPosition(value, query.getEdition()));
                    break;
                }
            }

            for (CommonJoker value : CommonJoker.values()) {
                if (query.getItem().equalsIgnoreCase(value.getName())) {
                    items.add(new ItemPosition(value, query.getEdition()));
                    break;
                }
            }

            for (RareJoker value : RareJoker.values()) {
                if (query.getItem().equalsIgnoreCase(value.getName())) {
                    items.add(new ItemPosition(value, query.getEdition()));
                    break;
                }
            }

            for (LegendaryJoker value : values()) {
                if (query.getItem().equalsIgnoreCase(value.getName())) {
                    items.add(new ItemPosition(value, query.getEdition()));
                    break;
                }
            }

            for (UnCommonJoker value : UnCommonJoker.values()) {
                if (query.getItem().equalsIgnoreCase(value.getName())) {
                    items.add(new ItemPosition(value, query.getEdition()));
                    break;
                }
            }
            for (Tag value : Tag.values()) {
                if (query.getItem().equalsIgnoreCase(value.getName())) {
                    items.add(new ItemPosition(value, query.getEdition()));
                    break;
                }
            }

            for (Boss value : Boss.values()) {
                if (query.getItem().equalsIgnoreCase(value.getName())) {
                    items.add(new ItemPosition(value, query.getEdition()));
                    break;
                }
            }

            for (Planet value : Planet.values()) {
                if (query.getItem().equalsIgnoreCase(value.getName())) {
                    items.add(new ItemPosition(value, query.getEdition()));
                    break;
                }
            }

            for (Tarot value : Tarot.values()) {
                if (query.getItem().equalsIgnoreCase(value.getName())) {
                    items.add(new ItemPosition(value, query.getEdition()));
                    break;
                }
            }

            for (Voucher value : Voucher.values()) {
                if (query.getItem().equalsIgnoreCase(value.getName())) {
                    items.add(new ItemPosition(value, query.getEdition()));
                    break;
                }
            }
        }

        if (items.size() < queries.size()) {
            var itemNames = items.stream()
                    .map(Item::getName)
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            var missing = queries.stream()
                    .map(Query::getItem)
                    .filter(item -> !itemNames.contains(item.toLowerCase()))
                    .collect(Collectors.joining(","));

            throw new IllegalStateException("Failed to parse search, missing: " + missing + ", tokens %s items %s".formatted(queries.size(), items.size()));
        }

        return items;
    }


}
