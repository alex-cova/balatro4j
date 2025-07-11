package com.balatro.structs;

import com.balatro.api.Item;
import com.balatro.api.Shop;
import com.balatro.enums.Edition;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ShopQueue implements Shop {

    public static final ShopQueue EMPTY = new ShopQueue(Collections.emptyMap(), Collections.emptyList());

    @JsonIgnore
    private final Map<String, Edition> map;
    private final List<EditionItem> queue;

    public ShopQueue() {
        this.map = new HashMap<>(20);
        this.queue = new ArrayList<>(20);
    }

    public ShopQueue(Map<String, Edition> map, List<EditionItem> queue) {
        this.map = map;
        this.queue = queue;
    }

    @Override
    public @NotNull Iterator<EditionItem> iterator() {
        return queue.iterator();
    }

    @Contract(" -> new")
    @Override
    public @NotNull Shop copy() {
        return new ShopQueue(Collections.unmodifiableMap(this.map), Collections.unmodifiableList(this.queue));
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public void add(@NotNull ShopItem value, Edition edition) {
        map.put(value.getItem().getName(), edition);
        queue.add(new EditionItem(value.getItem(), edition));
    }

    @Override
    public EditionItem get(int index) {
        return queue.get(index);
    }

    @Override
    public Stream<EditionItem> stream() {
        return queue.stream();
    }

    @Override
    public boolean contains(@NotNull Item item, @NotNull Edition edition) {
        var i = map.get(item.getName());

        if (i == null) {
            return false;
        }

        return i == edition;
    }
}
