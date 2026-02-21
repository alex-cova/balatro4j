package tests;

import com.balatro.Seed32bit;
import com.balatro.api.Balatro;
import com.balatro.cache.CompressedData;
import com.balatro.cache.JokerFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

public class GenerateCanio {
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
