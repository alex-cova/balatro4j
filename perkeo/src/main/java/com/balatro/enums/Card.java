package com.balatro.enums;

import com.balatro.api.Item;

public enum Card implements Item {
    C_2("C_2"),
    C_3("C_3"),
    C_4("C_4"),
    C_5("C_5"),
    C_6("C_6"),
    C_7("C_7"),
    C_8("C_8"),
    C_9("C_9"),
    C_A("C_A"),
    C_J("C_J"),
    C_K("C_K"),
    C_Q("C_Q"),
    C_T("C_T"),
    D_2("D_2"),
    D_3("D_3"),
    D_4("D_4"),
    D_5("D_5"),
    D_6("D_6"),
    D_7("D_7"),
    D_8("D_8"),
    D_9("D_9"),
    D_A("D_A"),
    D_J("D_J"),
    D_K("D_K"),
    D_Q("D_Q"),
    D_T("D_T"),
    H_2("H_2"),
    H_3("H_3"),
    H_4("H_4"),
    H_5("H_5"),
    H_6("H_6"),
    H_7("H_7"),
    H_8("H_8"),
    H_9("H_9"),
    H_A("H_A"),
    H_J("H_J"),
    H_K("H_K"),
    H_Q("H_Q"),
    H_T("H_T"),
    S_2("S_2"),
    S_3("S_3"),
    S_4("S_4"),
    S_5("S_5"),
    S_6("S_6"),
    S_7("S_7"),
    S_8("S_8"),
    S_9("S_9"),
    S_A("S_A"),
    S_J("S_J"),
    S_K("S_K"),
    S_Q("S_Q"),
    S_T("S_T");

    private final String name;

    Card(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getYIndex() {
        return 12;
    }

    public Rank getRank(){
        return switch (this) {
            case C_2, D_2, H_2, S_2 -> Rank.R_2;
            case C_3, D_3, H_3, S_3 -> Rank.R_3;
            case C_4, D_4, H_4, S_4 -> Rank.R_4;
            case C_5, D_5, H_5, S_5 -> Rank.R_5;
            case C_6, D_6, H_6, S_6 -> Rank.R_6;
            case C_7, D_7, H_7, S_7 -> Rank.R_7;
            case C_8, D_8, H_8, S_8 -> Rank.R_8;
            case C_9, D_9, H_9, S_9 -> Rank.R_9;
            case C_A, D_A, H_A, S_A -> Rank.R_Ace;
            case C_J, D_J, H_J, S_J -> Rank.R_Jack;
            case C_Q, D_Q, H_Q, S_Q -> Rank.R_Queen;
            case C_K, D_K, H_K, S_K -> Rank.R_King;
            case C_T, D_T, H_T, S_T -> Rank.R_10;
        };
    }

    public Suit getSuit() {
        return switch (this) {
            case C_2, C_3, C_4, C_5, C_6, C_7, C_8, C_9, C_A, C_J, C_K, C_Q, C_T -> Suit.Clubs;
            case D_2, D_3, D_4, D_5, D_6, D_7, D_8, D_9, D_A, D_J, D_K, D_Q, D_T -> Suit.Diamonds;
            case H_2, H_3, H_4, H_5, H_6, H_7, H_8, H_9, H_A, H_J, H_K, H_Q, H_T -> Suit.Hearts;
            case S_2, S_3, S_4, S_5, S_6, S_7, S_8, S_9, S_A, S_J, S_K, S_Q, S_T -> Suit.Spades;
        };
    }

}
