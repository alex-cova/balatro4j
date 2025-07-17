package com.balatro.enums;

import com.balatro.api.Item;

public enum Suit implements Item {
    Spades("Spades"),
    Hearts("Hearts"),
    Clubs("Clubs"),
    Diamonds("Diamonds");

    private final String name;

    public int index() {
        return switch (this) {
            case Hearts -> 0;
            case Clubs -> 1;
            case Diamonds -> 2;
            case Spades -> 3;
        };
    }

    Suit(String name) {
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
