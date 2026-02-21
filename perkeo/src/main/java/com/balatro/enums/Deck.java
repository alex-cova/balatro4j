package com.balatro.enums;

public enum Deck {
    RED_DECK("Red Deck"),
    BLUE_DECK("Blue Deck"),
    YELLOW_DECK("Yellow Deck"),
    GREEN_DECK("Green Deck"),
    BLACK_DECK("Black Deck"),
    MAGIC_DECK("Magic Deck"),
    NEBULA_DECK("Nebula Deck"),
    GHOST_DECK("Ghost Deck"),
    ABANDONED_DECK("Abandoned Deck"),
    CHECKERED_DECK("Checkered Deck"),
    ZODIAC_DECK("Zodiac Deck"),
    PAINTED_DECK("Painted Deck"),
    ANAGLYPH_DECK("Anaglyph Deck"),
    PLASMA_DECK("Plasma Deck"),
    ERRATIC_DECK("Erratic Deck");

    private final String name;

    Deck(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}