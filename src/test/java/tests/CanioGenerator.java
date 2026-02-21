package tests;

import com.balatro.Seed32bit;
import com.balatro.api.Balatro;
import com.balatro.cache.CompressedData;
import com.balatro.cache.JokerFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

public class CanioGenerator implements Seed32bit {

    public static void main(String[] args) {
        new CanioGenerator().generate();
    }

    public void generate() {
        var baos = new ByteArrayOutputStream();
        int count = 0;
        Set<Integer> integers = new HashSet<>();

        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            if (Seed32bit.isSeedable(i)) {
                integers.add(i);
            }
        }


        integers.parallelStream()
                .map(i -> {
                    var run = Balatro.builder(decode(i), 8)
                            .analyzeAll();
                    return new CompressedData(run);
                }).forEach(e -> JokerFile.write(baos, e));

        System.out.println("Generated seeds: " + count);

        try {
            var file = new File("canio.jkr");
            Files.write(file.toPath(), baos.toByteArray());
            baos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //6216949
        //6216564
        //6216048

    }
}
