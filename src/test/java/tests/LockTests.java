package tests;

import com.balatro.Cache2D;
import com.balatro.Coordinate;
import com.balatro.Functions;
import com.balatro.enums.Deck;
import com.balatro.enums.LegendaryJoker;
import com.balatro.enums.Stake;
import com.balatro.enums.Version;
import com.balatro.structs.InstanceParams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.balatro.Functions.*;

public class LockTests {

    @Test
    void testLegendaryLock() {
        var functions = new Functions("IGSPUNF", 1, new InstanceParams(Deck.PLASMA_DECK, Stake.White_Stake, false, Version.v_101f));
        functions.lock(LegendaryJoker.Triboulet);
        var data = functions.nextJoker("sou", joker1SouArr, joker2SouArr, joker3SouArr, joker4SouArr, raritySouArr, editionSouArr, 1, true);
        System.out.println(data.joker.getName());
    }

    @Test
    void testCache2D() {
        var cache = new Cache2D(1);

        var c = new Coordinate("1234", 1, 1);
        cache.put(c, 1.0);

        var result = cache.get(c);

        Assertions.assertEquals(1.0, result);

        var c2 = new Coordinate("1234", 1, 2);

        result = cache.get(c2);

        Assertions.assertEquals(0.0, result);
    }
}
