package com.balatro;

import com.balatro.api.AbstractCard;
import com.balatro.api.Balatro;
import com.balatro.api.Item;
import com.balatro.api.Run;
import com.balatro.enums.*;
import com.balatro.structs.*;
import com.balatro.structs.Card;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class BalatroImpl implements Balatro {

    public static final List<? extends Item> options = Arrays.asList(
            CommonJoker.Golden_Ticket,
            CommonJoker.Hanging_Chad,
            CommonJoker.Shoot_the_Moo,
            CommonJoker.Swashbuckler,
            RareJoker.Blueprint,
            RareJoker.Brainstorm,
            RareJoker.Burnt_Joke,
            RareJoker.Drivers_License,
            RareJoker.Hit_the_Road,
            RareJoker.Invisible_Joker,
            RareJoker.Stuntman,
            RareJoker.The_Duo,
            RareJoker.The_Family,
            RareJoker.The_Order,
            RareJoker.The_Tribe,
            RareJoker.The_Trio,
            RareJoker.Wee_Joker,
            Tag.Foil_Tag,
            Tag.Holographic_Tag,
            Tag.Negative_Tag,
            Tag.Polychrome_Tag,
            Tag.Rare_Tag,
            UnCommonJoker.Acrobat,
            UnCommonJoker.Arrowhead,
            UnCommonJoker.Astronomer,
            UnCommonJoker.Bloodstone,
            UnCommonJoker.Cartomancer,
            UnCommonJoker.Certificate,
            UnCommonJoker.Flower_Pot,
            UnCommonJoker.Glass_Joker,
            UnCommonJoker.Matador,
            UnCommonJoker.Merry_Andy,
            UnCommonJoker.Mr_Bones,
            UnCommonJoker.Onyx_Agate,
            UnCommonJoker.Oops_All_6s,
            UnCommonJoker.Rough_Gem,
            UnCommonJoker.Satellite,
            UnCommonJoker.Seeing_Double,
            UnCommonJoker.Showman,
            UnCommonJoker.Smeared_Joker,
            UnCommonJoker.Sock_and_Buskin,
            UnCommonJoker.The_Idol,
            UnCommonJoker.Throwback,
            UnCommonJoker.Troubadour,
            UnCommonJoker100.Bootstraps,
            Voucher.Antimatter,
            Voucher.Glow_Up,
            Voucher.Illusion,
            Voucher.Liquidation,
            Voucher.Money_Tree,
            Voucher.Nacho_Tong,
            Voucher.Observatory,
            Voucher.Omen_Globe,
            Voucher.Overstock_Plus,
            Voucher.Palett,
            Voucher.Petroglyph,
            Voucher.Planet_Tycoon,
            Voucher.Recyclomancy,
            Voucher.Reroll_Glut,
            Voucher.Retcon,
            Voucher.Tarot_Tycoon
    );

    static final char[] CHARACTERS = {'1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    public static byte[] generateRandomSeed() {
        byte[] chars = new byte[8];
        for (int i = 0; i < 8; i++) {
            int index = ThreadLocalRandom.current().nextInt(CHARACTERS.length);
            chars[i] = (byte) CHARACTERS[index];
        }
        return chars;
    }

    private final byte[] seed;
    private int maxAnte;
    private List<Integer> cardsPerAnte;
    private Deck deck;
    private Stake stake;
    private Version version;
    private boolean analyzeStandardPacks;
    private boolean analyzeCelestialPacks;
    private boolean analyzeTags;
    private boolean analyzeBoss;
    private boolean analyzeVoucher;
    private boolean analyzeShopQueue;
    private boolean analyzeJokers;
    private boolean analyzeArcana;
    private boolean analyzeSpectral;
    private boolean freshProfile;
    private boolean freshRun = true;
    private boolean showman;


    public BalatroImpl(byte[] seed, int maxAnte, List<Integer> cardsPerAnte, Deck deck, Stake stake, Version version,
                       @NotNull Set<PackKind> enabledPacks, boolean analyzeTags, boolean analyzeBoss, boolean analyzeShopQueue) {
        this.seed = seed;
        this.maxAnte = maxAnte;
        this.cardsPerAnte = cardsPerAnte;
        this.deck = deck;
        this.stake = stake;
        this.version = version;
        this.analyzeStandardPacks = enabledPacks.contains(PackKind.Standard);
        this.analyzeCelestialPacks = enabledPacks.contains(PackKind.Celestial);
        this.analyzeArcana = enabledPacks.contains(PackKind.Arcana);
        this.analyzeSpectral = enabledPacks.contains(PackKind.Spectral);
        this.analyzeJokers = enabledPacks.contains(PackKind.Buffoon);
        this.analyzeTags = analyzeTags;
        this.analyzeBoss = analyzeBoss;
        this.analyzeShopQueue = analyzeShopQueue;
    }

    @Override
    public Run analyze() {
        return performAnalysis(seed, maxAnte, cardsPerAnte, deck, stake, version);
    }

    @Contract("_, _, _, _, _, _ -> new")
    private @NotNull RunImpl performAnalysis(byte[] seed, int maxAnte, @NotNull List<Integer> cardsPerAnte, Deck deck, Stake stake, @NotNull Version version) {
        if (cardsPerAnte.size() != maxAnte) {
            throw new IllegalArgumentException("cardsPerAnte must have the same size as maxAnte (%s - %s)".formatted(maxAnte, cardsPerAnte.size()));
        }

        Functions functions = new Functions(seed, maxAnte, new InstanceParams(deck, stake, showman, version));
        functions.initLocks(1, freshProfile, freshRun);
        functions.firstLock();
        functions.setDeck(deck);
        var antes = new ArrayList<AnteImpl>(options.size());

        for (int a = 1; a <= maxAnte; a++) {
            functions.initUnlocks(a, freshProfile);
            var play = new AnteImpl(a);
            antes.add(play);

            if (analyzeBoss) {
                play.setBoss(functions.nextBoss(a));
            }

            if (analyzeVoucher) {
                var voucher = functions.nextVoucher(a);
                play.setVoucher(voucher);

                functions.lock(voucher);
                // Unlock next level voucher
                for (int i = 0; i < Functions.VOUCHERS.length; i += 2) {
                    if (Functions.VOUCHERS[i].equals(voucher)) {
                        functions.unlock(Functions.VOUCHERS[i + 1]);
                    }
                }
            }

            if (analyzeTags) {
                play.addTag(functions.nextTag(a));
                play.addTag(functions.nextTag(a));
            }

            if (analyzeShopQueue) {
                for (int q = 1; q <= cardsPerAnte.get(a - 1); q++) {
                    play.addToQueue(functions.nextShopItem(a));
                }
            }

            int numPacks = (a == 1) ? 4 : 6;

            for (int p = 1; p <= numPacks; p++) {
                var pack = functions.nextPack(a);
                var packInfo = new PackInfo(pack);
                Set<EditionItem> options = new HashSet<>();

                switch (packInfo.getKind()) {
                    case PackKind.Celestial -> {
                        if (!analyzeCelestialPacks) continue;
                        var cards = functions.nextCelestialPack(packInfo.getSize(), a);
                        for (Item card : cards) {
                            options.add(card.asEditionItem());
                        }
                    }
                    case PackKind.Arcana -> {
                        if (!analyzeArcana) continue;
                        var cards = functions.nextArcanaPack(packInfo.getSize(), a);
                        for (Item card : cards) {
                            if (card instanceof EditionItem ei) {
                                options.add(ei);
                                continue;
                            }
                            options.add(card.asEditionItem());
                        }
                    }
                    case PackKind.Spectral -> {
                        if (!analyzeSpectral) continue;
                        var spectral = functions.nextSpectralPack(packInfo.getSize(), a);
                        for (Item card : spectral) {
                            if (card instanceof EditionItem ei) {
                                options.add(ei);
                                continue;
                            }
                            options.add(card.asEditionItem());
                        }
                    }
                    case PackKind.Buffoon -> {
                        if (!analyzeJokers) continue;
                        var cards = functions.nextBuffoonPack(packInfo.getSize(), a);

                        for (JokerData joker : cards) {
                            options.add(new EditionItem(joker.getJoker(), getSticker(joker, stake)));
                        }
                    }
                    case PackKind.Standard -> {
                        if (!analyzeStandardPacks) continue;

                        var cards = functions.nextStandardPack(packInfo.getSize(), a);

                        for (Card card : cards) {
                            StringBuilder output = new StringBuilder();

                            if (card.seal() != Seal.NoSeal) {
                                output.append(card.seal()).append(" ");
                            }
                            if (card.edition() != Edition.NoEdition) {
                                output.append(card.edition()).append(" ");
                            }
                            if (card.enhancement() != null) {
                                output.append(card.enhancement().getName()).append(" ");
                            }

                            var rank = card.base().getRank();
                            output.append(rank.getName());
                            output.append(" of ")
                                    .append(card.base().getSuit().getName());
                            options.add(new EditionItem(new AbstractCard(output.toString(), card)));
                        }
                    }
                }

                play.addPack(packInfo, options);
            }
        }

        return new RunImpl(new String(seed), Collections.unmodifiableList(antes));
    }

    private static @NotNull Edition getSticker(@NotNull JokerData joker, Stake stake) {
        if (stake == Stake.White_Stake) return joker.getEdition();

        if (joker.getStickers().isEternal()) {
            return Edition.Eternal;
        }

        if (joker.getStickers().isPerishable()) {
            return Edition.Perishable;
        }

        if (joker.getStickers().isRental()) {
            return Edition.Rental;
        }

        return joker.getEdition();
    }

    @Override
    public @NotNull @Unmodifiable Run analyzeAll() {
        enableAll();
        return analyze();
    }

    @Override
    public Balatro enableBoss() {
        analyzeBoss = true;
        return null;
    }

    @Override
    public Balatro enableShop() {
        analyzeShopQueue = true;
        return this;
    }

    @Override
    public Balatro enableSpectralPack() {
        analyzeSpectral = true;
        return this;
    }

    @Override
    public Balatro enableVouchers() {
        analyzeVoucher = true;
        return this;
    }

    @Override
    public Balatro enableJokerPack() {
        analyzeJokers = true;
        return this;
    }

    @Override
    public Balatro enableCelestialPack() {
        analyzeCelestialPacks = true;
        return this;
    }

    @Override
    public Balatro enableArcanaPack() {
        analyzeArcana = true;
        return this;
    }

    @Override
    public Balatro enableTags() {
        analyzeTags = true;
        return this;
    }

    @Override
    public void printConfigurations() {
        System.out.println("----------------------- CONFIGURATION -----------------------");
        System.out.println("Standard: " + analyzeStandardPacks);
        System.out.println("Celestial: " + analyzeCelestialPacks);
        System.out.println("Arcana: " + analyzeArcana);
        System.out.println("Spectral: " + analyzeSpectral);
        System.out.println("Jokers: " + analyzeJokers);
        System.out.println("Tags: " + analyzeTags);
        System.out.println("Boss: " + analyzeBoss);
        System.out.println("Shop: " + analyzeShopQueue);
        System.out.println("Vouchers: " + analyzeVoucher);
        System.out.println("----------------------- CONFIGURATION -----------------------");
    }

    @Override
    public Balatro disableAll() {
        analyzeStandardPacks = false;
        analyzeCelestialPacks = false;
        analyzeArcana = false;
        analyzeSpectral = false;
        analyzeJokers = false;
        analyzeTags = false;
        analyzeBoss = false;
        analyzeShopQueue = false;
        analyzeVoucher = false;
        return this;
    }

    @Override
    public Balatro enableAll() {
        analyzeStandardPacks = true;
        analyzeCelestialPacks = true;
        analyzeArcana = true;
        analyzeSpectral = true;
        analyzeJokers = true;
        analyzeTags = true;
        analyzeBoss = true;
        analyzeShopQueue = true;
        analyzeVoucher = true;
        return this;
    }

    @Override
    public Balatro maxAnte(int maxAnte) {
        this.maxAnte = maxAnte;

        if (maxAnte > 1) {
            cardsPerAnte = new ArrayList<>();
            for (int i = 0; i < this.maxAnte; i++) {
                if (i == 0) {
                    cardsPerAnte.add(15);
                    continue;
                }
                cardsPerAnte.add(50);
            }
        }

        return this;
    }

    @Override
    public Balatro disableShopQueue() {
        this.analyzeShopQueue = false;
        return this;
    }

    @Override
    public Balatro version(Version version) {
        this.version = version;
        return this;
    }

    @Override
    public Balatro deck(Deck deck) {
        this.deck = deck;
        return this;
    }

    @Override
    public Balatro stake(Stake stake) {
        this.stake = stake;
        return this;
    }

    @Override
    public Balatro freshProfile(boolean freshProfile) {
        this.freshProfile = freshProfile;
        return this;
    }

    @Override
    public Balatro freshRun(boolean freshRun) {
        this.freshRun = freshRun;
        return this;
    }

    @Override
    public Balatro showman(boolean showman) {
        this.showman = showman;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    @Override
    public Balatro disablePack(@NotNull PackKind packKind) {
        switch (packKind) {
            case Standard -> analyzeStandardPacks = false;
            case Celestial -> analyzeCelestialPacks = false;
            case Arcana -> analyzeArcana = false;
            case Spectral -> analyzeSpectral = false;
            case Buffoon -> analyzeJokers = false;
        }
        return this;
    }
}
