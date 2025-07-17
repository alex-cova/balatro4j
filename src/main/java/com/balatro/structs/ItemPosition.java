package com.balatro.structs;

import com.balatro.api.Filter;
import com.balatro.api.Item;
import com.balatro.enums.Edition;
import org.jetbrains.annotations.NotNull;

public record ItemPosition(Item item, int ante, Edition edition) implements Comparable<ItemPosition>, Item {

    public ItemPosition(@NotNull EditionItem item, int ante) {
        this(item.item(), ante, item.edition());
    }

    public ItemPosition(Item item, Edition edition) {
        this(item, 8, edition);
    }

    public ItemPosition(Item item, int ante) {
        this(item, ante, Edition.NoEdition);
    }

    public int encode() {
        int value = 0;

        value |= item.getYIndex() << 24;
        value |= item.ordinal() << 16;
        value |= edition.ordinal() << 8;
        value |= ante;

        return value;
    }

    @Override
    public Filter auto(int ante, Edition edition) {
        return item.auto(ante, edition);
    }

    @Override
    public int getYIndex() {
        return item.getYIndex();
    }

    @Override
    public int ordinal() {
        return item.ordinal();
    }

    public String getName() {
        return item.getName();
    }

    @Override
    public int compareTo(@NotNull ItemPosition o) {
        if (ante == o.ante) {
            return Integer.compare(item.ordinal(), o.item.ordinal());
        }

        return Integer.compare(ante, o.ante);
    }
}
