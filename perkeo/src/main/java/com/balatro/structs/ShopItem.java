package com.balatro.structs;

import com.balatro.api.Item;
import com.balatro.enums.Edition;
import com.balatro.enums.Type;
import org.jetbrains.annotations.Nullable;

public class ShopItem {
    private final Type type;
    private final Item item;
    private final @Nullable JokerData jokerData;

    public ShopItem(Type type, Item item) {
        this.type = type;
        this.item = item;
        this.jokerData = null;
    }

    public ShopItem(Type type, Item item, @Nullable JokerData jokerData) {
        this.type = type;
        this.item = item;
        this.jokerData = jokerData;
    }

    public Type getType() {
        return type;
    }

    public Item getItem() {
        return item;
    }

    public @Nullable JokerData getJokerData() {
        return jokerData;
    }

    public Edition getEdition() {
        if (jokerData == null) return Edition.NoEdition;
        return jokerData.getEdition();
    }
}