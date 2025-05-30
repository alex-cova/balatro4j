package com.balatro;

import com.balatro.api.Balatro;
import com.balatro.api.Run;
import com.balatro.enums.Edition;
import com.balatro.enums.PackKind;
import com.balatro.enums.RareJoker;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.balatro.enums.LegendaryJoker.Perkeo;
import static com.balatro.enums.LegendaryJoker.Triboulet;

public class Main {
    public static void main(String[] args) {
        if (System.getenv("STARTUP_TIMEOUT") != null) {
            long startTime = Long.parseLong(System.getenv("STARTUP_TIMEOUT"));
            long currentTime = System.currentTimeMillis();

            System.out.println("-------------------------------------------------");
            System.out.println("STARTUP TIME: " + (currentTime - startTime) + " ms " + SeedFinderImpl.getMemory());
            System.out.println("-------------------------------------------------");
        }

        var seeds = Balatro.search(10, 10_000_000)
                .configuration(config -> config.maxAnte(1)
                        .disablePack(PackKind.Standard)
                        .disablePack(PackKind.Celestial)
                )
                .filter(Perkeo.inPack(Edition.Negative).or(Triboulet.inPack(Edition.Negative))
                        .and(RareJoker.Blueprint.inPack().or(RareJoker.Blueprint.inShop()))
                        .and(RareJoker.Invisible_Joker.inPack().or(RareJoker.Invisible_Joker.inShop())))
                .find();

        System.out.println("Seeds found: " + seeds.size());

        var decimalFormat = new DecimalFormat("0.0");

        List<Run> runs = new ArrayList<>();

        for (String seed : seeds) {
            var play = Balatro.builder(seed, 8)
                    .analyzeAll();

            runs.add(play);
        }

        runs.sort((o1, o2) -> Double.compare(o2.getScore(), o1.getScore()));

        for (Run run : runs) {
            System.out.println(run.seed() + " " + decimalFormat.format(run.getScore()));
        }
    }
}
