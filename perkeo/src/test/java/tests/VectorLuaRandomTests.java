package tests;

import com.balatro.LuaRandom;
import com.balatro.vector.VectorLuaRandom;
import jdk.incubator.vector.DoubleVector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class VectorLuaRandomTests {

    @Test
    void vectorRandomMatchesScalarRandom() {
        double[] seeds = new double[VectorLuaRandom.laneCount()];
        for (int i = 0; i < seeds.length; i++) {
            seeds[i] = i / 10.0;
        }

        DoubleVector vectorSeeds = DoubleVector.fromArray(VectorLuaRandom.DOUBLE_SPECIES, seeds, 0);
        double[] actual = new double[seeds.length];
        VectorLuaRandom.random(vectorSeeds).intoArray(actual, 0);

        for (int i = 0; i < seeds.length; i++) {
            Assertions.assertEquals(LuaRandom.random(seeds[i]), actual[i]);
        }
    }
}
