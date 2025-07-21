package com.balatro.api;

/*
    0   | Chars: 32,
    32  | Voucher: 32
    64  | UnCommonJoker: 64
    128 | CommonJoker: 60
    188 | Special: 2
    190 | LegendaryJoker: 5
    195 | Spectral: 16
    211 | RareJoker: 20
    231 | Tag.values: 24
         */
public interface Stored {

    int getIndex();

}
