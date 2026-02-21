package com.balatro.enums;

import com.balatro.api.Filter;
import com.balatro.api.Item;
import com.balatro.jackson.ItemSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ItemSerializer.class)
public enum Tarot implements Item {
    The_Fool("The Fool"),
    The_Magician("The Magician"),
    The_High_Priestess("The High Priestess"),
    The_Empress("The Empress"),
    The_Emperor("The Emperor"),
    The_Hierophant("The Hierophant"),
    The_Lovers("The Lovers"),
    The_Chariot("The Chariot"),
    Justice("Justice"),
    The_Hermit("The Hermit"),
    The_Wheel_of_Fortune("The Wheel of Fortune"),
    Strength("Strength"),
    The_Hanged_Man("The Hanged Man"),
    Death("Death"),
    Temperance("Temperance"),
    The_Devil("The Devil"),
    The_Tower("The Tower"),
    The_Star("The Star"),
    The_Moon("The Moon"),
    The_Sun("The Sun"),
    Judgement("Judgement"),
    The_World("The World");

    private final String name;

    Tarot(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getYIndex() {
        return 4;
    }

    @Override
    public Filter auto(int ante, Edition edition) {
        return inPack(ante, edition).or(inShop(ante, edition));
    }
}
