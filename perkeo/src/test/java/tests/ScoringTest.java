package tests;

import com.balatro.api.Balatro;
import org.junit.jupiter.api.Test;

public class ScoringTest {

    @Test
    public void testScoring() {
        var score = Balatro.builder("IGSPUNF", 8)
                .analyzeAll()
                .getScore();

        System.out.println("IGSPUNF Score: " + score);

        score = Balatro.builder("AP98OW5", 8)
                .analyzeAll()
                .getScore();

        System.out.println("AP98OW5 Score: " + score);
    }
}
