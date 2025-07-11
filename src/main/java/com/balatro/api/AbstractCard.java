package com.balatro.api;

import com.balatro.enums.Edition;
import com.balatro.jackson.ItemSerializer;
import com.balatro.structs.Card;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ItemSerializer.class)
public record AbstractCard(String getName, Card card) implements Item {

    @Override
    public int getYIndex() {
        return -1;
    }

    @Override
    public int ordinal() {
        return 0;
    }

    @Override
    public Filter auto(int ante, Edition edition) {
        return null;
    }
}
