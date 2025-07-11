package com.balatro.api.filter;

import com.balatro.api.*;
import com.balatro.enums.*;

public record InPackFilter(int ante, Item item, Edition edition) implements Filter {

    public InPackFilter(Item item) {
        this(-1, item, Edition.NoEdition);
    }

    @Override
    public boolean filter(Run run) {
        if (ante == -1) return run.hasInPack(item, edition);
        return run.hasInPack(ante, item, edition);
    }

    @Override
    public void configure(Balatro balatro) {
        if (item instanceof Spectral) {
            balatro.enableSpectralPack();
        }

        if (item instanceof Joker) {
            balatro.enableJokerPack();
        }

        if (item instanceof LegendaryJoker || item == Specials.THE_SOUL) {
            balatro.enableSpectralPack();
            balatro.enableArcanaPack();
        }

        if (item instanceof Planet || item == Specials.BLACKHOLE) {
            balatro.enableCelestialPack();
        }

        if (item instanceof Tarot) {
            balatro.enableArcanaPack();
        }
    }
}
