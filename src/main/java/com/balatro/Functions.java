package com.balatro;

import com.balatro.enums.*;
import com.balatro.enums.Card;
import com.balatro.enums.Pack;
import com.balatro.structs.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.balatro.Util.pseudohash;
import static com.balatro.Util.round13;

public class Functions extends Lock {

    public static final List<Tarot> TAROTS = Arrays.asList(Tarot.values());
    public static final List<Planet> PLANETS = Arrays.asList(Planet.values());
    public static final List<Spectral> SPECTRALS = Arrays.asList(Spectral.values());
    public static final List<LegendaryJoker> LEGENDARY_JOKERS = Arrays.asList(LegendaryJoker.values());
    public static final List<UnCommonJoker> UNCOMMON_JOKERS = Arrays.asList(UnCommonJoker.values());
    public static final List<UnCommonJoker101C> UNCOMMON_JOKERS_101C = Arrays.asList(UnCommonJoker101C.values());
    public static final List<UnCommonJoker100> UNCOMMON_JOKERS_100 = Arrays.asList(UnCommonJoker100.values());
    public static final List<Card> CARDS = Arrays.asList(Card.values());
    public static final List<Enhancement> ENHANCEMENTS = Arrays.asList(Enhancement.values());
    public static final List<Voucher> VOUCHERS = Arrays.asList(Voucher.values());
    public static final List<Tag> TAGS = Arrays.asList(Tag.values());
    public static final List<Pack> PACKS = Arrays.asList(Pack.values());
    public static final List<RareJoker> RARE_JOKERS = Arrays.asList(RareJoker.values());
    public static final List<RareJoker101C> RARE_JOKERS_101C = Arrays.asList(RareJoker101C.values());
    public static final List<RareJoker100> RARE_JOKERS_100 = Arrays.asList(RareJoker100.values());
    public static final List<CommonJoker> COMMON_JOKERS = Arrays.asList(CommonJoker.values());
    public static final List<CommonJoker100> COMMON_JOKERS_100 = Arrays.asList(CommonJoker100.values());
    public static final List<Boss> BOSSES = Arrays.asList(Boss.values());

    private final InstanceParams params;
    private final Cache cache;
    public final String seed;
    public final double hashedSeed;

    public Functions(String s, InstanceParams params) {
        seed = s;
        hashedSeed = pseudohash(s);
        this.params = params;
        cache = new Cache();
    }

    private LuaRandom getRandom(String id) {
        return new LuaRandom(getNode(id));
    }

    private double getNode(String ID) {
        var c = cache.nodes.get(ID);

        if (c == null) {
            c = pseudohash(ID + seed);
            cache.nodes.put(ID, c);
        }

        var value = round13((c * 1.72431234 + 2.134453429141) % 1);

        cache.nodes.put(ID, value);

        return (value + hashedSeed) / 2;
    }

    private double random(String ID) {
        var rng = getRandom(ID);
        return rng.random();
    }

    public Pack randweightedchoice(String ID, List<Pack> items) {
        var rng = getRandom(ID);
        double poll = rng.random() * items.getFirst().getValue();
        int idx = 1;
        double weight = 0;
        while (weight < poll) {
            weight += items.get(idx).getValue();
            idx++;
        }
        return items.get(idx - 1);
    }


    public <T extends Named> T randchoice(String ID, List<T> items) {
        var rng = getRandom(ID);
        T item = items.get(rng.randint(0, items.size() - 1));

        if (!params.showman && isLocked(item) || "RETRY".equals(item.getName())) {
            int resample = 2;
            while (true) {
                rng = getRandom(ID + "_resample" + resample);
                item = items.get(rng.randint(0, items.size() - 1));
                resample++;
                if ((!isLocked(item) && !"RETRY".equals(item.getName())) || resample > 1000) {
                    return item;
                }
            }
        }
        return item;
    }

    // Card Generators
    public String nextTarot(String source, int ante, boolean soulable) {
        if (soulable && (params.showman || !isLocked("The Soul")) && random("soul_Tarot" + ante) > 0.997) {
            return "The Soul";
        }
        return randchoice("Tarot" + source + ante, TAROTS).getName();
    }

    public String nextPlanet(String source, int ante, boolean soulable) {
        if (soulable && (params.showman || !isLocked("Black Hole")) && random("soul_Planet" + ante) > 0.997) {
            return "Black Hole";
        }
        return randchoice("Planet" + source + ante, PLANETS).name();
    }

    public String nextSpectral(String source, int ante, boolean soulable) {
        if (soulable) {
            String forcedKey = "RETRY";
            if ((params.showman || !isLocked("The Soul")) && random("soul_Spectral" + ante) > 0.997) {
                forcedKey = "The Soul";
            }
            if ((params.showman || !isLocked("Black Hole")) && random("soul_Spectral" + ante) > 0.997) {
                forcedKey = "Black Hole";
            }
            if (!forcedKey.equals("RETRY")) {
                return forcedKey;
            }
        }
        return randchoice("Spectral" + source + ante, SPECTRALS).getName();
    }

    static Set<String> setA = Set.of("Gros Michel", "Ice Cream", "Cavendish", "Luchador", "Turtle Bean", "Diet Cola", "Popcorn", "Ramen", "Seltzer", "Mr. Bones", "Invisible Joker");
    static Set<String> setB = Set.of("Ceremonial Dagger", "Ride the Bus", "Runner", "Constellation", "Green Joker", "Red Card", "Madness", "Square Joker", "Vampire", "Rocket", "Obelisk", "Lucky Cat", "Flash Card", "Spare Trousers", "Castle", "Wee Joker");

    public JokerData nextJoker(String source, int ante, boolean hasStickers) {

        // Get rarity
        String rarity;

        switch (source) {
            case "sou" -> rarity = "4";
            case "wra", "rta" -> rarity = "3";
            case "uta" -> rarity = "2";
            default -> {
                double rarityPoll = random("rarity" + ante + source);
                if (rarityPoll > 0.95) {
                    rarity = "3";
                } else if (rarityPoll > 0.7) {
                    rarity = "2";
                } else {
                    rarity = "1";
                }
            }
        }

        // Get edition
        int editionRate = 1;

        if (isVoucherActive("Glow Up")) {
            editionRate = 4;
        } else if (isVoucherActive("Hone")) {
            editionRate = 2;
        }

        String edition;
        double editionPoll = random("edi" + source + ante);

        if (editionPoll > 0.997) {
            edition = "Negative";
        } else if (editionPoll > 1 - 0.006 * editionRate) {
            edition = "Polychrome";
        } else if (editionPoll > 1 - 0.02 * editionRate) {
            edition = "Holographic";
        } else if (editionPoll > 1 - 0.04 * editionRate) {
            edition = "Foil";
        } else {
            edition = "No Edition";
        }

        // Get next joker
        Named joker;

        switch (rarity) {
            case "4" -> {
                if (params.version > 10099) {
                    joker = randchoice("Joker4", LEGENDARY_JOKERS);
                } else {
                    joker = randchoice("Joker4" + source + ante, LEGENDARY_JOKERS);
                }
            }
            case "3" -> {
                if (params.version > 10103) joker = randchoice("Joker3" + source + ante, RARE_JOKERS);
                else {
                    if (params.version > 10099) {
                        joker = randchoice("Joker3" + source + ante, RARE_JOKERS_101C);
                    } else {
                        joker = randchoice("Joker3" + source + ante, RARE_JOKERS_100);
                    }
                }
            }
            case "2" -> {
                if (params.version > 10103) {
                    joker = randchoice("Joker2" + source + ante, UNCOMMON_JOKERS);
                } else {
                    if (params.version > 10099) {
                        joker = randchoice("Joker2" + source + ante, UNCOMMON_JOKERS_101C);
                    } else {
                        joker = randchoice("Joker2" + source + ante, UNCOMMON_JOKERS_100);
                    }
                }
            }
            default -> {
                if (params.version > 10099) {
                    joker = randchoice("Joker1" + source + ante, COMMON_JOKERS);
                } else {
                    joker = randchoice("Joker1" + source + ante, COMMON_JOKERS_100);
                }
            }
        }

        // Get next joker stickers
        var stickers = new JokerStickers();
        if (hasStickers) {
            if (params.version > 10103) {
                double stickerPoll = random(((source.equals("buf")) ? "packetper" : "etperpoll") + ante);
                if (stickerPoll > 0.7 && (params.getStake() == Stake.Black_Stake || params.getStake() == Stake.Blue_Stake || params.getStake() == Stake.Purple_Stake || params.getStake() == Stake.Orange_Stake || params.getStake() == Stake.Gold_Stake)) {
                    if (!setA.contains(joker.getName())) {
                        stickers.eternal = true;
                    }
                }
                if ((stickerPoll > 0.4 && stickerPoll <= 0.7) && (params.getStake() == Stake.Orange_Stake || params.getStake() == Stake.Gold_Stake)) {
                    if (!setB.contains(joker.getName())) {
                        stickers.perishable = true;
                    }
                }
                if (params.getStake() == Stake.Gold_Stake) {
                    stickers.rental = random(((source.equals("buf")) ? "packssjr" : "ssjr") + ante) > 0.7;
                }
            } else {
                if (params.getStake() == Stake.Black_Stake || params.getStake() == Stake.Blue_Stake || params.getStake() == Stake.Purple_Stake || params.getStake() == Stake.Orange_Stake || params.getStake() == Stake.Gold_Stake) {
                    if (!setA.contains(joker.getName())) {
                        stickers.eternal = random("stake_shop_joker_eternal" + ante) > 0.7;
                    }
                }
                if (params.version > 10099) {
                    if ((params.getStake() == Stake.Orange_Stake || params.getStake() == Stake.Gold_Stake) && !stickers.eternal) {
                        stickers.perishable = random("ssjp" + ante) > 0.49;
                    }
                    if (params.getStake() == Stake.Gold_Stake) {
                        stickers.rental = random("ssjr" + ante) > 0.7;
                    }
                }
            }
        }

        return new JokerData(joker.getName(), rarity, edition, stickers);
    }

    // Shop Logic
    public ShopInstance getShopInstance() {
        double tarotRate = 4;
        double planetRate = 4;
        double playingCardRate = 0;
        double spectralRate = 0;

        if (params.getDeck() == Deck.GHOST_DECK) {
            spectralRate = 2;
        }
        if (isVoucherActive("Tarot Tycoon")) {
            tarotRate = 32;
        } else if (isVoucherActive("Tarot Merchant")) {
            tarotRate = 9.6;
        }
        if (isVoucherActive("Planet Tycoon")) {
            planetRate = 32;
        } else if (isVoucherActive("Planet Merchant")) {
            planetRate = 9.6;
        }
        if (isVoucherActive("Magic Trick")) {
            playingCardRate = 4;
        }

        return new ShopInstance(20, tarotRate, planetRate, playingCardRate, spectralRate);
    }

    public ShopItem nextShopItem(int ante) {
        var shop = getShopInstance();

        double cdtPoll = random("cdt" + ante) * shop.getTotalRate();

        String type;

        if (cdtPoll < shop.jokerRate) {
            type = "Joker";
        } else {
            cdtPoll -= shop.jokerRate;
            if (cdtPoll < shop.tarotRate) {
                type = "Tarot";
            } else {
                cdtPoll -= shop.tarotRate;
                if (cdtPoll < shop.planetRate) {
                    type = "Planet";
                } else {
                    cdtPoll -= shop.planetRate;
                    if (cdtPoll < shop.playingCardRate) {
                        type = "Playing Card";
                    } else {
                        type = "Spectral";
                    }
                }
            }
        }

        switch (type) {
            case "Joker" -> {
                var jkr = nextJoker("sho", ante, true);
                return new ShopItem(type, jkr.joker, jkr);
            }
            case "Tarot" -> {
                return new ShopItem(type, nextTarot("sho", ante, false));
            }
            case "Planet" -> {
                return new ShopItem(type, nextPlanet("sho", ante, false));
            }
            case "Spectral" -> {
                return new ShopItem(type, nextSpectral("sho", ante, false));
            }
        }

        return new ShopItem();
    }

    public Pack nextPack(int ante) {
        if (ante <= 2 && !cache.generatedFirstPack && params.version > 10099) {
            cache.generatedFirstPack = true;
            return Pack.Buffoon_Pack;
        }

        return randweightedchoice("shop_pack" + ante, PACKS);
    }

    public com.balatro.structs.Pack packInfo(@NotNull Pack p) {
        var pack = p.getName();
        if (pack.charAt(0) == 'M') {
            return new com.balatro.structs.Pack(pack.substring(5), (pack.charAt(5) == 'B' || pack.charAt(6) == 'p') ? 4 : 5, 2);
        } else if (pack.charAt(0) == 'J') {
            return new com.balatro.structs.Pack(pack.substring(6), (pack.charAt(6) == 'B' || pack.charAt(7) == 'p') ? 4 : 5, 1);
        } else {
            return new com.balatro.structs.Pack(pack, (pack.charAt(0) == 'B' || pack.charAt(1) == 'p') ? 2 : 3, 1);
        }
    }

    public com.balatro.structs.Card nextStandardCard(int ante) {
        // Enhancement
        String enhancement;

        if (random("stdset" + ante) <= 0.6) {
            enhancement = "No Enhancement";
        } else {
            enhancement = randchoice("Enhancedsta" + ante, ENHANCEMENTS).getName();
        }

        // Base
        var base = randchoice("frontsta" + ante, CARDS);

        // Edition
        String edition;

        double editionPoll = random("standard_edition" + ante);

        if (editionPoll > 0.988) {
            edition = "Polychrome";
        } else if (editionPoll > 0.96) {
            edition = "Holographic";
        } else if (editionPoll > 0.92) {
            edition = "Foil";
        } else {
            edition = "No Edition";
        }

        // Seal
        String seal;

        if (random("stdseal" + ante) <= 0.8) {
            seal = "No Seal";
        } else {
            double sealPoll = random("stdsealtype" + ante);
            if (sealPoll > 0.75) {
                seal = "Red Seal";
            } else if (sealPoll > 0.5) {
                seal = "Blue Seal";
            } else if (sealPoll > 0.25) {
                seal = "Gold Seal";
            } else {
                seal = "Purple Seal";
            }
        }

        return new com.balatro.structs.Card(base.getName(), enhancement, edition, seal);
    }

    public List<String> nextArcanaPack(int size, int ante) {
        List<String> pack = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            if (isVoucherActive("Omen Globe") && random("omen_globe") > 0.8) {
                pack.add(nextSpectral("ar2", ante, true));
            } else {
                pack.add(nextTarot("ar1", ante, true));
            }
            if (!params.showman) {
                lock(pack.get(i));
            }
        }

        for (int i = 0; i < size; i++) {
            unlock(pack.get(i));
        }

        return pack;
    }

    public List<String> nextCelestialPack(int size, int ante) {
        List<String> pack = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            pack.add(nextPlanet("pl1", ante, true));
            if (!params.showman) {
                lock(pack.get(i));
            }
        }

        for (int i = 0; i < size; i++) {
            unlock(pack.get(i));
        }

        return pack;
    }

    public List<String> nextSpectralPack(int size, int ante) {
        List<String> pack = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            pack.add(nextSpectral("spe", ante, true));

            if (!params.isShowman()) {
                lock(pack.get(i));
            }
        }

        for (int i = 0; i < size; i++) {
            unlock(pack.get(i));
        }

        return pack;
    }

    public List<com.balatro.structs.Card> nextStandardPack(int size, int ante) {
        List<com.balatro.structs.Card> pack = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            pack.add(nextStandardCard(ante));
        }

        return pack;
    }

    public List<JokerData> nextBuffoonPack(int size, int ante) {
        List<JokerData> pack = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            pack.add(nextJoker("buf", ante, true));
            if (!params.isShowman()) {
                lock(pack.get(i).getJoker());
            }
        }
        for (int i = 0; i < size; i++) {
            unlock(pack.get(i).getJoker());
        }

        return pack;
    }

    // Misc methods
    public boolean isVoucherActive(String voucher) {
        return params.getVouchers().contains(voucher);
    }

    public void activateVoucher(String voucher) {
        params.getVouchers().add(voucher);
        lock(voucher);
        // Unlock next level voucher
        for (int i = 0; i < VOUCHERS.size(); i += 2) {
            if (VOUCHERS.get(i).equals(voucher)) {
                unlock(VOUCHERS.get(i + 1).getName());
            }
        }
    }

    public Voucher nextVoucher(int ante) {
        return randchoice("Voucher" + ante, VOUCHERS);
    }

    public void setDeck(Deck deck) {
        params.setDeck(deck);
        switch (deck) {
            case MAGIC_DECK:
                activateVoucher("Crystal Ball");
                break;
            case NEBULA_DECK:
                activateVoucher("Telescope");
                break;
            case ZODIAC_DECK:
                activateVoucher("Tarot Merchant");
                activateVoucher("Planet Merchant");
                activateVoucher("Overstock");
                break;
            default:
                break;
        }
    }

    public Tag nextTag(int ante) {
        return randchoice("Tag" + ante, TAGS);
    }

    public Boss nextBoss(int ante) {
        List<Boss> bossPool = new ArrayList<>();
        int numBosses = 0;

        // First pass: Try to find unlocked bosses
        for (Boss boss : BOSSES) {
            if (!isLocked(boss)) {
                if ((ante % 8 == 0 && boss.charAt(0) != 'T') ||
                        (ante % 8 != 0 && boss.charAt(0) == 'T')) {
                    bossPool.add(boss);
                    numBosses++;
                }
            }
        }

        // If no bosses found, unlock appropriate bosses and try again
        if (numBosses == 0) {
            for (Boss boss : BOSSES) {
                if ((ante % 8 == 0 && boss.charAt(0) != 'T') ||
                        (ante % 8 != 0 && boss.charAt(0) == 'T')) {
                    unlock(boss.getName());
                }
            }
            return nextBoss(ante);
        }

        var chosenBoss = randchoice("boss", bossPool);
        lock(chosenBoss);
        return chosenBoss;
    }

}

