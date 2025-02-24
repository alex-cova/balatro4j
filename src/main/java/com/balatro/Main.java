package com.balatro;

import com.balatro.api.Balatro;
import com.balatro.api.Run;
import com.balatro.enums.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.balatro.enums.LegendaryJoker.*;

public class Main {
    public static void main(String[] args) {
        if (System.getenv("STARTUP_TIMEOUT") != null) {
            long startTime = Long.parseLong(System.getenv("STARTUP_TIMEOUT"));
            long currentTime = System.currentTimeMillis();

            System.out.println("-------------------------------------------------");
            System.out.println("STARTUP TIME: " + (currentTime - startTime) + " ms " + SeedFinderImpl.getMemory());
            System.out.println("-------------------------------------------------");
        }

        var seeds = Balatro.search(10, 100_000)
                .configuration(config -> config.maxAnte(1).disableShopQueue()
                        .disablePack(PackKind.Buffoon))
                .filter(Perkeo.inPack().or(Triboulet.inPack()).or(Yorick.inPack()).or(Chicot.inPack()).or(Canio.inPack()))
                .find();

        System.out.println("Seeds found: " + seeds.size());

        var decimalFormat = new DecimalFormat("0.0");

        List<Run> runs = new ArrayList<>();

        for (String seed : seeds) {
            var play = Balatro.builder(seed, 8)
                    .analyzeAll()
                    .analyze();

            runs.add(play);
        }

        runs.sort((o1, o2) -> Double.compare(o2.getScore(), o1.getScore()));

        for (Run run : runs) {
            System.out.println(run.seed() + " " + decimalFormat.format(run.getScore()));
        }
    }
}
