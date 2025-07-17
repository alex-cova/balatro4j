package com.balatro.enums;

import com.balatro.api.Filter;
import com.balatro.api.Item;
import com.balatro.jackson.ItemSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ItemSerializer.class)
public enum Specials implements Item {
    BLACKHOLE("Black Hole"),
    THE_SOUL("The Soul");

    private final String name;

    Specials(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getYIndex() {
        return 9;
    }

    @Override
    public Filter auto(int ante, Edition edition) {
        return inPack(ante);
    }
}
