package com.balatro.cache;

import com.balatro.Seed32bit;
import com.balatro.api.Ante;
import com.balatro.api.Run;
import com.balatro.api.Stored;
import com.balatro.enums.Specials;
import com.balatro.enums.Spectral;
import com.balatro.enums.Tag;
import com.balatro.structs.EditionItem;
import com.balatro.structs.JokerData;
import org.jetbrains.annotations.NotNull;

public class CompressedData implements Seed32bit {

    private final Memory256Long memory;

    public CompressedData(long[] data) {
        this.memory = new Memory256Long(data);
    }

    public CompressedData(@NotNull Run run) {
        this.memory = new Memory256Long(encode(run.seed()));

        for (Ante ante : run.antes()) {
            for (EditionItem joker : ante.getJokers()) {
                if (joker.item() instanceof Stored s) {
                    memory.setBit(s.getIndex());
                }
            }

            for (Tag tag : ante.getTags()) {
                memory.setBit(tag.getIndex());
            }

            if (ante.hasInPack(Specials.BLACKHOLE)) {
                memory.setBit(Specials.BLACKHOLE.getIndex());
            }

            memory.setBit(ante.getVoucher().getIndex());

            for (Tag tag : ante.getTags()) {
                memory.setBit(tag.getIndex());
            }

            for (JokerData value : ante.getLegendaryJokers().values()) {
                if (value.getJoker() instanceof Stored s) {
                    memory.setBit(s.getIndex());
                }
            }

            for (Spectral spectral : ante.getSpectrals()) {
                memory.setBit(spectral.getIndex());
            }
        }
    }

    public String getSeed() {
        long value = memory.getMemory()[0];
        return decode((int) value);
    }

    public long[] getData() {
        return memory.getMemory();
    }

    public boolean isOn(@NotNull Stored stored) {
        return memory.getBit(stored.getIndex());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CompressedData data) {
            return memory.equals(data.memory);
        }
        return false;
    }

    public String getChecksum() {
        return Long.toHexString(memory.generateChecksum());
    }
}
