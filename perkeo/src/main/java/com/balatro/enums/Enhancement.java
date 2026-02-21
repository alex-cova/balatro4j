package com.balatro.enums;

import com.balatro.api.Item;

public enum Enhancement implements Item {
    Bonus("Bonus"),
    Mult("Mult"),
    Wild("Wild"),
    Glass("Glass"),
    Steel("Steel"),
    Stone("Stone"),
    Gold("Gold"),
    Luck("Lucky");

    private final String name;

    Enhancement(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getYIndex() {
        return 11;
    }
}
