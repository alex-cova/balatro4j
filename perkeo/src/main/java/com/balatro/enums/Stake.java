package com.balatro.enums;

import com.balatro.api.Item;

public enum Stake implements Item {
    White_Stake("White Stake"),
    Red_Stake("Red Stake"),
    Green_Stake("Green Stake"),
    Black_Stake("Black Stake"),
    Blue_Stake("Blue Stake"),
    Purple_Stake("Purple Stake"),
    Orange_Stake("Orange Stake"),
    Gold_Stake("Gold Stake");

    private final String name;

    Stake(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getYIndex() {
        return -1;
    }


}
