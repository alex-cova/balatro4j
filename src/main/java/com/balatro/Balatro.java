package com.balatro;

import com.balatro.enums.*;
import com.balatro.structs.*;
import com.balatro.structs.Card;
import com.balatro.structs.Pack;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Balatro {

    public static final List<String> options = Arrays.asList(
            "Negative Tag",
            "Foil Tag",
            "Holographic Tag",
            "Polychrome Tag",
            "Rare Tag",
            "Golden Ticket",
            "Mr. Bones",
            "Acrobat",
            "Sock and Buskin",
            "Swashbuckler",
            "Troubadour",
            "Certificate",
            "Smeared Joker",
            "Throwback",
            "Hanging Chad",
            "Rough Gem",
            "Bloodstone",
            "Arrowhead",
            "Onyx Agate",
            "Glass Joker",
            "Showman",
            "Flower Pot",
            "Blueprint",
            "Wee Joker",
            "Merry Andy",
            "Oops! All 6s",
            "The Idol",
            "Seeing Double",
            "Matador",
            "Hit the Road",
            "The Duo",
            "The Trio",
            "The Family",
            "The Order",
            "The Tribe",
            "Stuntman",
            "Invisible Joker",
            "Brainstorm",
            "Satellite",
            "Shoot the Moon",
            "Driver's License",
            "Cartomancer",
            "Astronomer",
            "Burnt Joker",
            "Bootstraps",
            "Overstock Plus",
            "Liquidation",
            "Glow Up",
            "Reroll Glut",
            "Omen Globe",
            "Observatory",
            "Nacho Tong",
            "Recyclomancy",
            "Tarot Tycoon",
            "Planet Tycoon",
            "Money Tree",
            "Antimatter",
            "Illusion",
            "Petroglyph",
            "Retcon",
            "Palette"
    );

    static final String CHARACTERS = "123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String generateRandomString() {
        StringBuilder result = new StringBuilder(7);
        for (int i = 0; i < 7; i++) {
            int index = ThreadLocalRandom.current().nextInt(CHARACTERS.length());
            result.append(CHARACTERS.charAt(index));
        }
        return result.toString();
    }

    public static void main(String[] args) {
        ForkJoinTask<?> submit = ForkJoinPool.commonPool().submit(Balatro::generate);
        ForkJoinPool.commonPool().submit(Balatro::generate);
        ForkJoinPool.commonPool().submit(Balatro::generate);
        ForkJoinPool.commonPool().submit(Balatro::generate);
        ForkJoinPool.commonPool().submit(Balatro::generate);
        ForkJoinPool.commonPool().submit(Balatro::generate);
        ForkJoinPool.commonPool().submit(Balatro::generate);
        ForkJoinPool.commonPool().submit(Balatro::generate);

        while (!submit.isDone()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static AtomicInteger count = new AtomicInteger(0);

    static void generate() {
        for (int i = 0; i < 100_000; i++) {
            var seed = generateRandomString();
            var result = new Balatro()
                    .performAnalysis(seed);

            if (result.hasLegendary(1, LegendaryJoker.Perke)  && result.hasInShop(1, RareJoker.Blueprint)) {
                System.err.println(seed);
            }
        }
    }

    public Analysis performAnalysis(String seed) {
        return performAnalysis(8, List.of(15, 50, 50, 50, 50, 50, 50, 50), Deck.RED_DECK, Stake.White_Stake, Version.v_101f, seed);
    }

    public Analysis performAnalysis(int ante, List<Integer> cardsPerAnte, Deck deck, Stake stake, Version version, String seed) {
        boolean[] selectedOptions = new boolean[61];
        Arrays.fill(selectedOptions, true);

        Functions inst = new Functions(seed);
        inst.setParams(new InstanceParams(deck, stake, false, version.getVersion()));
        inst.initLocks(1, false, false);
        inst.lock("Overstock Plus");
        inst.lock("Liquidation");
        inst.lock("Glow Up");
        inst.lock("Reroll Glut");
        inst.lock("Omen Globe");
        inst.lock("Observatory");
        inst.lock("Nacho Tong");
        inst.lock("Recyclomancy");
        inst.lock("Tarot Tycoon");
        inst.lock("Planet Tycoon");
        inst.lock("Money Tree");
        inst.lock("Antimatter");
        inst.lock("Illusion");
        inst.lock("Petroglyph");
        inst.lock("Retcon");
        inst.lock("Palette");

        for (int i = 0; i < options.size(); i++) {
            if (!selectedOptions[i]) inst.lock(options.get(i));
        }

        inst.setDeck(deck);
        var antes = new ArrayList<Ante>(options.size());

        for (int a = 1; a <= ante; a++) {
            inst.initUnlocks(a, false);
            var play = new Ante(a, inst);
            antes.add(play);
            play.setBoss(inst.nextBoss(a));
            var voucher = inst.nextVoucher(a);
            play.setVoucher(voucher);

            inst.lock(voucher);
            // Unlock next level voucher
            for (int i = 0; i < Functions.VOUCHERS.size(); i += 2) {
                if (Functions.VOUCHERS.get(i).equals(voucher)) {
                    // Only unlock it if it's unlockable
                    if (selectedOptions[options.indexOf(Functions.VOUCHERS.get(i + 1).getName())]) {
                        inst.unlock(Functions.VOUCHERS.get(i + 1).getName());
                    }
                }
            }

            play.addTag(inst.nextTag(a));
            play.addTag(inst.nextTag(a));

            for (int q = 1; q <= cardsPerAnte.get(a - 1); q++) {
                var sticker = "";

                ShopItem item = inst.nextShopItem(a);

                if (item.getType().equals("Joker")) {
                    if (item.getJokerData().getStickers().isEternal()) {
                        sticker = "Eternal ";
                    }
                    if (item.getJokerData().getStickers().isPerishable()) {
                        sticker = "Perishable ";
                    }
                    if (item.getJokerData().getStickers().isRental()) {
                        sticker = "Rental ";
                    }
                    if (!item.getJokerData().getEdition().equals("No Edition")) {
                        sticker = item.getJokerData().getEdition();
                    }
                }

                play.addToQueue(item, sticker);
            }

            int numPacks = (a == 1) ? 4 : 6;

            for (int p = 1; p <= numPacks; p++) {
                var pack = inst.nextPack(a);
                Pack packInfo = inst.packInfo(pack);
                Set<String> options = new HashSet<>();

                switch (packInfo.getType()) {
                    case "Celestial Pack" -> {
                        List<String> cards = inst.nextCelestialPack(packInfo.getSize(), a);
                        for (int c = 0; c < packInfo.getSize(); c++) {
                            options.add(cards.get(c));
                        }
                    }
                    case "Arcana Pack" -> {
                        List<String> cards = inst.nextArcanaPack(packInfo.getSize(), a);
                        for (int c = 0; c < packInfo.getSize(); c++) {
                            options.add(cards.get(c));
                        }
                    }
                    case "Spectral Pack" -> {
                        List<String> cards = inst.nextSpectralPack(packInfo.getSize(), a);
                        for (int c = 0; c < packInfo.getSize(); c++) {
                            options.add(cards.get(c));
                        }
                    }
                    case "Buffoon Pack" -> {
                        List<JokerData> cards = inst.nextBuffoonPack(packInfo.getSize(), a);

                        for (int c = 0; c < packInfo.getSize(); c++) {
                            JokerData joker = cards.get(c);
                            var sticker = getSticker(joker);

                            options.add(sticker + " " + joker.getJoker());
                        }
                    }
                    case "Standard Pack" -> {
                        List<Card> cards = inst.nextStandardPack(packInfo.getSize(), a);

                        for (int c = 0; c < packInfo.getSize(); c++) {
                            Card card = cards.get(c);
                            StringBuilder output = new StringBuilder();
                            if (!card.getSeal().equals("No Seal")) {
                                output.append(card.getSeal()).append(" ");
                            }
                            if (!card.getEdition().equals("No Edition")) {
                                output.append(card.getEdition()).append(" ");
                            }
                            if (!card.getEnhancement().equals("No Enhancement")) {
                                output.append(card.getEnhancement()).append(" ");
                            }

                            char rank = card.getBase().charAt(2);

                            switch (rank) {
                                case 'T':
                                    output.append("10");
                                    break;
                                case 'J':
                                    output.append("Jack");
                                    break;
                                case 'Q':
                                    output.append("Queen");
                                    break;
                                case 'K':
                                    output.append("King");
                                    break;
                                case 'A':
                                    output.append("Ace");
                                    break;
                                default:
                                    output.append(rank);
                                    break;
                            }
                            output.append(" of ");
                            char suit = card.getBase().charAt(0);
                            switch (suit) {
                                case 'C':
                                    output.append("Clubs");
                                    break;
                                case 'S':
                                    output.append("Spades");
                                    break;
                                case 'D':
                                    output.append("Diamonds");
                                    break;
                                case 'H':
                                    output.append("Hearts");
                                    break;
                            }
                            options.add(output.toString());
                        }
                    }
                }

                play.addPack(packInfo, options);
            }
        }

        return new Analysis(antes);
    }

    private static String getSticker(JokerData joker) {
        var sticker = "";
        if (joker.getStickers().isEternal()) {
            sticker = "Eternal ";
        }
        if (joker.getStickers().isPerishable()) {
            sticker = "Perishable ";
        }
        if (joker.getStickers().isRental()) {
            sticker = "Rental ";
        }
        if (!joker.getEdition().equals("No Edition")) {
            sticker = joker.getEdition();
        }
        return sticker;
    }
}
