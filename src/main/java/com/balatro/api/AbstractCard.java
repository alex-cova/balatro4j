package com.balatro.api;

import com.balatro.jackson.ItemSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ItemSerializer.class)
public record AbstractCard(String getName) implements Item {

    @Override
    public int getYIndex() {
        return -1;
    }

    @Override
    public int ordinal() {
        return 0;
    }
}
