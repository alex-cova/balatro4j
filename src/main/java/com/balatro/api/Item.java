package com.balatro.api;

import com.balatro.enums.Edition;

public interface Item {

    String getName();

    default boolean equals(String value) {
        return getName().equals(value);
    }

    default Filter inPack(int ante) {
        return new InPackFilter(ante, this);
    }

    default Filter inPack() {
        return new InPackFilter(this);
    }

    default Filter inBuffonPack() {
        return new InBuffonPackFilter(this);
    }

    default Filter inBuffonPack(int ante) {
        return new InBuffonPackFilter(ante, this);
    }


    default Filter inShop() {
        return new InShopFilter(this);
    }

    default Filter inShop(int ante) {
        return new InShopFilter(ante, this);
    }

    default Filter inSpectral() {
        return new SpectralFilter(this);
    }

    default Filter inSpectral(int ante) {
        return new SpectralFilter(ante, this);
    }

    default EditionItem edition(Edition edition) {
        return new EditionItem(this, edition);
    }

}
