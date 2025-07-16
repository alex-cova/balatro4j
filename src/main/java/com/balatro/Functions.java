package com.balatro;

import com.balatro.api.Item;
import com.balatro.api.Lock;
import com.balatro.enums.*;
import com.balatro.enums.Card;
import com.balatro.structs.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.balatro.Util.pseudohash;
import static com.balatro.Util.round13;

public final class Functions implements Lock {

    public static final Tarot[] TAROTS = Tarot.values();
    public static final Planet[] PLANETS = Planet.values();
    public static final Spectral[] SPECTRALS = Spectral.values();
    public static final LegendaryJoker[] LEGENDARY_JOKERS = LegendaryJoker.values();
    public static final UnCommonJoker[] UNCOMMON_JOKERS = UnCommonJoker.values();
    public static final UnCommonJoker101C[] UNCOMMON_JOKERS_101C = UnCommonJoker101C.values();
    public static final UnCommonJoker100[] UNCOMMON_JOKERS_100 = UnCommonJoker100.values();
    public static final Card[] CARDS = Card.values();
    public static final Enhancement[] ENHANCEMENTS = Enhancement.values();
    public static final Voucher[] VOUCHERS = Voucher.values();
    public static final Tag[] TAGS = Tag.values();
    public static final PackType[] PACKS = PackType.values();
    public static final RareJoker[] RARE_JOKERS = RareJoker.values();
    public static final RareJoker101C[] RARE_JOKERS_101C = RareJoker101C.values();
    public static final RareJoker100[] RARE_JOKERS_100 = RareJoker100.values();
    public static final CommonJoker[] COMMON_JOKERS = CommonJoker.values();
    public static final CommonJoker100[] COMMON_JOKERS_100 = CommonJoker100.values();
    public static final Boss[] BOSSES = Boss.values();

    private final InstanceParams params;
    private final Cache cache;
    public final byte[] seed;
    public final double hashedSeed;
    private final Lock lock;

    public Functions(@NotNull String seed, int maxAnte, InstanceParams params) {
        this(seed.getBytes(), maxAnte, params);
    }

    public Functions(byte[] seed, int maxAnte, InstanceParams params) {
        this.seed = seed;
        hashedSeed = pseudohash(seed);
        this.params = params;
        cache = new Cache2D(maxAnte);
        this.lock = new LongArrayLock();
    }

    private int randintResample(String id, int max) {
        var c = cache.get(id);

        if (c == null) {
            c = pseudohash(id.getBytes(), seed);
            cache.put(id, c);
        }

        var value = round13((c * 1.72431234 + 2.134453429141) % 1);

        cache.put(id, value);

        return LuaRandom.randint((value + hashedSeed) / 2, max);
    }

    private double getNode(Coordinate id) {
        var c = cache.get(id);

        if (c == 0.0) {
            c = id.pseudohash(seed);
            cache.put(id, c);
        }

        var value = round13((c * 1.72431234 + 2.134453429141) % 1);

        cache.put(id, value);

        return (value + hashedSeed) / 2;
    }

    private double random(Coordinate coordinate) {
        return LuaRandom.random(getNode(coordinate));
    }

    private int randint(Coordinate c, int max) {
        return LuaRandom.randint(getNode(c), max);
    }

    public PackType randweightedchoice(Coordinate ID, PackType[] items) {
        double poll = random(ID) * 22.42;
        int idx = 0;
        double weight = 0;
        while (weight < poll) {
            weight += items[idx].getValue();
            idx++;
        }
        return items[idx - 1];
    }

    // Card Generators
    public Item nextTarot(Coordinate source, int ante, boolean soulable) {
        if (soulable && (params.isShowman() || !isLocked(Specials.THE_SOUL)) && random(soul_TarotArr[ante]) > 0.997) {
            var data = nextJoker("sou", joker1SouArr, joker2SouArr, joker3SouArr, joker4SouArr, raritySouArr, editionSouArr, ante, true);
            lock(data.joker);
            lock(Specials.THE_SOUL);
            return new EditionItem(data.joker, data.edition);
        }
        var tarot = randchoice(source, TAROTS);

        if (tarot == Tarot.The_Wheel_of_Fortune) {
            return new EditionItem(tarot, nextWheelOfFortune());
        }

        return tarot;
    }

    public Item nextPlanet(Coordinate source, int ante, boolean soulable) {
        if (soulable && (params.isShowman() || !isLocked(Specials.BLACKHOLE)) && random(soul_PlanetArr[ante]) > 0.997) {
            return Specials.BLACKHOLE;
        }
        return randchoice(source, PLANETS);
    }

    public Item nextSpectral(Coordinate source, int ante, boolean soulable) {
        if (soulable) {
            Item forcedKey = null;
            Edition edition = null;

            if ((params.isShowman() || !isLocked(Specials.THE_SOUL)) && random(soul_SpectralArr[ante]) > 0.997) {
                var data = nextJoker("sou", joker1SouArr, joker2SouArr, joker3SouArr, joker4SouArr, raritySouArr, editionSouArr, ante, true);
                forcedKey = data.joker;
                edition = data.edition;

                lock(data.joker);
            }

            if ((params.isShowman() || !isLocked(Specials.BLACKHOLE)) && random(soul_SpectralArr[ante]) > 0.997) {
                forcedKey = Specials.BLACKHOLE;
            }

            if (forcedKey != null) {
                if (edition != null) {
                    return new EditionItem(forcedKey, edition);
                }
                return forcedKey;
            }
        }

        return randchoice(source, SPECTRALS);
    }

    static final Set<String> setA = Set.of("Gros Michel", "Ice Cream", "Cavendish", "Luchador", "Turtle Bean", "Diet Cola",
            "Popcorn", "Ramen", "Seltzer", "Mr. Bones", "Invisible Joker");

    static final Set<String> setB = Set.of("Ceremonial Dagger", "Ride the Bus", "Runner", "Constellation", "Green Joker",
            "Red Card", "Madness", "Square Joker", "Vampire", "Rocket", "Obelisk", "Lucky Cat", "Flash Card",
            "Spare Trousers", "Castle", "Wee Joker");

    public JokerData nextJoker(@NotNull String source,
                               Coordinate[] joker1Arr, Coordinate[] joker2Arr, Coordinate[] joker3Arr, Coordinate[] joker4Arr,
                               Coordinate[] rarityArr, Coordinate[] editionArr,
                               int ante, boolean hasStickers) {
        // Get rarity
        int rarity = 1;

        switch (source) {
            case "sou" -> rarity = 4;
            case "wra", "rta" -> rarity = 3;
            case "uta" -> rarity = 2;
            default -> {
                double rarityPoll = random(rarityArr[ante]);
                if (rarityPoll > 0.95) {
                    rarity = 3;
                } else if (rarityPoll > 0.7) {
                    rarity = 2;
                }
            }
        }

        final var edition = getEdition(ante, editionArr);

        // Get next joker
        Coordinate coordinate;
        Item[] items;

        switch (rarity) {
            case 4 -> {
                items = LEGENDARY_JOKERS;
                if (params.version > 10099) {
                    coordinate = Joker4;
                } else {
                    coordinate = joker4Arr[ante];
                }
            }
            case 3 -> {
                coordinate = joker3Arr[ante];

                if (params.version > 10103) {
                    items = RARE_JOKERS;
                } else {
                    if (params.version > 10099) {
                        items = RARE_JOKERS_101C;
                    } else {
                        items = RARE_JOKERS_100;
                    }
                }
            }
            case 2 -> {
                coordinate = joker2Arr[ante];

                if (params.version > 10103) {
                    items = UNCOMMON_JOKERS;
                } else {
                    if (params.version > 10099) {
                        items = UNCOMMON_JOKERS_101C;
                    } else {
                        items = UNCOMMON_JOKERS_100;
                    }
                }
            }
            default -> {
                coordinate = joker1Arr[ante];

                if (params.version > 10099) {
                    items = COMMON_JOKERS;
                } else {
                    items = COMMON_JOKERS_100;
                }
            }
        }

        final var joker = randchoice(coordinate, items);

        // Get next joker stickers
        var stickers = new JokerStickers();

        if (hasStickers) {
            if (params.version > 10103) {
                boolean searchForSticker = (params.getStake() == Stake.Black_Stake ||
                                            params.getStake() == Stake.Blue_Stake ||
                                            params.getStake() == Stake.Purple_Stake ||
                                            params.getStake() == Stake.Orange_Stake ||
                                            params.getStake() == Stake.Gold_Stake);

                double stickerPoll = 0.0;

                if (searchForSticker) {
                    if (source.equals("buf")) {
                        stickerPoll = random(packetperArr[ante]);
                    } else {
                        stickerPoll = random(etperpollArr[ante]);
                    }
                }

                if (stickerPoll > 0.7) {
                    if (!setA.contains(joker.getName())) {
                        stickers.setRental(true);
                    }
                }

                if ((stickerPoll > 0.4 && stickerPoll <= 0.7) && (params.getStake() == Stake.Orange_Stake || params.getStake() == Stake.Gold_Stake)) {
                    if (!setB.contains(joker.getName())) {
                        stickers.setPerishable(true);
                    }
                }

                if (params.getStake() == Stake.Gold_Stake) {
                    if (source.equals("buf")) {
                        stickers.setRental(random(packssjrArr[ante]) > 0.7);
                    } else {
                        stickers.setRental(random(ssjrArr[ante]) > 0.7);
                    }
                }

            } else {
                if (params.getStake() == Stake.Black_Stake || params.getStake() == Stake.Blue_Stake ||
                    params.getStake() == Stake.Purple_Stake || params.getStake() == Stake.Orange_Stake ||
                    params.getStake() == Stake.Gold_Stake) {
                    if (!setA.contains(joker.getName())) {
                        stickers.setEternal(random(stake_shop_joker_eternalArr[ante]) > 0.7);
                    }
                }
                if (params.version > 10099) {
                    if ((params.getStake() == Stake.Orange_Stake || params.getStake() == Stake.Gold_Stake) && !stickers.isEternal()) {
                        stickers.setPerishable(random(ssjpArr[ante]) > 0.49);
                    }
                    if (params.getStake() == Stake.Gold_Stake) {
                        stickers.setRental(random(ssjrArr[ante]) > 0.7);
                    }
                }
            }
        }

        return new JokerData(joker, rarity, edition, stickers)
                .setResampleInfo(coordinate, items);
    }

    // Shop Logic
    @Contract(" -> new")
    public @NotNull ShopInstance getShopInstance() {
        double tarotRate = 4;
        double planetRate = 4;
        double playingCardRate = 0;
        double spectralRate = 0;

        if (params.getDeck() == Deck.GHOST_DECK) {
            spectralRate = 2;
        }
        if (isVoucherActive(Voucher.Tarot_Tycoon)) {
            tarotRate = 32;
        } else if (isVoucherActive(Voucher.Tarot_Merchant)) {
            tarotRate = 9.6;
        }
        if (isVoucherActive(Voucher.Planet_Tycoon)) {
            planetRate = 32;
        } else if (isVoucherActive(Voucher.Planet_Merchant)) {
            planetRate = 9.6;
        }
        if (isVoucherActive(Voucher.Magic_Trick)) {
            playingCardRate = 4;
        }

        return new ShopInstance(20, tarotRate, planetRate, playingCardRate, spectralRate);
    }

    @Contract("_ -> new")
    public @NotNull ShopItem nextShopItem(int ante) {
        var shop = getShopInstance();

        double cdtPoll = random(cdtArr[ante]) * shop.getTotalRate();

        Type type;

        if (cdtPoll < shop.jokerRate) {
            type = Type.Joker;
        } else {
            cdtPoll -= shop.jokerRate;
            if (cdtPoll < shop.tarotRate) {
                type = Type.Tarot;
            } else {
                cdtPoll -= shop.tarotRate;
                if (cdtPoll < shop.planetRate) {
                    type = Type.Planet;
                } else {
                    cdtPoll -= shop.planetRate;
                    if (cdtPoll < shop.playingCardRate) {
                        type = Type.PlayingCard;
                    } else {
                        type = Type.Spectral;
                    }
                }
            }
        }

        switch (type) {
            case Type.Joker -> {
                var jkr = nextJoker("sho", joker1ShoArr, joker2ShoArr, joker3ShoArr, joker4ShoArr,
                        rarityShoArr, editionShoArr, ante, true);
                return new ShopItem(type, jkr.joker, jkr);
            }
            case Type.Tarot -> {
                return new ShopItem(type, nextTarot(tarotShoArr[ante], ante, false));
            }
            case Type.Planet -> {
                return new ShopItem(type, nextPlanet(planetShoArr[ante], ante, false));
            }
            case Type.Spectral -> {
                return new ShopItem(type, nextSpectral(spectralShoArr[ante], ante, false));
            }
            case Type.PlayingCard -> {
                return new ShopItem(type, nextStandardCard(ante));
            }
        }

        throw new IllegalStateException("Error: ShopItem type not found");

//        return new ShopItem();
    }

    public PackType nextPack(int ante) {
        if (ante <= 2 && !cache.isGeneratedFirstPack() && params.version > 10099) {
            cache.setGeneratedFirstPack(true);
            return PackType.Buffoon_Pack;
        }

        return randweightedchoice(shop_packArr[ante], PACKS);
    }

    public static Coordinate[] planetShoArr;
    public static Coordinate[] planetpl1lArr;
    public static Coordinate[] tarotShoArr;
    public static Coordinate[] tarotAr1Arr;
    public static Coordinate[] tarotarArr;
    public static Coordinate[] spectralShoArr;
    public static Coordinate[] spectralAr2Arr;
    public static Coordinate[] spectralSpeArr;
    public static Coordinate[] joker4ShoArr;
    public static Coordinate[] joker4BufArr;
    public static Coordinate[] joker4SouArr;
    public static Coordinate[] joker3ShoArr;
    public static Coordinate[] joker3BufArr;
    public static Coordinate[] joker3SouArr;
    public static Coordinate[] joker2ShoArr;
    public static Coordinate[] joker2BufArr;
    public static Coordinate[] joker2SouArr;
    public static Coordinate[] joker1ShoArr;
    public static Coordinate[] joker1BufArr;
    public static Coordinate[] joker1SouArr;

    public static Coordinate[] rarityShoArr;
    public static Coordinate[] rarityBufArr;
    public static Coordinate[] raritySouArr;
    public static Coordinate[] editionShoArr;
    public static Coordinate[] editionBufArr;
    public static Coordinate[] editionSouArr;

    public static Coordinate[] packssjrArr;
    public static Coordinate[] etperpollArr;
    public static Coordinate[] packetperArr;
    public static Coordinate[] stake_shop_joker_eternalArr;
    public static Coordinate[] ssjpArr;
    public static Coordinate[] ssjrArr;
    public static Coordinate[] shop_packArr;
    public static Coordinate[] stdsetArr;
    public static Coordinate[] standard_editionArr;
    public static Coordinate[] enhancedstaArr;
    public static Coordinate[] stdsealArr;
    public static Coordinate[] stdsealtypeArr;
    public static Coordinate[] frontstaArr;
    public static Coordinate[] soul_SpectralArr;
    public static Coordinate[] soul_PlanetArr;
    public static Coordinate[] soul_TarotArr;
    public static Coordinate[] cdtArr;
    public static Coordinate[] VoucherArr;
    public static Coordinate[] TagArr;
    public static Coordinate boss = new Coordinate("boss", -1, 0);
    public static Coordinate omen_globe = new Coordinate("omen_globe", -1, 1);
    public static Coordinate Joker4 = new Coordinate("Joker4", -1, 2);
    public static Coordinate wheel_of_fortune = new Coordinate("wheel_of_fortune", -1, 3);
    public static Coordinate edition_generic = new Coordinate("edition_generic", -1, 4);

    static {
        heat(30);
    }

    @SuppressWarnings("SameParameterValue")
    private static void heat(int max) {
        max = max + 1;

        if (rarityShoArr != null && rarityShoArr.length == max) return;
        long init = System.currentTimeMillis();

        rarityShoArr = new Coordinate[max];
        rarityBufArr = new Coordinate[max];
        raritySouArr = new Coordinate[max];
        packssjrArr = new Coordinate[max];
        etperpollArr = new Coordinate[max];
        packetperArr = new Coordinate[max];
        stake_shop_joker_eternalArr = new Coordinate[max];
        ssjpArr = new Coordinate[max];
        ssjrArr = new Coordinate[max];
        shop_packArr = new Coordinate[max];
        TagArr = new Coordinate[max];
        VoucherArr = new Coordinate[max];
        cdtArr = new Coordinate[max];
        soul_PlanetArr = new Coordinate[max];
        stdsetArr = new Coordinate[max];
        standard_editionArr = new Coordinate[max];
        enhancedstaArr = new Coordinate[max];
        stdsealArr = new Coordinate[max];
        stdsealtypeArr = new Coordinate[max];
        frontstaArr = new Coordinate[max];
        soul_SpectralArr = new Coordinate[max];
        soul_TarotArr = new Coordinate[max];
        planetShoArr = new Coordinate[max];
        planetpl1lArr = new Coordinate[max];
        tarotShoArr = new Coordinate[max];
        tarotarArr = new Coordinate[max];
        tarotAr1Arr = new Coordinate[max];
        spectralShoArr = new Coordinate[max];
        spectralAr2Arr = new Coordinate[max];
        spectralSpeArr = new Coordinate[max];
        joker4ShoArr = new Coordinate[max];
        joker4BufArr = new Coordinate[max];
        joker4SouArr = new Coordinate[max];
        joker3ShoArr = new Coordinate[max];
        joker3BufArr = new Coordinate[max];
        joker3SouArr = new Coordinate[max];
        joker2ShoArr = new Coordinate[max];
        joker2BufArr = new Coordinate[max];
        joker2SouArr = new Coordinate[max];
        joker1ShoArr = new Coordinate[max];
        joker1BufArr = new Coordinate[max];
        joker1SouArr = new Coordinate[max];
        editionShoArr = new Coordinate[max];
        editionBufArr = new Coordinate[max];
        editionSouArr = new Coordinate[max];

        for (int ante = 0; ante < max; ante++) {
            stdsetArr[ante] = new Coordinate("stdset" + ante, ante, 0);
            standard_editionArr[ante] = new Coordinate("standard_edition" + ante, ante, 1);
            enhancedstaArr[ante] = new Coordinate("Enhancedsta" + ante, ante, 2);
            stdsealArr[ante] = new Coordinate("stdseal" + ante, ante, 3);
            stdsealtypeArr[ante] = new Coordinate("stdsealtype" + ante, ante, 4);
            frontstaArr[ante] = new Coordinate("frontsta" + ante, ante, 5);
            soul_SpectralArr[ante] = new Coordinate("soul_Spectral" + ante, ante, 6);
            soul_PlanetArr[ante] = new Coordinate("soul_Planet" + ante, ante, 7);
            soul_TarotArr[ante] = new Coordinate("soul_Tarot" + ante, ante, 8);
            cdtArr[ante] = new Coordinate("cdt" + ante, ante, 9);
            VoucherArr[ante] = new Coordinate("Voucher" + ante, ante, 10);
            TagArr[ante] = new Coordinate("Tag" + ante, ante, 11);
            shop_packArr[ante] = new Coordinate("shop_pack" + ante, ante, 12);
            ssjrArr[ante] = new Coordinate("ssjr" + ante, ante, 13);
            ssjpArr[ante] = new Coordinate("ssjp" + ante, ante, 14);
            stake_shop_joker_eternalArr[ante] = new Coordinate("stake_shop_joker_eternal" + ante, ante, 15);
            packetperArr[ante] = new Coordinate("packetper" + ante, ante, 16);
            etperpollArr[ante] = new Coordinate("etperpoll" + ante, ante, 17);
            packssjrArr[ante] = new Coordinate("packssjr" + ante, ante, 18);

            planetShoArr[ante] = new Coordinate("Planetsho" + ante, ante, 19);
            planetpl1lArr[ante] = new Coordinate("Planetpl1" + ante, ante, 20);

            tarotShoArr[ante] = new Coordinate("Tarotsho" + ante, ante, 21);
            tarotarArr[ante] = new Coordinate("Tarotar" + ante, ante, 22);
            tarotAr1Arr[ante] = new Coordinate("Tarotar1" + ante, ante, 23);

            spectralShoArr[ante] = new Coordinate("Spectralsho" + ante, ante, 24);
            spectralAr2Arr[ante] = new Coordinate("Spectralar2" + ante, ante, 25);
            spectralSpeArr[ante] = new Coordinate("Spectralspe" + ante, ante, 26);

            rarityShoArr[ante] = new Coordinate("rarity" + ante + "sho", ante, 27);
            rarityBufArr[ante] = new Coordinate("rarity" + ante + "buf", ante, 28);
            raritySouArr[ante] = new Coordinate("rarity" + ante + "sou", ante, 29);

            editionShoArr[ante] = new Coordinate("edisho" + ante, ante, 30);
            editionBufArr[ante] = new Coordinate("edibuf" + ante, ante, 31);
            editionSouArr[ante] = new Coordinate("edisou" + ante, ante, 32);

            joker4ShoArr[ante] = new Coordinate("Joker4sho" + ante, ante, 33);
            joker4BufArr[ante] = new Coordinate("Joker4buf" + ante, ante, 34);
            joker4SouArr[ante] = new Coordinate("Joker4sou" + ante, ante, 35);

            joker3ShoArr[ante] = new Coordinate("Joker3sho" + ante, ante, 36);
            joker3BufArr[ante] = new Coordinate("Joker3buf" + ante, ante, 37);
            joker3SouArr[ante] = new Coordinate("Joker3sou" + ante, ante, 38);

            joker2ShoArr[ante] = new Coordinate("Joker2sho" + ante, ante, 39);
            joker2BufArr[ante] = new Coordinate("Joker2buf" + ante, ante, 40);
            joker2SouArr[ante] = new Coordinate("Joker2sou" + ante, ante, 41);

            joker1ShoArr[ante] = new Coordinate("Joker1sho" + ante, ante, 42);
            joker1BufArr[ante] = new Coordinate("Joker1buf" + ante, ante, 43);
            joker1SouArr[ante] = new Coordinate("Joker1sou" + ante, ante, 44);
        }

        System.out.println("Heating to: " + max + " took: " + (System.currentTimeMillis() - init) + "ms");
    }


    @Contract("_ -> new")
    public com.balatro.structs.@NotNull Card nextStandardCard(int ante) {
        // Enhancement
        Enhancement enhancement = null;

        if (random(stdsetArr[ante]) > 0.6) {
            enhancement = randchoice(enhancedstaArr[ante], ENHANCEMENTS);
        }

        // Edition
        var edition = Edition.NoEdition;

        double editionPoll = random(standard_editionArr[ante]);

        if (editionPoll > 0.988) {
            edition = Edition.Polychrome;
        } else if (editionPoll > 0.96) {
            edition = Edition.Polychrome;
        } else if (editionPoll > 0.92) {
            edition = Edition.Foil;
        }

        // Seal
        var seal = Seal.NoSeal;

        if (random(stdsealArr[ante]) > 0.8) {
            double sealPoll = random(stdsealtypeArr[ante]);
            if (sealPoll > 0.75) {
                seal = Seal.RedSeal;
            } else if (sealPoll > 0.5) {
                seal = Seal.BlueSeal;
            } else if (sealPoll > 0.25) {
                seal = Seal.GoldSeal;
            } else {
                seal = Seal.PurpleSeal;
            }
        }

        return new com.balatro.structs.Card(randchoice(frontstaArr[ante], CARDS), enhancement, edition, seal);
    }

    public @NotNull Item @NotNull [] nextArcanaPack(int size, int ante) {
        var pack = new Item[size];

        for (int i = 0; i < size; i++) {
            if (isVoucherActive(Voucher.Omen_Globe) && random(omen_globe) > 0.8) {
                pack[i] = nextSpectral(spectralAr2Arr[ante], ante, true);
            } else {
                pack[i] = nextTarot(tarotAr1Arr[ante], ante, true);
            }
            if (!params.isShowman()) {
                lock(pack[i]);
            }
        }

        if (params.isShowman()) return pack;

        for (Item item : pack) {
            if (item instanceof EditionItem) {
                unlock(Specials.THE_SOUL);
                continue;
            }
            unlock(item);
        }

        return pack;
    }

    public @NotNull Item @NotNull [] nextCelestialPack(int size, int ante) {
        var pack = new Item[size];

        for (int i = 0; i < size; i++) {
            pack[i] = nextPlanet(planetpl1lArr[ante], ante, true);
            if (!params.isShowman()) {
                lock(pack[i]);
            }
        }

        if (params.isShowman()) return pack;

        for (Item item : pack) {
            unlock(item);
        }

        return pack;
    }

    public @NotNull Item @NotNull [] nextSpectralPack(int size, int ante) {
        var pack = new Item[size];

        for (int i = 0; i < size; i++) {
            pack[i] = nextSpectral(spectralSpeArr[ante], ante, true);

            if (!params.isShowman()) {
                lock(pack[i]);
            }
        }

        if (params.isShowman()) return pack;

        for (Item item : pack) {
            if (item instanceof EditionItem) {
                continue;
            }

            unlock(item);
        }

        return pack;
    }

    public @NotNull com.balatro.structs.Card @NotNull [] nextStandardPack(int size, int ante) {
        var pack = new com.balatro.structs.Card[size];

        for (int i = 0; i < size; i++) {
            pack[i] = nextStandardCard(ante);
        }

        return pack;
    }

    public @NotNull JokerData @NotNull [] nextBuffoonPack(int size, int ante) {
        var pack = new JokerData[size];

        JokerData joker;

        for (int i = 0; i < size; i++) {
            joker = nextJoker("buf", joker1BufArr, joker2BufArr, joker3BufArr, joker4BufArr, rarityBufArr, editionBufArr, ante, true);
            pack[i] = joker;

            if (!params.isShowman()) {
                lock(joker.getJoker());
            }
        }

        if (params.isShowman()) return pack;

        for (JokerData jokerData : pack) {
            unlock(jokerData.getJoker());
        }

        return pack;
    }

    // Misc methods
    public boolean isVoucherActive(@NotNull Voucher voucher) {
        return params.isVoucherActive(voucher);
    }

    public void activateVoucher(Voucher voucher) {
        params.activateVoucher(voucher);
        lock(voucher);
        // Unlock next level voucher
        for (int i = voucher.ordinal(); i < VOUCHERS.length; i += 2) {
            if (VOUCHERS[i].equals(voucher)) {
                unlock(VOUCHERS[i + 1]);
            }
        }
    }

    public Voucher nextVoucher(int ante) {
        return randchoice(VoucherArr[ante], VOUCHERS);
    }

    public void setDeck(Deck deck) {
        params.setDeck(deck);
        switch (deck) {
            case MAGIC_DECK:
                activateVoucher(Voucher.Crystal_Ball);
                break;
            case NEBULA_DECK:
                activateVoucher(Voucher.Telescope);
                break;
            case ZODIAC_DECK:
                activateVoucher(Voucher.Tarot_Merchant);
                activateVoucher(Voucher.Planet_Merchant);
                activateVoucher(Voucher.Overstock);
                break;
            default:
                break;
        }
    }

    public Tag nextTag(int ante) {
        return randchoice(TagArr[ante], TAGS);
    }

    public Boss nextBoss(int ante) {
        List<Boss> bossPool = new ArrayList<>();
        int numBosses = 0;

        // First pass: Try to find unlocked bosses
        for (Boss boss : BOSSES) {
            if (isLocked(boss)) continue;

            if (ante % 8 == 0 && boss.notT()) {
                bossPool.add(boss);
                numBosses++;
            } else if (boss.isT()) {
                bossPool.add(boss);
                numBosses++;
            }
        }

        // If no bosses found, unlock appropriate bosses and try again
        if (numBosses == 0) {
            for (Boss boss : BOSSES) {
                if (ante % 8 == 0 && boss.notT()) {
                    unlock(boss);
                } else if (boss.isT()) {
                    unlock(boss);
                }
            }
            return nextBoss(ante);
        }

        var chosenBoss = _randchoice(boss, bossPool);
        lock(chosenBoss);
        return chosenBoss;
    }

    @SuppressWarnings({"SameParameterValue", "unchecked"})
    private <T extends Item> T _randchoice(Coordinate id, @NotNull List<T> items) {
        T item = items.get(randint(id, items.size() - 1));

        if (isLocked(item)) {
            return resample(id, (T[]) items.toArray());
        }

        return item;
    }

    public <T extends Item> T randchoice(Coordinate id, @NotNull T @NotNull [] items) {
        T item = items[randint(id, items.length - 1)];

        if (params.isShowman()) return item;

        if (isLocked(item)) {
            return resample(id, items);
        }
        return item;
    }

    public Item resample(@NotNull JokerData jokerData) {
        return resample(jokerData.getCoordinate(), jokerData.getItems());
    }

    private <T extends Item> T resample(@NotNull Coordinate id, @NotNull T @NotNull [] items) {
        T item;

        int resample = 2;
        while (true) {
            item = items[randintResample(id.resample(resample), items.length - 1)];
            resample++;
            if (!isLocked(item) || resample > 1000) {
                return item;
            }
        }
    }

    @Override
    public void unlock(Item item) {
        lock.unlock(item);
    }

    @Override
    public void lock(@NotNull Item item) {
        lock.lock(item);
    }

    @Override
    public boolean isLocked(@NotNull Item item) {
        return lock.isLocked(item);
    }

    @Override
    public void initUnlocks(int ante, boolean freshProfile) {
        lock.initUnlocks(ante, freshProfile);
    }

    @Override
    public void initLocks(int ante, boolean freshProfile, boolean freshRun) {
        lock.initLocks(ante, freshProfile, freshRun);
    }

    @Override
    public void firstLock() {
        lock.firstLock();
    }

    public int getShopWindow() {
        int size = 2;

        if (lock.isLocked(Voucher.Overstock)) {
            size++;
        }

        if (lock.isLocked(Voucher.Overstock_Plus)) {
            size++;
        }

        return size;
    }

    public Edition nextWheelOfFortune() {
        //1 / 4
        if (random(wheel_of_fortune) > 0.25) {
            return pollEdition(wheel_of_fortune, null, true, true);
        }

        return Edition.NoEdition;
    }

    private Edition getEdition(int ante, Coordinate[] editionArr) {
        // Get edition
        var editionRate = getEditionRate();
        var edition = Edition.NoEdition;
        var editionPoll = random(editionArr[ante]);

        if (editionPoll > 0.997) {
            edition = Edition.Negative;
        } else if (editionPoll > 1 - 0.006 * editionRate) {
            edition = Edition.Polychrome;
        } else if (editionPoll > 1 - 0.02 * editionRate) {
            edition = Edition.Holographic;
        } else if (editionPoll > 1 - 0.04 * editionRate) {
            edition = Edition.Foil;
        }

        return edition;
    }

    public int getEditionRate() {
        int editionRate = 1;

        if (isVoucherActive(Voucher.Glow_Up)) {
            editionRate = 4;
        } else if (isVoucherActive(Voucher.Hone)) {
            editionRate = 2;
        }

        return editionRate;
    }

    public Edition pollEdition(Coordinate coordinate, Double modifier, boolean noNegative, boolean guaranteed) {
        double editionPoll = random(coordinate);
        var editionRate = getEditionRate();

        if (guaranteed) {
            if (editionPoll > 1 - 0.003 * 25 && !noNegative) {
                return Edition.Polychrome;
            } else if (editionPoll > 1 - 0.006 * 25) {
                return Edition.Polychrome;
            } else if (editionPoll > 1 - 0.02 * 25) {
                return Edition.Holographic;
            } else if (editionPoll > 1 - 0.04 * 25) {
                return Edition.Foil;
            }
        } else {
            if (editionPoll > 1 - multi(0.003, modifier) && !noNegative) {
                return Edition.Negative;
            } else if (editionPoll > 1 - 0.006 * multi(editionRate, modifier)) {
                return Edition.Polychrome;
            } else if (editionPoll > 1 - 0.02 * multi(editionRate, modifier)) {
                return Edition.Holographic;
            } else if (editionPoll > 1 - 0.04 * multi(editionRate, modifier)) {
                return Edition.Foil;
            }
        }

        return null;
    }

    private double multi(double editionRate, Double mod) {
        if (mod == null) return editionRate;

        return editionRate * mod;
    }
}

