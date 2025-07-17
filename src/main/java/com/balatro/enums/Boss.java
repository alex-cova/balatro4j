package com.balatro.enums;

import com.balatro.api.Filter;
import com.balatro.api.Item;
import com.balatro.api.filter.BossFilter;
import com.balatro.jackson.ItemSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@JsonSerialize(using = ItemSerializer.class)
public enum Boss implements Item {

    The_Arm("The Arm"),
    The_Club("The Club"),
    The_Eye("The Eye"),
    Amber_Acorn("Amber Acorn"),
    Cerulean_Bell("Cerulean Bell"),
    Crimson_Heart("Crimson Heart"),
    Verdant_Leaf("Verdant Leaf"),
    Violet_Vessel("Violet Vessel"),
    The_Fish("The Fish"),
    The_Flint("The Flint"),
    The_Goad("The Goad"),
    The_Head("The Head"),
    The_Hook("The Hook"),
    The_House("The House"),
    The_Manacle("The Manacle"),
    The_Mark("The Mark"),
    The_Mouth("The Mouth"),
    The_Needle("The Needle"),
    The_Ox("The Ox"),
    The_Pillar("The Pillar"),
    The_Plant("The Plant"),
    The_Psychic("The Psychic"),
    The_Serpent("The Serpent"),
    The_Tooth("The Tooth"),
    The_Wall("The Wall"),
    The_Water("The Water"),
    The_Wheel("The Wheel"),
    The_Window("The Window");

    private final String name;

    Boss(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getYIndex() {
        return 6;
    }

    public char charAt(int index) {
        return name.charAt(index);
    }

    public boolean isT() {
        return !notT();
    }

    public boolean notT() {
        return this == Amber_Acorn ||
               this == Cerulean_Bell ||
               this == Crimson_Heart ||
               this == Verdant_Leaf ||
               this == Violet_Vessel;
    }

    @Contract(" -> new")
    public @NotNull Filter isPresent() {
        return new BossFilter(this, false);
    }

    @Contract(" -> new")
    public @NotNull Filter isNotPresent() {
        return new BossFilter(this, true);
    }

    @Override
    public Filter auto(int ante, Edition edition) {
        return isPresent();
    }
}
