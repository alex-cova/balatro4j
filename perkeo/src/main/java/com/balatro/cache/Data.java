package com.balatro.cache;

import com.balatro.api.Ante;
import com.balatro.api.Run;
import com.balatro.enums.*;
import com.balatro.structs.EditionItem;
import com.balatro.structs.ItemPosition;
import com.balatro.structs.JokerData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Data {

    private final String seed;
    private int score;
    private final int[] data;

    public Data(String seed, int score, int[] data) {
        this.seed = seed;
        this.score = score;
        // Sort numerically so isOn can binary-search on (yIndex, ordinal).
        // Encoding puts yIndex in the high byte, so ascending int order == (yIndex, ordinal, edition, ante).
        Arrays.sort(data);
        this.data = data;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Data(@NotNull Run run) {
        this.seed = run.seed();
        this.score = (int) run.getScore();

        List<ItemPosition> itemPositions = new ArrayList<>();

        for (Ante ante : run.antes()) {
            for (EditionItem joker : ante.getJokers()) {
                itemPositions.add(new ItemPosition(joker, ante.getAnte()));
            }

            itemPositions.add(new ItemPosition(ante.getVoucher(), ante.getAnte()));
            itemPositions.add(new ItemPosition(ante.getBoss(), ante.getAnte()));

            for (Tag tag : ante.getTags()) {
                itemPositions.add(new ItemPosition(tag, ante.getAnte()));
            }

            for (Tarot tarot : ante.getTarots()) {
                itemPositions.add(new ItemPosition(tarot, ante.getAnte()));
            }

            for (Planet planet : ante.getPlanets()) {
                itemPositions.add(new ItemPosition(planet, ante.getAnte()));
            }

            for (JokerData value : ante.getLegendaryJokers().values()) {
                itemPositions.add(new ItemPosition(value.asEditionItem(), ante.getAnte()));
            }

            for (Spectral spectral : ante.getSpectrals()) {
                itemPositions.add(new ItemPosition(spectral, ante.getAnte()));
            }
        }

        data = new int[itemPositions.size()];

        for (int i = 0; i < itemPositions.size(); i++) {
            data[i] = itemPositions.get(i).encode();
        }

        // Sort numerically for binary-search in isOn — the old ItemPosition.compareTo sort is no longer needed.
        Arrays.sort(data);

        itemPositions.clear();
    }

    public String getSeed() {
        return seed;
    }

    public int getScore() {
        return score;
    }

    public int[] getData() {
        return data;
    }

    public boolean contains(@NotNull List<ItemPosition> items) {
        for (ItemPosition item : items) {
            if (!isOn(item)) {
                return false;
            }
        }

        return true;
    }


    public boolean isOn(@NotNull ItemPosition item) {
        // data[] is sorted ascending. The encoding is yIndex<<24 | ordinal<<16 | edition<<8 | ante.
        // All entries matching (yIndex, ordinal) form a contiguous range we can bracket with two
        // binary searches over the top 16 bits.
        final int prefix = (item.getYIndex() << 24) | (item.ordinal() << 16);
        final int lo = lowerBound(data, prefix);

        if (lo == data.length || (data[lo] & 0xFFFF0000) != prefix) {
            return false;
        }

        // ante == 0 with NoEdition is the "any match is fine" shortcut in the original.
        if (item.edition() == Edition.NoEdition && item.ante() == 0) {
            return true;
        }

        final int hi = lowerBound(data, prefix + 0x10000);
        final int requestedEdition = item.edition().ordinal();
        final int requestedAnte = item.ante();

        for (int i = lo; i < hi; i++) {
            final int value = data[i];
            final int edition = (value >> 8) & 0xFF;
            if (edition != requestedEdition) continue;

            final int ante = value & 0xFF;
            if (ante > requestedAnte) continue;

            return true;
        }
        return false;
    }

    private static int lowerBound(int[] a, int key) {
        int lo = 0, hi = a.length;
        while (lo < hi) {
            final int mid = (lo + hi) >>> 1;
            if (a[mid] < key) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo;
    }
}
