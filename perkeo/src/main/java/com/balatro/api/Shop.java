package com.balatro.api;

import com.balatro.enums.Edition;
import com.balatro.structs.EditionItem;
import com.balatro.structs.ShopItem;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public interface Shop extends Iterable<EditionItem> {

    Shop copy();

    void add(@NotNull ShopItem value);

    EditionItem get(int index);

    Stream<EditionItem> stream();

    int size();

    boolean contains(@NotNull Item item, @NotNull Edition edition);
}
