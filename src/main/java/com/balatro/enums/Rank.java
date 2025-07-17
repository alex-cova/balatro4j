package com.balatro.enums;

import com.balatro.api.Item;
import com.balatro.jackson.ItemSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ItemSerializer.class)
public enum Rank implements Item {
    R_2("2"),
    R_3("3"),
    R_4("4"),
    R_5("5"),
    R_6("6"),
    R_7("7"),
    R_8("8"),
    R_9("9"),
    R_10("10"),
    R_Jack("Jack"),
    R_Queen("Queen"),
    R_King("King"),
    R_Ace("Ace");

    public int index() {
        return switch (this) {
            case R_2 -> 0;
            case R_3 -> 1;
            case R_4 -> 2;
            case R_5 -> 3;
            case R_6 -> 4;
            case R_7 -> 5;
            case R_8 -> 6;
            case R_9 -> 7;
            case R_10 -> 8;
            case R_Jack -> 9;
            case R_Queen -> 10;
            case R_King -> 11;
            case R_Ace -> 12;
        };
    }

    private final String name;

    Rank(String name) {
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
