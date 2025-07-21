package com.balatro.enums;

import com.balatro.api.Filter;
import com.balatro.api.Item;
import com.balatro.api.Stored;
import com.balatro.api.filter.TagFilter;
import com.balatro.jackson.ItemSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ItemSerializer.class)
public enum Tag implements Item, Stored {
    Uncommon_Tag("Uncommon Tag"),
    Rare_Tag("Rare Tag"),
    Negative_Tag("Negative Tag"),
    Foil_Tag("Foil Tag"),
    Holographic_Tag("Holographic Tag"),
    Polychrome_Tag("Polychrome Tag"),
    Investment_Tag("Investment Tag"),
    Voucher_Tag("Voucher Tag"),
    Boss_Tag("Boss Tag"),
    Standard_Tag("Standard Tag"),
    Charm_Tag("Charm Tag"),
    Meteor_Tag("Meteor Tag"),
    Buffoon_Tag("Buffoon Tag"),
    Handy_Tag("Handy Tag"),
    Garbage_Tag("Garbage Tag"),
    Ethereal_Tag("Ethereal Tag"),
    Coupon_Tag("Coupon Tag"),
    Double_Tag("Double Tag"),
    Juggle_Tag("Juggle Tag"),
    D6_Tag("D6 Tag"),
    Top_up_Tag("Top-up Tag"),
    Speed_Tag("Speed Tag"),
    Orbital_Tag("Orbital Tag"),
    Economy_Tag("Economy Tag");

    private final String name;

    Tag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getYIndex() {
        return 8;
    }

    @Override
    public Filter auto(int ante, Edition edition) {
        return new TagFilter(this, ante);
    }


    @Override
    public int getIndex() {
        return 231 + ordinal();
    }
}
