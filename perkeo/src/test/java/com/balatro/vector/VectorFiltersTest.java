package com.balatro.vector;

import com.balatro.api.Balatro;
import com.balatro.api.Filter;
import com.balatro.enums.Edition;
import com.balatro.enums.PackKind;
import jdk.incubator.vector.DoubleVector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.balatro.enums.LegendaryJoker.*;

final class VectorFiltersTest {

    @Test
    void negativeLegendaryAnteOnePrefilterMatchesScalarForKnownSeeds() {
        String[] seeds = {
                "NALDC19B",
                "12345678",
                "HVC549OP",
                "FV5S4RBO",
                "33JCJSA",
                "FHSRBAMA",
                "ABCDEFGH",
                "ZZZZZZZZ"
        };

        VectorSearchContext context = context(seeds);
        VectorSeedFilter vectorFilter = VectorFilters.negativeLegendaryInAnteOnePacks();
        VectorMaskBits actual = vectorFilter.filter(context);
        Filter scalarFilter = scalarPerformanceFilter();

        for (int lane = 0; lane < seeds.length && lane < context.laneCount(); lane++) {
            boolean expected = scalarMatches(seeds[lane], scalarFilter);
            Assertions.assertEquals(expected, actual.laneIsSet(lane), seeds[lane]);
            Assertions.assertEquals(expected, ((ScalarSeedPrefilter) vectorFilter).filterScalar(seeds[lane].getBytes()),
                    "scalar prefilter " + seeds[lane]);
        }
    }

    @Test
    void scalarBatchPrefilterMatchesScalarPrefilter() {
        byte[] seedDigits = "123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes();
        byte[] suffixSeed = "NALDC19B".getBytes();
        ScalarSeedPrefilter filter = (ScalarSeedPrefilter) VectorFilters.negativeLegendaryInAnteOnePacks();
        long batch = filter.filterScalarBatch(suffixSeed, 0, seedDigits.length, seedDigits);

        byte[] seed = suffixSeed.clone();
        for (int lane = 0; lane < seedDigits.length; lane++) {
            seed[0] = seedDigits[lane];
            Assertions.assertEquals(filter.filterScalar(seed), (batch & (1L << lane)) != 0L,
                    "batch prefilter " + new String(seed));
        }
    }

    private static VectorSearchContext context(String[] seeds) {
        int lanes = VectorSearchContext.DOUBLE_SPECIES.length();
        double[][] chars = new double[VectorSearchContext.MAX_SEED_LENGTH][lanes];
        boolean[] valid = new boolean[lanes];

        for (int lane = 0; lane < lanes; lane++) {
            valid[lane] = lane < seeds.length;
            String seed = valid[lane] ? seeds[lane] : "00000000";
            for (int i = 0; i < VectorSearchContext.MAX_SEED_LENGTH; i++) {
                chars[i][lane] = seed.charAt(i);
            }
        }

        DoubleVector[] seedVectors = new DoubleVector[VectorSearchContext.MAX_SEED_LENGTH];
        for (int i = 0; i < seedVectors.length; i++) {
            seedVectors[i] = DoubleVector.fromArray(VectorSearchContext.DOUBLE_SPECIES, chars[i], 0);
        }
        return new VectorSearchContext(seedVectors, valid, VectorSearchContext.MAX_SEED_LENGTH, java.util.Map.of());
    }

    private static boolean scalarMatches(String seed, Filter filter) {
        return filter.filter(Balatro.builder(seed, 1)
                .maxAnte(1)
                .disablePack(PackKind.Standard)
                .disablePack(PackKind.Buffoon)
                .disablePack(PackKind.Celestial)
                .analyze());
    }

    private static Filter scalarPerformanceFilter() {
        return Perkeo.inPack(Edition.Negative)
                .or(Triboulet.inPack(Edition.Negative))
                .or(Canio.inPack(Edition.Negative))
                .or(Yorick.inPack(Edition.Negative))
                .or(Chicot.inPack(Edition.Negative));
    }
}
