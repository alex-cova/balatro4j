package tests;

import com.balatro.api.Balatro;
import com.balatro.cache.Data;
import com.balatro.cache.JokerFile;
import com.balatro.enums.PackKind;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.stream.Collectors;

public class Rebuild {

    public static void main(String[] args) {
        var data = JokerFile.readFile(new File("perkeo_70k.jkr"));

        var seeds = data.stream()
                .map(Data::getSeed)
                .collect(Collectors.toSet());

        System.out.println("Processing " + seeds.size() + " seeds");

        data = seeds.parallelStream()
                .map(seed -> Balatro.builder(seed, 8)
                        .enableAll()
                        .disablePack(PackKind.Standard)
                        .analyze())
                .map(Data::new)
                .toList();

        var decimalFormat = new DecimalFormat("#,##0");

        var baos = new ByteArrayOutputStream();

        for (Data datum : data) {
            JokerFile.write(baos, datum);
        }

        System.out.println("File size: " + decimalFormat.format(baos.size()));

        try {
            Files.write(new File("perkeo_70k.jkr").toPath(), baos.toByteArray());
            baos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
