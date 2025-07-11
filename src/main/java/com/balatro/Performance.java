package com.balatro;

import com.balatro.api.Balatro;
import com.balatro.api.Run;
import com.balatro.enums.Edition;
import com.balatro.enums.PackKind;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.balatro.enums.LegendaryJoker.*;


public class Performance {
    public static void main(String[] args) {
        if (System.getenv("STARTUP_TIMEOUT") != null) {
            long startTime = Long.parseLong(System.getenv("STARTUP_TIMEOUT"));
            long currentTime = System.currentTimeMillis();

            System.out.println("-------------------------------------------------");
            System.out.println("STARTUP TIME: " + (currentTime - startTime) + " ms " + SeedFinderImpl.getMemory());
            System.out.println("-------------------------------------------------");
        }

        var df = new DecimalFormat("#,##0");

        var seeds = Balatro.search(100_000_000)
                .configuration(config -> config.maxAnte(1)
                        .disablePack(PackKind.Standard)
                        .disablePack(PackKind.Buffoon)
                        .disablePack(PackKind.Celestial)
                )
                .filter(Perkeo.inPack(Edition.Negative).or(Triboulet.inPack(Edition.Negative))
                        .or(Canio.inPack(Edition.Negative)).or(Yorick.inPack(Edition.Negative))
                        .or(Chicot.inPack(Edition.Negative)))
                .find();

        System.out.println("Seeds found: " + df.format(seeds.size()));

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
