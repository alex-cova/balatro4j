package tests;

import com.balatro.Seed32bit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Seed32Tests implements Seed32bit {

    @Test
    void testGeneration() {
        String x;
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            if (Seed32bit.isSeedable(i)) {
                x = decode(i);
                Assertions.assertEquals(i, encode(x));
            }
        }
    }
}
