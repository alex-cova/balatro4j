package com.balatro.enums;

import com.balatro.api.Item;
import com.balatro.jackson.ItemSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ItemSerializer.class)
public enum PackType implements Item {

    Arcana_Pack("Arcana Pack", 4, 3, 1),
    Jumbo_Arcana_Pack("Jumbo Arcana Pack", 2, 5, 1),
    Mega_Arcana_Pack("Mega Arcana Pack", 0.5, 5, 2),

    Celestial_Pack("Celestial Pack", 4, 3, 1),
    Jumbo_Celestial_Pack("Jumbo Celestial Pack", 2, 5, 1),
    Mega_Celestial_Pack("Mega Celestial Pack", 0.5, 5, 2),

    Standard_Pack("Standard Pack", 4, 3, 1),
    Jumbo_Standard_Pack("Jumbo Standard Pack", 2, 5, 1),
    Mega_Standard_Pack("Mega Standard Pack", 0.5, 5, 2),

    Buffoon_Pack("Buffoon Pack", 1.2, 2, 1),
    Jumbo_Buffoon_Pack("Jumbo Buffoon Pack", 0.6, 4, 1),
    Mega_Buffoon_Pack("Mega Buffoon Pack", 0.15, 4, 2),

    Spectral_Pack("Spectral Pack", 0.6, 2, 1),
    Jumbo_Spectral_Pack("Jumbo Spectral Pack", 0.3, 4, 1),
    Mega_Spectral_Pack("Mega Spectral Pack", 0.07, 4, 2);

    private final String name;
    private final double value;
    private final int choices;
    private final int size;

    PackType(String name, double value, int size, int choices) {
        this.name = name;
        this.value = value;
        this.size = size;
        this.choices = choices;
    }

    public int getSize() {
        return size;
    }

    public int getChoices() {
        return choices;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getYIndex() {
        return 0;
    }

    public double getValue() {
        return value;
    }

    public PackKind getKind() {
        return switch (this) {
            case Arcana_Pack, Jumbo_Arcana_Pack, Mega_Arcana_Pack -> PackKind.Arcana;
            case Celestial_Pack, Jumbo_Celestial_Pack, Mega_Celestial_Pack -> PackKind.Celestial;
            case Standard_Pack, Jumbo_Standard_Pack, Mega_Standard_Pack -> PackKind.Standard;
            case Buffoon_Pack, Jumbo_Buffoon_Pack, Mega_Buffoon_Pack -> PackKind.Buffoon;
            case Spectral_Pack, Jumbo_Spectral_Pack, Mega_Spectral_Pack -> PackKind.Spectral;
        };
    }

    public boolean isLegendaryEnabled() {
        return getKind().isLegendaryEnabled();
    }

    public boolean isMega() {
        return this == Mega_Arcana_Pack || this == Mega_Celestial_Pack || this == Mega_Standard_Pack ||
                this == Mega_Buffoon_Pack || this == Mega_Spectral_Pack;
    }

    public boolean isJumbo() {
        return this == Jumbo_Arcana_Pack || this == Jumbo_Celestial_Pack || this == Jumbo_Standard_Pack ||
                this == Jumbo_Buffoon_Pack || this == Jumbo_Spectral_Pack;
    }

    public boolean isBuffon() {
        return this == Buffoon_Pack || this == Jumbo_Buffoon_Pack || this == Mega_Buffoon_Pack;
    }

    public boolean isSpectral() {
        return this == Spectral_Pack || this == Jumbo_Spectral_Pack || this == Mega_Spectral_Pack;
    }

    public boolean isStandard() {
        return this == Standard_Pack || this == Jumbo_Standard_Pack || this == Mega_Standard_Pack;
    }

    public boolean isArcana() {
        return this == Arcana_Pack || this == Jumbo_Arcana_Pack || this == Mega_Arcana_Pack;
    }

}
