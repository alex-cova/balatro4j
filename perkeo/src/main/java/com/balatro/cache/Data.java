package com.balatro.cache;

import com.balatro.api.Ante;
import com.balatro.api.Run;
import com.balatro.enums.*;
import com.balatro.structs.EditionItem;
import com.balatro.structs.ItemPosition;
import com.balatro.structs.JokerData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class Data {

    private final String seed;
    private int score;
    private final int[] data;

    public Data(String seed, int score, int[] data) {
        this.seed = seed;
        this.score = score;
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

        itemPositions.sort(ItemPosition::compareTo);

        data = new int[itemPositions.size()];

        for (int i = 0; i < itemPositions.size(); i++) {
            data[i] = itemPositions.get(i).encode();
        }

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
        for (int value : data) {
            int yIndex = (value >> 24) & 0xFF;

            if (yIndex != item.getYIndex()) continue;

            int ordinal = (value >> 16) & 0xFF;

            if (ordinal != item.ordinal()) continue;

            if (item.edition() == Edition.NoEdition && item.ante() == 0) {
                return true;
            }

            int edition = (value >> 8) & 0xFF;

            if (item.edition().ordinal() != edition) continue;

            int ante = (value) & 0xFF;

            if (ante > item.ante()) continue;

            return true;
        }

        return false;
    }


}
