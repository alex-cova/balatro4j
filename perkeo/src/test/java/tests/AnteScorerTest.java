package tests;

import com.balatro.api.Ante;
import com.balatro.api.AnteScorer;
import com.balatro.api.Balatro;
import com.balatro.api.Run;
import com.balatro.enums.*;
import com.balatro.impl.AnteScorerImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AnteScorerTest {

    @Test
    void testNullSafety() {
        AnteScorerImpl scorer = AnteScorerImpl.INSTANCE;
        Assertions.assertEquals(0.0f, scorer.calculate((Run) null));
        Assertions.assertEquals(0.0f, scorer.calculate((Ante) null));
        Assertions.assertEquals(0.0f, scorer.calculate(null, true));
        Assertions.assertEquals(0.0f, scorer.scoreSpectral(null));
        Assertions.assertEquals(0.0f, scorer.scoreTarot(null));
        Assertions.assertEquals(0.0f, scorer.scoreVoucher(null));
        Assertions.assertEquals(0.0f, scorer.scoreTag(null));
        Assertions.assertEquals(0.0f, scorer.scoreBoss(null));
    }

    @Test
    void testSingletonAndDefaultConsistency() {
        AnteScorerImpl scorer = new AnteScorerImpl();
        Assertions.assertNotNull(AnteScorer.getDefault());
        Assertions.assertSame(AnteScorerImpl.INSTANCE, AnteScorer.getDefault());

        Run run = Balatro.builder("IGSPUNF", 1).analyzeAll();
        Assertions.assertEquals(scorer.calculate(run), AnteScorer.getDefault().calculate(run));
        Assertions.assertEquals(run.getScore(), AnteScorer.getDefault().calculate(run));
    }

    @Test
    void testAnteLevelScoring() {
        Run run = Balatro.builder("IGSPUNF", 2).analyzeAll();
        Ante ante1 = run.getFirstAnte();
        Ante ante2 = run.getSecondAnte();

        float ante1Score = AnteScorer.getDefault().calculate(ante1);
        float ante2Score = AnteScorer.getDefault().calculate(ante2);

        Assertions.assertTrue(ante1Score > 0);
        Assertions.assertTrue(ante2Score > 0);
    }

    @Test
    void testHighValueConsumablesAndVouchers() {
        AnteScorerImpl scorer = AnteScorerImpl.INSTANCE;

        // Vouchers
        Assertions.assertEquals(5.0f, scorer.scoreVoucher(Voucher.Antimatter));
        Assertions.assertEquals(2.0f, scorer.scoreVoucher(Voucher.Observatory));
        Assertions.assertEquals(1.0f, scorer.scoreVoucher(Voucher.Blank));
        Assertions.assertEquals(1.0f, scorer.scoreVoucher(Voucher.Directors_Cut));

        // Spectrals
        Assertions.assertEquals(4.0f, scorer.scoreSpectral(Spectral.Deja_Vu));
        Assertions.assertEquals(4.0f, scorer.scoreSpectral(Spectral.Ectoplasm));
        Assertions.assertEquals(3.0f, scorer.scoreSpectral(Spectral.Immolate));
        Assertions.assertEquals(3.0f, scorer.scoreSpectral(Spectral.Ankh));
        Assertions.assertEquals(2.0f, scorer.scoreSpectral(Spectral.Cryptid));

        // Tarots
        Assertions.assertEquals(2.5f, scorer.scoreTarot(Tarot.Death));
        Assertions.assertEquals(2.5f, scorer.scoreTarot(Tarot.The_Hanged_Man));
        Assertions.assertEquals(2.0f, scorer.scoreTarot(Tarot.Temperance));
        Assertions.assertEquals(1.5f, scorer.scoreTarot(Tarot.The_Hermit));

        // Tags
        Assertions.assertEquals(5.0f, scorer.scoreTag(Tag.Negative_Tag));
        Assertions.assertEquals(2.5f, scorer.scoreTag(Tag.Double_Tag));
        Assertions.assertEquals(2.0f, scorer.scoreTag(Tag.Polychrome_Tag));

        // Boss hazards
        Assertions.assertEquals(-1.0f, scorer.scoreBoss(Boss.The_Plant));
        Assertions.assertEquals(-0.5f, scorer.scoreBoss(Boss.The_Arm));
    }

    @Test
    void testSeedWithMultipleLegendariesAccumulatesProperly() {
        // JHZ7FPM ante 1 has both Perkeo and Triboulet
        Run run = Balatro.builder("JHZ7FPM", 1).analyzeAll();
        Ante ante1 = run.getFirstAnte();

        Assertions.assertTrue(ante1.hasLegendary(LegendaryJoker.Perkeo));
        Assertions.assertTrue(ante1.hasLegendary(LegendaryJoker.Triboulet));

        float score = AnteScorer.getDefault().calculate(ante1);
        // Base ante bonus (8 - 1) * 10 = 70.
        // Each legendary has rarity 4 (50 * 4 = 200). Two legendaries = 400.
        // Plus packs, shop, vouchers. Score must be well above 470.
        Assertions.assertTrue(score >= 470.0f, "Multiple legendaries should accumulate score: " + score);
    }
}
