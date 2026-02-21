package com.balatro.structs;

import com.balatro.api.Filter;
import com.balatro.api.Item;
import com.balatro.api.Joker;
import com.balatro.enums.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EditionItem(Item item, @NotNull Edition edition) implements Item {

    public EditionItem(@NotNull Item item) {
        this(item, Edition.NoEdition);
    }

    public EditionItem {
        if (item instanceof EditionItem) {
            throw new RuntimeException("EditionItem cannot be created from another EditionItem");
        }

        Objects.requireNonNull(item);
        Objects.requireNonNull(edition);
    }

    @JsonIgnore
    @Override
    public Filter auto(int ante, Edition edition) {
        return item.auto(ante, edition);
    }

    @JsonIgnore
    public boolean isJoker() {
        return item instanceof Joker;
    }

    @JsonIgnore
    public boolean isTarot() {
        return item instanceof Tarot;
    }

    @JsonIgnore
    public boolean isSpectral() {
        return item instanceof Spectral;
    }

    public boolean is(Item item) {
        return this.item.equals(item);
    }

    @JsonIgnore
    public boolean isLegendary() {
        return item instanceof LegendaryJoker;
    }

    public @Nullable JokerType getJokerType() {
        if (item instanceof Joker joker) {
            return joker.getType();
        }

        return null;
    }

    public PackKind getKind() {
        if (item instanceof Joker joker) {
            return PackKind.Buffoon;
        }

        if (item instanceof Tarot) {
            return PackKind.Arcana;
        }

        if (item instanceof Spectral) {
            return PackKind.Spectral;
        }

        if (item instanceof Planet) {
            return PackKind.Celestial;
        }

        if (item instanceof LegendaryJoker) {
            return PackKind.Legendary;
        }

        if (item instanceof Specials) {
            return PackKind.Legendary;
        }

        return PackKind.Standard;
    }

    public boolean hasSticker() {
        return edition != Edition.NoEdition;
    }

    public boolean hasEdition(Edition edition) {
        return this.edition != Edition.NoEdition && this.edition.eq(edition);
    }

    public boolean equals(Item item) {
        if (item instanceof EditionItem editionItem) {
            return editionItem.eq(this.item) && edition.eq(editionItem.edition());
        }

        return item.eq(this.item);
    }

    @Contract(value = " -> new", pure = true)
    public @NotNull JokerData jokerData() {
        int rarity = 0;

        if (item instanceof Joker joker) {
            rarity = joker.getType().getRarity();
        }

        return new JokerData(item, rarity, edition, null);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;

        EditionItem that = (EditionItem) object;
        return item.equals(that.item);
    }

    @Override
    public int hashCode() {
        return item.hashCode();
    }

    @Override
    public int ordinal() {
        return item.ordinal();
    }

    @JsonIgnore
    @Override
    public String getName() {
        return item.getName();
    }

    @Override
    public int getYIndex() {
        return item.getYIndex();
    }

    @Override
    public @NotNull String toString() {
        return item.getName();
    }
}