package com.balatro.enums;

public enum JokerType {
    LEGENDARY,
    RARE,
    UNCOMMON,
    COMMON;

    public int getRarity() {
        return switch (this) {
            case LEGENDARY -> 4;
            case RARE -> 3;
            case UNCOMMON -> 2;
            case COMMON -> 1;
        };
    }
}
