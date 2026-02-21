package com.balatro.enums;

import com.balatro.api.Item;
import com.balatro.jackson.ItemSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ItemSerializer.class)
public enum Edition implements Item {
    Negative("Negative"),
    Polychrome("Polychrome"),
    Holographic("Holographic"),
    Foil("Foil"),
    NoEdition("No Edition"),
    Eternal("Eternal"),
    Perishable("Perishable"),
    Rental("Rental");

    private final String name;

    Edition(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getEnhancerIndex() {
        if (this == Edition.Foil) {
            return 1;
        } else if (this == Edition.Holographic) {
            return 2;
        } else if (this == Edition.Polychrome) {
            return 3;
        }

        return -1;
    }

    @Override
    public int getYIndex() {
        return -1;
    }

    @JsonIgnore
    public float getMultiplier() {
        return switch (this) {
            case Negative -> 2.0f;
            case Polychrome -> 1.5f;
            case Holographic -> 1.2f;
            case Foil -> 1.1f;
            default -> 0; // No Edition or other cases
        };
    }

}
