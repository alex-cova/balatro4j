package com.balatro.enums;

import com.balatro.api.Filter;
import com.balatro.api.Item;
import com.balatro.api.filter.SpectralFilter;
import com.balatro.jackson.ItemSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ItemSerializer.class)
public enum Spectral implements Item {

    Familiar("Familiar"),
    Grim("Grim"),
    Incantation("Incantation"),
    Talisman("Talisman"),
    Aura("Aura"),
    Wraith("Wraith"),
    Sigil("Sigil"),
    Ouija("Ouija"),
    Ectoplasm("Ectoplasm"),
    Immolate("Immolate"),
    Ankh("Ankh"),
    Deja_Vu("Deja Vu"),
    Hex("Hex"),
    Trance("Trance"),
    Medium("Medium"),
    Cryptid("Cryptid");

    private final String name;

    Spectral(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getYIndex() {
        return 5;
    }

    @Override
    public Filter auto(int ante, Edition edition) {
        return new SpectralFilter(ante, this, edition);
    }
}
