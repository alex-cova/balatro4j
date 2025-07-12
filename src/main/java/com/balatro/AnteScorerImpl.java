package com.balatro;

import com.balatro.api.Ante;
import com.balatro.api.Run;
import com.balatro.enums.*;
import com.balatro.structs.EditionItem;
import com.balatro.structs.JokerData;
import com.balatro.structs.Pack;
import org.jetbrains.annotations.NotNull;

public class AnteScorerImpl implements AnteScorer {

    @Override
    public float calculate(@NotNull Run run) {
        var score = 0.0f;

        for (var ante : run.antes()) {
            score += calculateScore(ante);
        }

        if (run.hasLegendary(LegendaryJoker.Perkeo)) {
            if (run.hasVoucher(Voucher.Observatory)) {
                score += 25.0f;
            }

            if (run.hasJoker(RareJoker.Blueprint, 10)) {
                score += 25.0f;
            }

            if (run.hasJoker(RareJoker.Brainstorm, 10)) {
                score += 25.0f;
            }

            if (run.hasJoker(RareJoker.Baron, 10) && run.hasJoker(UnCommonJoker.Mime, 10)) {
                score += 15.0f;
            }
        }

        if (run.hasLegendary(LegendaryJoker.Triboulet)) {
            if (run.hasJoker(RareJoker.Blueprint, 10)) {
                score += 25.0f;
            }

            if (run.hasJoker(RareJoker.Brainstorm, 10)) {
                score += 25.0f;
            }

            if (run.hasJoker(UnCommonJoker.Sock_and_Buskin, 10)) {
                score += 25.0f;
            }
        }

        if (run.hasLegendary(LegendaryJoker.Canio)) {
            if (run.hasJoker(UnCommonJoker.Pareidolia, 10)) {
                score += 25.0f;
            }
        }

        return score;
    }

    private float calculateScore(@NotNull Ante ante) {
        float score = 0.0f;

        for (JokerData value : ante.getLegendaryJokers().values()) {
            score = (50 * value.rarity) * value.getEdition().getMultiplier();
        }

        for (int i = 0; i < ante.getShopQueue().size(); i++) {
            var item = ante.getShopQueue().get(i);

            if (item.isJoker()) {
                score += ((50 - i) * item.jokerData().rarity) * item.edition().getMultiplier();
            }

            if (i > 30) {
                continue;
            }

            if (item.isSpectral()) {
                if (item.is(Spectral.Cryptid)) {
                    score += 2.0f;
                }

                if (item.is(Spectral.Deja_Vu)) {
                    score += 4.0f;
                }
            }

            if (item.isTarot()) {
                if (item.is(Tarot.Temperance)) {
                    score += 2.0f;
                }

                if (item.is(Tarot.The_Hermit)) {
                    score += 1.5f;
                }

                if (item.is(Tarot.The_Fool)) {
                    score += 1.0f;
                }
            }
        }

        for (Pack pack : ante.getPacks()) {
            if (pack.getType().isStandard()) continue;
            score += pack.getType().getChoices();

            if (pack.getType().isSpectral()) {
                if (pack.containsOption(Spectral.Cryptid)) {
                    score += 2.0f;
                }

                if (pack.containsOption(Spectral.Deja_Vu)) {
                    score += 4.0f;
                }
            }

            if (pack.getType().isArcana()) {
                if (pack.containsOption(Tarot.Temperance)) {
                    score += 2.0f;
                }

                if (pack.containsOption(Tarot.The_Hermit)) {
                    score += 1.5f;
                }

                if (pack.containsOption(Tarot.The_Fool)) {
                    score += 1.0f;
                }
            }

            if (pack.getType().isBuffon()) {
                for (EditionItem option : pack.getOptions()) {
                    score += (50 * option.jokerData().rarity) * option.edition().getMultiplier();
                }
            }
        }

        var tags = ante.getTags();
        var boss = ante.getBoss();
        var voucher = ante.getVoucher();

        for (Tag tag : tags) {
            if (tag == Tag.Negative_Tag) {
                score += 5;
            }

            if (tag == Tag.Charm_Tag) {
                score += 0.5f;
            }
        }

        if (boss == Boss.The_Arm) {
            score -= 0.5f;
        }

        if (ante.hasInPack(Specials.BLACKHOLE)) {
            score += 5.0f;
        }

        if (voucher == Voucher.Blank) {
            score += 1.0f;
        }

        if (voucher == Voucher.Clearance_Sale) {
            score += 0.5f;
        }

        if (voucher == Voucher.Overstock) {
            score += 0.2f;
        }

        if (voucher == Voucher.Liquidation) {
            score += 0.5f;
        }

        if (voucher == Voucher.Hieroglyph) {
            score += 0.5f;
        }

        if (voucher == Voucher.Paint_Brush) {
            score += 0.5f;
        }

        if (voucher == Voucher.Recyclomancy) {
            score += 0.5f;
        }

        if (voucher == Voucher.Grabber) {
            score += 0.5f;
        }

        if (voucher == Voucher.Wasteful) {
            score += 0.2f;
        }

        score += Math.max(0, 8 - ante.getAnte()) * 10;

        return score;
    }
}
