package com.balatro.impl;

import com.balatro.api.Ante;
import com.balatro.api.AnteScorer;
import com.balatro.api.Joker;
import com.balatro.api.Run;
import com.balatro.api.Shop;
import com.balatro.enums.*;
import com.balatro.structs.EditionItem;
import com.balatro.structs.JokerData;
import com.balatro.structs.Pack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * High-performance scoring engine that evaluates the strategic potential and quality of a Balatro run or ante.
 * <p>
 * Evaluates:
 * <ul>
 *   <li><b>Legendary Jokers</b>: Value, rarity scaling, and editions (accumulative rather than overwriting)</li>
 *   <li><b>Synergies</b>: Iconic engine combos (Perkeo + Observatory/Cryptid, Baron + Mime, Triboulet + Retriggers, Canio + Pareidolia/Trading Card)</li>
 *   <li><b>Shop Queue Items</b>: Rarity and edition multipliers weighted by reroll depth (with floor to prevent negative scores)</li>
 *   <li><b>Booster Packs</b>: High-impact Tarots, Spectrals, Buffoons, and Celestial packs</li>
 *   <li><b>Vouchers & Tags</b>: Slot additions (Antimatter), economy scalers, reroll modifiers, negative tags</li>
 *   <li><b>Boss Hazards</b>: Run-ending bosses penalized (mitigated when Chicot is present)</li>
 *   <li><b>Progression</b>: Early-ante priority weighting</li>
 * </ul>
 */
public class AnteScorerImpl implements AnteScorer {

    public static final AnteScorerImpl INSTANCE = new AnteScorerImpl();

    public AnteScorerImpl() {
    }

    @Override
    public float calculate(@Nullable Run run) {
        if (run == null) {
            return 0.0f;
        }

        var antes = run.antes();
        if (antes == null || antes.isEmpty()) {
            return 0.0f;
        }

        boolean hasChicot = run.hasLegendary(LegendaryJoker.Chicot);
        float score = 0.0f;

        for (Ante ante : antes) {
            if (ante != null) {
                score += calculate(ante, hasChicot);
            }
        }

        score += calculateSynergies(run);

        return score;
    }

    @Override
    public float calculate(@Nullable Ante ante) {
        return calculate(ante, false);
    }

    /**
     * Calculates the score for an individual ante, with optional boss hazard negation (e.g. from Chicot).
     *
     * @param ante        the ante to score
     * @param nullifyBoss true if boss penalties should be ignored
     * @return the computed score for the ante
     */
    public float calculate(@Nullable Ante ante, boolean nullifyBoss) {
        if (ante == null) {
            return 0.0f;
        }

        float score = 0.0f;

        // 1. Legendary Jokers in ante (accumulative, bug fix for previous '=' overwrite and 0 rarity)
        Map<String, JokerData> legendaryJokers = ante.getLegendaryJokers();
        if (legendaryJokers != null && !legendaryJokers.isEmpty()) {
            for (JokerData value : legendaryJokers.values()) {
                if (value != null) {
                    Edition edition = value.getEdition();
                    float multiplier = (edition.getMultiplier() > 0) ? edition.getMultiplier() : 1.0f;
                    int rarity = value.rarity > 0 ? value.rarity : 4;
                    score += (50.0f * rarity) * multiplier;
                }
            }
        }

        // 2. Shop Queue items (evaluated once to avoid repeated defensive copies)
        Shop shopQueue = ante.getShopQueue();
        if (shopQueue != null) {
            int queueSize = shopQueue.size();
            for (int i = 0; i < queueSize; i++) {
                EditionItem item = shopQueue.get(i);
                if (item == null) {
                    continue;
                }

                if (item.isJoker()) {
                    int rarity = getJokerRarity(item);
                    Edition edition = item.edition();
                    float multiplier = edition.getMultiplier();
                    // Bounded position weight to prevent negative scores for deep shop slots
                    int positionWeight = Math.max(1, 50 - i);
                    score += (positionWeight * rarity) * multiplier;
                }

                // Consumables evaluated in reachable shop depth (first 30 slots)
                if (i <= 30) {
                    if (item.isSpectral() && item.item() instanceof Spectral spectral) {
                        score += scoreSpectral(spectral);
                    } else if (item.isTarot() && item.item() instanceof Tarot tarot) {
                        score += scoreTarot(tarot);
                    }
                }
            }
        }

        // 3. Packs
        List<Pack> packs = ante.getPacks();
        if (packs != null) {
            for (Pack pack : packs) {
                if (pack == null || pack.getType().isStandard()) {
                    continue;
                }

                score += pack.getType().getChoices();

                if (pack.getType().isSpectral()) {
                    Collection<EditionItem> options = pack.getOptions();
                    if (options != null) {
                        for (EditionItem option : options) {
                            if (option != null && option.item() instanceof Spectral s) {
                                score += scoreSpectral(s);
                            }
                        }
                    }
                } else if (pack.getType().isArcana()) {
                    Collection<EditionItem> options = pack.getOptions();
                    if (options != null) {
                        for (EditionItem option : options) {
                            if (option != null && option.item() instanceof Tarot t) {
                                score += scoreTarot(t);
                            }
                        }
                    }
                } else if (pack.getType().isBuffon()) {
                    Collection<EditionItem> options = pack.getOptions();
                    if (options != null) {
                        for (EditionItem option : options) {
                            if (option != null) {
                                int rarity = getJokerRarity(option);
                                score += (50.0f * rarity) * option.edition().getMultiplier();
                            }
                        }
                    }
                }
            }
        }

        // 4. Tags
        Set<Tag> tags = ante.getTags();
        if (tags != null) {
            for (Tag tag : tags) {
                if (tag != null) {
                    score += scoreTag(tag);
                }
            }
        }

        // 5. Boss hazards
        if (!nullifyBoss) {
            Boss boss = ante.getBoss();
            if (boss != null) {
                score += scoreBoss(boss);
            }
        }

        // 6. Specials (Black Hole)
        if (ante.hasInPack(Specials.BLACKHOLE)) {
            score += 5.0f;
        }

        // 7. Vouchers
        Voucher voucher = ante.getVoucher();
        if (voucher != null) {
            score += scoreVoucher(voucher);
        }

        // 8. Early ante progression bonus (earlier antes are more impactful)
        score += Math.max(0, 8 - ante.getAnte()) * 10.0f;

        return score;
    }

    /**
     * Calculates synergy scores across the entire run for iconic high-score and endless builds.
     *
     * @param run the run to evaluate
     * @return total synergy bonus score
     */
    public float calculateSynergies(@NotNull Run run) {
        float score = 0.0f;

        // Memoize common query results across synergies to avoid repeated multi-ante traversals
        boolean hasBlueprint = run.hasJoker(RareJoker.Blueprint, 10);
        boolean hasBrainstorm = run.hasJoker(RareJoker.Brainstorm, 10);
        boolean hasBaron = run.hasJoker(RareJoker.Baron, 10);
        boolean hasMime = run.hasJoker(UnCommonJoker.Mime, 10);

        // --- Perkeo synergies ---
        if (run.hasLegendary(LegendaryJoker.Perkeo)) {
            if (run.hasVoucher(Voucher.Observatory)) {
                score += 25.0f;
            }
            if (hasBlueprint) {
                score += 25.0f;
            }
            if (hasBrainstorm) {
                score += 25.0f;
            }
            if (hasBaron && hasMime) {
                score += 15.0f;
            }
            if (run.hasInPack(Spectral.Cryptid) || run.hasInShop(Spectral.Cryptid)) {
                score += 20.0f; // Infinite Cryptids via Perkeo duplication
            }
        }

        // --- Triboulet synergies ---
        if (run.hasLegendary(LegendaryJoker.Triboulet)) {
            if (hasBlueprint) {
                score += 25.0f;
            }
            if (hasBrainstorm) {
                score += 25.0f;
            }
            if (run.hasJoker(UnCommonJoker.Sock_and_Buskin, 10)) {
                score += 25.0f;
            }
            if (run.hasJoker(CommonJoker.Hanging_Chad, 10)) {
                score += 10.0f;
            }
        }

        // --- Canio synergies ---
        if (run.hasLegendary(LegendaryJoker.Canio)) {
            if (run.hasJoker(UnCommonJoker.Pareidolia, 10)) {
                score += 25.0f;
            }
            if (hasBlueprint) {
                score += 25.0f;
            }
            if (hasBrainstorm) {
                score += 25.0f;
            }
            if (run.hasJoker(UnCommonJoker.Trading_Card, 10)) {
                score += 15.0f;
            }
        }

        // --- Yorick synergies ---
        if (run.hasLegendary(LegendaryJoker.Yorick)) {
            if (hasBlueprint) {
                score += 25.0f;
            }
            if (hasBrainstorm) {
                score += 25.0f;
            }
            if (run.hasJoker(UnCommonJoker.Merry_Andy, 10) || run.hasJoker(UnCommonJoker.Burglar, 10)) {
                score += 10.0f;
            }
        }

        // --- Chicot utility ---
        if (run.hasLegendary(LegendaryJoker.Chicot)) {
            score += 10.0f; // Disables all Boss Blind effects across all antes
        }

        // --- Standalone Baron + Mime engine (independent of Perkeo) ---
        if (!run.hasLegendary(LegendaryJoker.Perkeo) && hasBaron && hasMime) {
            score += 20.0f;
            if (hasBlueprint || hasBrainstorm) {
                score += 15.0f;
            }
        }

        return score;
    }

    public float scoreSpectral(@Nullable Spectral spectral) {
        if (spectral == null) return 0.0f;
        return switch (spectral) {
            case Deja_Vu, Ectoplasm -> 4.0f;
            case Cryptid -> 2.0f;
            case Immolate, Ankh -> 3.0f;
            case Hex -> 2.5f;
            case Trance, Medium -> 1.5f;
            case Talisman, Aura -> 1.0f;
            default -> 0.0f;
        };
    }

    public float scoreTarot(@Nullable Tarot tarot) {
        if (tarot == null) return 0.0f;
        return switch (tarot) {
            case Death, The_Hanged_Man -> 2.5f;
            case Temperance -> 2.0f;
            case The_Hermit, Judgement -> 1.5f;
            case The_Fool, The_Wheel_of_Fortune, The_Emperor -> 1.0f;
            case The_High_Priestess -> 0.5f;
            default -> 0.0f;
        };
    }

    public float scoreVoucher(@Nullable Voucher voucher) {
        if (voucher == null) return 0.0f;
        return switch (voucher) {
            case Antimatter -> 5.0f;
            case Observatory -> 2.0f;
            case Blank, Palett, Nacho_Tong, Directors_Cut, Retcon, Telescope, Omen_Globe -> 1.0f;
            case Clearance_Sale, Liquidation, Hieroglyph, Petroglyph, Paint_Brush, Recyclomancy, Grabber,
                 Crystal_Ball, Reroll_Surplus, Reroll_Glut, Overstock_Plus -> 0.5f;
            case Overstock, Wasteful -> 0.2f;
            default -> 0.0f;
        };
    }

    public float scoreTag(@Nullable Tag tag) {
        if (tag == null) return 0.0f;
        return switch (tag) {
            case Negative_Tag -> 5.0f;
            case Double_Tag -> 2.5f;
            case Ethereal_Tag, Polychrome_Tag -> 2.0f;
            case Voucher_Tag, Rare_Tag -> 1.5f;
            case Buffoon_Tag, Coupon_Tag -> 1.0f;
            case Charm_Tag, Orbital_Tag, Economy_Tag, Meteor_Tag, Investment_Tag, D6_Tag -> 0.5f;
            default -> 0.0f;
        };
    }

    public float scoreBoss(@Nullable Boss boss) {
        if (boss == null) return 0.0f;
        return switch (boss) {
            case The_Plant -> -1.0f; // Debuffs all face cards (fatal to King/Queen engines)
            case The_Arm -> -0.5f;   // Decreases poker hand level
            case The_Needle -> -0.5f;// Play only 1 hand
            case The_Eye -> -0.5f;   // No repeat hand types
            case The_Mouth -> -0.5f; // Only 1 hand type
            case The_Water -> -0.5f; // 0 discards
            case The_Ox -> -0.5f;    // Resets money to $0
            default -> 0.0f;
        };
    }

    private static int getJokerRarity(@NotNull EditionItem item) {
        if (item.item() instanceof Joker joker) {
            return joker.getType().getRarity();
        }
        return 0;
    }
}
