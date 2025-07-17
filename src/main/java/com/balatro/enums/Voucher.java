package com.balatro.enums;

import com.balatro.api.Filter;
import com.balatro.api.Item;
import com.balatro.api.filter.VoucherFilter;
import com.balatro.jackson.ItemSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ItemSerializer.class)
public enum Voucher implements Item {
    Overstock("Overstock"),
    Overstock_Plus("Overstock Plus"),
    Clearance_Sale("Clearance Sale"),
    Liquidation("Liquidation"),
    Hone("Hone"),
    Glow_Up("Glow Up"),
    Reroll_Surplus("Reroll Surplus"),
    Reroll_Glut("Reroll Glut"),
    Crystal_Ball("Crystal Ball"),
    Omen_Globe("Omen Globe"),
    Telescope("Telescope"),
    Observatory("Observatory"),
    Grabber("Grabber"),
    Nacho_Tong("Nacho Tong"),
    Wasteful("Wasteful"),
    Recyclomancy("Recyclomancy"),
    Tarot_Merchant("Tarot Merchant"),
    Tarot_Tycoon("Tarot Tycoon"),
    Planet_Merchant("Planet Merchant"),
    Planet_Tycoon("Planet Tycoon"),
    Seed_Money("Seed Money"),
    Money_Tree("Money Tree"),
    Blank("Blank"),
    Antimatter("Antimatter"),
    Magic_Trick("Magic Trick"),
    Illusion("Illusion"),
    Hieroglyph("Hieroglyph"),
    Petroglyph("Petroglyph"),
    Directors_Cut("Director's Cut"),
    Retcon("Retcon"),
    Paint_Brush("Paint Brush"),
    Palett("Palette");

    private final String name;

    Voucher(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getYIndex() {
        return 7;
    }

    public Filter isPresent() {
        return new VoucherFilter(this);
    }

    @Override
    public Filter auto(int ante, Edition edition) {
        return new VoucherFilter(this, ante);
    }
}
