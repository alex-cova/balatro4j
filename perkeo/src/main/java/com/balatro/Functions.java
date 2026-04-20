package com.balatro;

import com.balatro.api.Cache;
import com.balatro.api.Item;
import com.balatro.api.Lock;
import com.balatro.enums.*;
import com.balatro.enums.Card;
import com.balatro.structs.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import static com.balatro.Util.pseudohash;
import static com.balatro.Util.round13;

public final class Functions implements Lock {

    public static final Tarot[] TAROTS = Tarot.values();
    public static final Planet[] PLANETS = Planet.values();
    public static final Spectral[] SPECTRALS = Spectral.values();
    public static final LegendaryJoker[] LEGENDARY_JOKERS = LegendaryJoker.values();
    public static final UnCommonJoker[] UNCOMMON_JOKERS = UnCommonJoker.values();
    public static final Card[] CARDS = Card.values();
    public static final Enhancement[] ENHANCEMENTS = Enhancement.values();
    public static final Voucher[] VOUCHERS = Voucher.values();
    public static final Tag[] TAGS = Tag.values();
    public static final PackType[] PACKS = PackType.values();
    public static final RareJoker[] RARE_JOKERS = RareJoker.values();
    public static final CommonJoker[] COMMON_JOKERS = CommonJoker.values();
    public static final Boss[] BOSSES = Boss.values();

    private static final double[] PACK_WEIGHTS;

    // Source classification — avoids repeated String.equals() in nextJoker's hot path.
    private static final int SRC_OTHER = 0;
    private static final int SRC_SOU = 1;
    private static final int SRC_WRA_OR_RTA = 2;
    private static final int SRC_UTA = 3;
    private static final int SRC_BUF = 4;

    // Stakes that enable sticker search — packed into an int bitmask indexed by Stake.ordinal().
    private static final int STICKER_STAKE_MASK =
            (1 << Stake.Black_Stake.ordinal())
          | (1 << Stake.Blue_Stake.ordinal())
          | (1 << Stake.Purple_Stake.ordinal())
          | (1 << Stake.Orange_Stake.ordinal())
          | (1 << Stake.Gold_Stake.ordinal());

    private static final int ORANGE_OR_GOLD_MASK =
            (1 << Stake.Orange_Stake.ordinal()) | (1 << Stake.Gold_Stake.ordinal());

    // Per-rarity bit masks of setA/setB membership. Index = rarity (1=common, 2=uncommon, 3=rare, 4=legendary).
    // Replaces HashSet<String> + joker.getName() lookups with a single AND/shift.
    private static final long[] SETA_MASK = new long[5];
    private static final long[] SETB_MASK = new long[5];

    private static final Set<String> SETA_NAMES = Set.of(
            "Gros Michel", "Ice Cream", "Cavendish", "Luchador", "Turtle Bean", "Diet Cola",
            "Popcorn", "Ramen", "Seltzer", "Mr. Bones", "Invisible Joker");

    private static final Set<String> SETB_NAMES = Set.of(
            "Ceremonial Dagger", "Ride the Bus", "Runner", "Constellation", "Green Joker",
            "Red Card", "Madness", "Square Joker", "Vampire", "Rocket", "Obelisk", "Lucky Cat",
            "Flash Card", "Spare Trousers", "Castle", "Wee Joker");

    // Kept for backwards compatibility with any external reference; no longer used internally.
    static final Set<String> setA = SETA_NAMES;
    static final Set<String> setB = SETB_NAMES;

    private final InstanceParams params;
    private final Cache cache;
    public final byte[] seed;
    public final double hashedSeed;
    private final Lock lock;
    private ShopInstance cachedShopInstance;

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

    /**
     * Long-keyed variant of randintResample that avoids String allocation.
     * Only falls back to String creation for the pseudohash on cache miss.
     */
    private int randintResample(Coordinate coord, int resampleCount, int max) {
        final long key = coord.resampleKey(resampleCount);
        // Primitive getter avoids boxing — NaN sentinel signals cache miss since cached values
        // always come from a (% 1) operation and are finite.
        final double cached = cache.getResampleOrNaN(key);

        final double c = Double.isNaN(cached)
                ? pseudohash(coord.resample(resampleCount).getBytes(), seed)
                : cached;

        final double value = round13((c * 1.72431234 + 2.134453429141) % 1);
        cache.putResample(key, value);

        return LuaRandom.randint((value + hashedSeed) / 2, max);
    }

    private double getNode(Coordinate id) {
        double c = cache.get(id);

        if (c == 0.0) {
            c = id.pseudohash(seed);
            // Skip the intermediate put — it would be overwritten by the put below anyway.
        }

        final double value = round13((c * 1.72431234 + 2.134453429141) % 1);
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
        final double poll = random(ID) * 22.42;
        int idx = 0;
        double weight = 0;

        // Fast path for the canonical PACKS array — avoids the virtual getValue() call per iteration.
        if (items == PACKS) {
            final double[] w = PACK_WEIGHTS;
            while (weight < poll) {
                weight += w[idx++];
            }
        } else {
            while (weight < poll) {
                weight += items[idx++].getValue();
            }
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

    public JokerData nextJoker(@NotNull String source,
                               Coordinate[] joker1Arr, Coordinate[] joker2Arr, Coordinate[] joker3Arr, Coordinate[] joker4Arr,
                               Coordinate[] rarityArr, Coordinate[] editionArr,
                               int ante, boolean hasStickers) {
        // Classify source once — subsequent dispatches become int comparisons instead of String.equals.
        final int srcType = sourceType(source);

        int rarity = 1;
        switch (srcType) {
            case SRC_SOU -> rarity = 4;
            case SRC_WRA_OR_RTA -> rarity = 3;
            case SRC_UTA -> rarity = 2;
            default -> {
                double rarityPoll = random(rarityArr[ante]);
                if (rarityPoll > 0.95) {
                    rarity = 3;
                } else if (rarityPoll > 0.7) {
                    rarity = 2;
                }
            }
        }

        final Edition edition = getEdition(ante, editionArr);

        final Coordinate coordinate;
        final Item[] items;
        switch (rarity) {
            case 4 -> { items = LEGENDARY_JOKERS; coordinate = Joker4; }
            case 3 -> { coordinate = joker3Arr[ante]; items = RARE_JOKERS; }
            case 2 -> { coordinate = joker2Arr[ante]; items = UNCOMMON_JOKERS; }
            default -> { coordinate = joker1Arr[ante]; items = COMMON_JOKERS; }
        }

        final Item joker = randchoice(coordinate, items);
        final JokerStickers stickers = new JokerStickers();

        if (hasStickers) {
            final Stake stake = params.getStake();
            final int stakeBit = 1 << stake.ordinal();
            final boolean searchForSticker = (STICKER_STAKE_MASK & stakeBit) != 0;

            double stickerPoll = 0.0;
            if (searchForSticker) {
                stickerPoll = (srcType == SRC_BUF)
                        ? random(packetperArr[ante])
                        : random(etperpollArr[ante]);
            }

            final long jokerBit = 1L << joker.ordinal();

            if (stickerPoll > 0.7) {
                if ((SETA_MASK[rarity] & jokerBit) == 0) {
                    stickers.setRental(true);
                }
            } else if (stickerPoll > 0.4 && (ORANGE_OR_GOLD_MASK & stakeBit) != 0) {
                if ((SETB_MASK[rarity] & jokerBit) == 0) {
                    stickers.setPerishable(true);
                }
            }

            if (stake == Stake.Gold_Stake) {
                final double poll = (srcType == SRC_BUF)
                        ? random(packssjrArr[ante])
                        : random(ssjrArr[ante]);
                stickers.setRental(poll > 0.7);
            }
        }

        return new JokerData(joker, rarity, edition, stickers)
                .setResampleInfo(coordinate, items);
    }

    private static int sourceType(String source) {
        return switch (source) {
            case "sou" -> SRC_SOU;
            case "wra", "rta" -> SRC_WRA_OR_RTA;
            case "uta" -> SRC_UTA;
            case "buf" -> SRC_BUF;
            default -> SRC_OTHER;
        };
    }

    // Shop Logic
    /**
     * Returns the ShopInstance for the current voucher state.
     * Cached to avoid redundant recomputation within the same ante.
     * Invalidated when vouchers are activated.
     */
    public @NotNull ShopInstance getShopInstance() {
        if (cachedShopInstance != null) return cachedShopInstance;

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

        cachedShopInstance = new ShopInstance(20, tarotRate, planetRate, playingCardRate, spectralRate);
        return cachedShopInstance;
    }

    @Contract("_ -> new")
    public @NotNull ShopItem nextShopItem(int ante) {
        final ShopInstance shop = getShopInstance();

        double cdtPoll = random(cdtArr[ante]) * shop.getTotalRate();
        final Type type;

        if (cdtPoll < shop.jokerRate()) {
            type = Type.Joker;
        } else {
            cdtPoll -= shop.jokerRate();
            if (cdtPoll < shop.tarotRate()) {
                type = Type.Tarot;
            } else {
                cdtPoll -= shop.tarotRate();
                if (cdtPoll < shop.planetRate()) {
                    type = Type.Planet;
                } else {
                    cdtPoll -= shop.planetRate();
                    type = (cdtPoll < shop.playingCardRate()) ? Type.PlayingCard : Type.Spectral;
                }
            }
        }

        return switch (type) {
            case Joker -> {
                var jkr = nextJoker("sho", joker1ShoArr, joker2ShoArr, joker3ShoArr, joker4ShoArr,
                        rarityShoArr, editionShoArr, ante, true);
                yield new ShopItem(type, jkr.joker, jkr);
            }
            case Tarot -> new ShopItem(type, nextTarot(tarotShoArr[ante], ante, false));
            case Planet -> new ShopItem(type, nextPlanet(planetShoArr[ante], ante, false));
            case Spectral -> new ShopItem(type, nextSpectral(spectralShoArr[ante], ante, false));
            case PlayingCard -> new ShopItem(type, nextStandardCard(ante));
        };
    }

    public PackType nextPack(int ante) {
        if (ante <= 2 && !cache.isGeneratedFirstPack()) {
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

        // Cache PackType weights in a primitive array to skip enum virtual dispatch in randweightedchoice.
        PACK_WEIGHTS = new double[PACKS.length];
        for (int i = 0; i < PACKS.length; i++) {
            PACK_WEIGHTS[i] = PACKS[i].getValue();
        }

        // Build rarity-indexed bit masks once from the name-based sets.
        buildSetMask(1, COMMON_JOKERS);
        buildSetMask(2, UNCOMMON_JOKERS);
        buildSetMask(3, RARE_JOKERS);
        buildSetMask(4, LEGENDARY_JOKERS);
    }

    private static void buildSetMask(int rarity, Item[] items) {
        long a = 0L, b = 0L;
        for (int i = 0; i < items.length; i++) {
            final String n = items[i].getName();
            if (SETA_NAMES.contains(n)) a |= (1L << i);
            if (SETB_NAMES.contains(n)) b |= (1L << i);
        }
        SETA_MASK[rarity] = a;
        SETB_MASK[rarity] = b;
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
        Edition edition = Edition.NoEdition;
        final double editionPoll = random(standard_editionArr[ante]);

        if (editionPoll > 0.988) {
            edition = Edition.Polychrome;
        } else if (editionPoll > 0.96) {
            edition = Edition.Holographic;
        } else if (editionPoll > 0.92) {
            edition = Edition.Foil;
        }

        // Seal
        Seal seal = Seal.NoSeal;

        if (random(stdsealArr[ante]) > 0.8) {
            final double sealPoll = random(stdsealtypeArr[ante]);
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
        final Item[] pack = new Item[size];
        final boolean showman = params.isShowman();
        final boolean omenGlobeActive = isVoucherActive(Voucher.Omen_Globe);

        for (int i = 0; i < size; i++) {
            if (omenGlobeActive && random(omen_globe) > 0.8) {
                pack[i] = nextSpectral(spectralAr2Arr[ante], ante, true);
            } else {
                pack[i] = nextTarot(tarotAr1Arr[ante], ante, true);
            }
            if (!showman) {
                lock(pack[i]);
            }
        }

        if (showman) return pack;

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
        final Item[] pack = new Item[size];
        final boolean showman = params.isShowman();

        for (int i = 0; i < size; i++) {
            pack[i] = nextPlanet(planetpl1lArr[ante], ante, true);
            if (!showman) {
                lock(pack[i]);
            }
        }

        if (showman) return pack;

        for (Item item : pack) {
            unlock(item);
        }

        return pack;
    }

    public @NotNull Item @NotNull [] nextSpectralPack(int size, int ante) {
        final Item[] pack = new Item[size];
        final boolean showman = params.isShowman();

        for (int i = 0; i < size; i++) {
            pack[i] = nextSpectral(spectralSpeArr[ante], ante, true);
            if (!showman) {
                lock(pack[i]);
            }
        }

        if (showman) return pack;

        for (Item item : pack) {
            if (item instanceof EditionItem) {
                continue;
            }
            unlock(item);
        }

        return pack;
    }

    public @NotNull com.balatro.structs.Card @NotNull [] nextStandardPack(int size, int ante) {
        final com.balatro.structs.Card[] pack = new com.balatro.structs.Card[size];

        for (int i = 0; i < size; i++) {
            pack[i] = nextStandardCard(ante);
        }

        return pack;
    }

    public @NotNull JokerData @NotNull [] nextBuffoonPack(int size, int ante) {
        final JokerData[] pack = new JokerData[size];
        final boolean showman = params.isShowman();

        for (int i = 0; i < size; i++) {
            final JokerData joker = nextJoker("buf", joker1BufArr, joker2BufArr, joker3BufArr, joker4BufArr,
                    rarityBufArr, editionBufArr, ante, true);
            pack[i] = joker;

            if (!showman) {
                lock(joker.getJoker());
            }
        }

        if (showman) return pack;

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
        cachedShopInstance = null; // invalidate cache since voucher state changed
        // Vouchers come in pairs: base at even ordinal, upgrade at odd.
        int ord = voucher.ordinal();
        if (ord % 2 == 0 && ord + 1 < VOUCHERS.length) {
            unlock(VOUCHERS[ord + 1]);
        }
    }

    public @NotNull Voucher nextVoucher(int ante) {
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

    public @NotNull Tag nextTag(int ante) {
        return randchoice(TagArr[ante], TAGS);
    }

    private final Boss[] bossBuffer = new Boss[BOSSES.length];

    public Boss nextBoss(int ante) {
        final boolean isBossAnte = (ante % 8 == 0);
        int numBosses = 0;

        // Iterative: find unlocked bosses; if none match, unlock the class and retry.
        while (true) {
            for (Boss b : BOSSES) {
                if (isLocked(b)) continue;

                if (isBossAnte ? b.notT() : b.isT()) {
                    bossBuffer[numBosses++] = b;
                }
            }

            if (numBosses > 0) break;

            for (Boss b : BOSSES) {
                if (isBossAnte ? b.notT() : b.isT()) {
                    unlock(b);
                }
            }
        }

        Boss chosenBoss = bossBuffer[randint(boss, numBosses - 1)];

        if (isLocked(chosenBoss)) {
            chosenBoss = resampleBoss(numBosses);
        }

        lock(chosenBoss);
        return chosenBoss;
    }

    private Boss resampleBoss(int poolSize) {
        int resampleCount = 2;
        while (true) {
            Boss b = bossBuffer[randintResample(boss, resampleCount, poolSize - 1)];
            resampleCount++;
            if (!isLocked(b) || resampleCount > 1000) {
                return b;
            }
        }
    }

    public <T extends Item> @NotNull T randchoice(Coordinate id, @NotNull T @NotNull [] items) {
        final T item = items[randint(id, items.length - 1)];

        if (item.isRetry()) {
            return resample(id, items);
        }

        if (!params.isShowman() && isLocked(item)) {
            return resample(id, items);
        }

        return item;
    }

    public @NotNull Item resample(@NotNull JokerData jokerData) {
        return resample(jokerData.getCoordinate(), jokerData.getItems());
    }

    private <T extends Item> @NotNull T resample(@NotNull Coordinate id, @NotNull T @NotNull [] items) {
        int resampleCount = 2;
        while (true) {
            final T item = items[randintResample(id, resampleCount, items.length - 1)];
            resampleCount++;
            if ((!item.isRetry() && !isLocked(item)) || resampleCount > 1000) {
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

    public Edition getEdition(int ante, Coordinate[] editionArr) {
        final int editionRate = getEditionRate();
        final double editionPoll = random(editionArr[ante]);

        if (editionPoll > 0.997) return Edition.Negative;
        if (editionPoll > 1.0 - 0.006 * editionRate) return Edition.Polychrome;
        if (editionPoll > 1.0 - 0.02 * editionRate) return Edition.Holographic;
        if (editionPoll > 1.0 - 0.04 * editionRate) return Edition.Foil;

        return Edition.NoEdition;
    }

    public int getEditionRate() {
        if (isVoucherActive(Voucher.Glow_Up)) return 4;
        if (isVoucherActive(Voucher.Hone)) return 2;
        return 1;
    }

    public @Nullable Edition pollEdition(Coordinate coordinate, Double modifier, boolean noNegative, boolean guaranteed) {
        final double editionPoll = random(coordinate);

        if (guaranteed) {
            // Preserve original thresholds exactly (0.925 / 0.85 / 0.5 / 0.0 — the "25x" guaranteed multiplier).
            if (editionPoll > 0.925 && !noNegative) return Edition.Polychrome;
            if (editionPoll > 0.85) return Edition.Polychrome;
            if (editionPoll > 0.5) return Edition.Holographic;
            if (editionPoll > 0.0) return Edition.Foil;
            return null;
        }

        final int editionRate = getEditionRate();
        // Hoist the modifier null-check out of the multi() calls.
        final double modOr1 = (modifier == null) ? 1.0 : modifier;
        final double rateMod = editionRate * modOr1;

        if (editionPoll > 1.0 - 0.003 * modOr1 && !noNegative) return Edition.Negative;
        if (editionPoll > 1.0 - 0.006 * rateMod) return Edition.Polychrome;
        if (editionPoll > 1.0 - 0.02 * rateMod) return Edition.Holographic;
        if (editionPoll > 1.0 - 0.04 * rateMod) return Edition.Foil;

        return null;
    }
}
