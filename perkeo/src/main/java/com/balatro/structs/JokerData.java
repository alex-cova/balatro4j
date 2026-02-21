package com.balatro.structs;

import com.balatro.Coordinate;
import com.balatro.api.Filter;
import com.balatro.api.Item;
import com.balatro.enums.Edition;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class JokerData implements Item {

    public Item joker;
    public int rarity;
    public Edition edition;
    public JokerStickers stickers;

    private Coordinate coordinate;
    private Item[] items;

    public JokerData(Item joker, int rarity, Edition edition, JokerStickers stickers) {
        this.joker = joker;
        this.rarity = rarity;
        this.edition = edition;
        this.stickers = stickers;
    }

    public JokerData setResampleInfo(Coordinate coordinate, Item[] items) {
        this.coordinate = coordinate;
        this.items = items;
        return this;
    }

    @JsonIgnore
    @Override
    public Filter auto(int ante, Edition edition) {
        return joker.auto(ante, edition);
    }

    @JsonIgnore
    public Coordinate getCoordinate() {
        return coordinate;
    }

    @JsonIgnore
    public Item[] getItems() {
        return items;
    }

    public Item getJoker() {
        return joker;
    }

    public Edition getEdition() {
        if (edition != Edition.NoEdition) {
            return edition;
        }

        if (getStickers() == null) {
            return edition;
        }

        if (getStickers().isRental()) {
            return Edition.Rental;
        }

        if (getStickers().isPerishable()) {
            return Edition.Perishable;
        }

        if (getStickers().isEternal()) {
            return Edition.Eternal;
        }

        return edition;
    }

    public JokerStickers getStickers() {
        return stickers;
    }

    @Override
    public int getYIndex() {
        return joker.getYIndex();
    }

    @Override
    public int ordinal() {
        return joker.ordinal();
    }

    @Override
    public String getName() {
        return joker.getName();
    }
}