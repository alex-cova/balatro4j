package com.balatro.cache;

import com.balatro.api.Item;
import com.balatro.api.Joker;
import com.balatro.api.Run;
import com.balatro.enums.*;
import org.jetbrains.annotations.NotNull;


/*
    Chars: 32, Voucher: 32
    UnCommonJoker: 64
    CommonJoker: 60, Blackhole: 1, Perkeo: 1
    LegendaryJoker: 4, Spectral: 16, RareJoker: 20, Tag.values: 24
         */
public class CompressedData {

    private final String seed;
    private final long[] data;

    public CompressedData(String seed, byte score, long[] data) {
        this.seed = seed;
        this.data = data;
    }

    public CompressedData(@NotNull Run run) {
        this.seed = run.seed();
        data = new long[4];

    }

    private static final int common_length = CommonJoker100.values().length;
    private static final int rare_length = RareJoker101C.values().length;
    private static final int spectral_length = Spectral.values().length;
    private static final int tag_length = Tag.values().length - 1;

    private @NotNull Coord getCoord(@NotNull Item item) {
        if (item instanceof Joker joker) {
            if (joker.getType() == JokerType.UNCOMMON) {
                return new Coord(joker.ordinal(), 0);
            }
            if (joker.getType() == JokerType.COMMON) {
                return new Coord(joker.ordinal(), 1);
            }
            if (joker.getType() == JokerType.RARE) {
                return new Coord(joker.ordinal(), 2);
            }
        }

        //CommonJoker100 + ordinal
        if (item instanceof Specials) {
            return new Coord(item.ordinal() + common_length, 1);
        }

        //RareJoker101C, Spectral
        if (item instanceof Spectral) {
            return new Coord(item.ordinal() + rare_length, 2);
        }

        //RareJoker101C + Spectra + Tag
        if (item instanceof Tag) {
            return new Coord(item.ordinal() + rare_length + spectral_length, 2);
        }

        //RareJoker101C + Spectra + Tag - 1 + Legendary
        if (item instanceof LegendaryJoker) {
            return new Coord(item.ordinal() + rare_length + spectral_length + tag_length, 2);
        }

        throw new IllegalStateException("incorrect type: " + item.getClass().getSimpleName());
    }

    private record Coord(int x, int y) {
    }
}
