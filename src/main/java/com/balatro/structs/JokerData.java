package com.balatro.structs;

import com.balatro.api.Item;
import com.balatro.enums.CommonJoker;
import com.balatro.enums.Edition;

public class JokerData {
    public Item joker;
    public int rarity;
    public Edition edition;
    public JokerStickers stickers;

    public JokerData(Item joker, int rarity, Edition edition, JokerStickers stickers) {
        this.joker = joker;
        this.rarity = rarity;
        this.edition = edition;
        this.stickers = stickers;
    }

    public Item getJoker() {
        return joker;
    }

    public Edition getEdition() {
        return edition;
    }

    public JokerStickers getStickers() {
        return stickers;
    }

}