package com.balatro.cache;

import com.balatro.Seed32bit;
import com.balatro.api.Ante;
import com.balatro.api.Balatro;
import com.balatro.api.Run;
import com.balatro.api.Stored;
import com.balatro.enums.Specials;
import com.balatro.enums.Spectral;
import com.balatro.enums.Tag;
import com.balatro.structs.EditionItem;
import com.balatro.structs.JokerData;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

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
//        StringBuilder sb = new StringBuilder(8);
//        for (int i = 0; i < 32; i++) {
//            if (((value >>> i) & 1) == 1) {
//                sb.append(BalatroImpl.CHARACTERS_MIN[i]);
//                if (sb.length() == 8) {
//                    break;
//                }
//            }
//        }
//
//        return sb.toString();
    }

    public long[] getData() {
        return memory.getMemory();
    }

    public boolean isOn(@NotNull Stored stored) {
        return memory.getBit(stored.getIndex());
    }


    public static void main(String[] args) {

        var format = new DecimalFormat("#,##0");

        var seeds = 100_000_000;
        Set<Integer> integers = new HashSet<>();

        for (int i = 0; i < seeds; i++) {
            if (Seed32bit.isSeedable(i)) {
                integers.add(i);
            }
        }

        System.out.println("Analyzing " + format.format(integers.size()) + " seeds");

        Seed32bit x = new Seed32bit() {
        };

        var compresseds = integers
                .stream()
                .parallel()
                .filter(a -> a != 0)
                .map(value -> Balatro.builder(x.decode(value), 8)
                        .analyzeAll())
                .map(CompressedData::new)
                .toList();

        System.out.println("Found " + format.format(compresseds.size()) + " seeds");

        var baos = new ByteArrayOutputStream();

        for (CompressedData compressed : compresseds) {
            JokerFile.write(baos, compressed);
        }


        System.out.println("File size: " + format.format(baos.size()));

        try {
            var file = new File("canio.jkr");
            Files.write(file.toPath(), baos.toByteArray());
            baos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
