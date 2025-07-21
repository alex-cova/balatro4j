package com.balatro.enums;

import com.balatro.api.Filter;
import com.balatro.api.Item;
import com.balatro.api.Stored;
import com.balatro.jackson.ItemSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ItemSerializer.class)
public enum LegendaryJoker implements Item, Stored {
    Canio("Canio"),
    Triboulet("Triboulet"),
    Yorick("Yorick"),
    Chicot("Chicot"),
    Perkeo("Perkeo");

    private final String name;

    LegendaryJoker(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getYIndex() {
        return 10;
    }

    @Override
    public Filter auto(int ante, Edition edition) {
        return inSpectral(ante, edition).or(inPack(ante, edition));
    }

    @Override
    public int getIndex() {
        return 190 + ordinal();
    }
}
