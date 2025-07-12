package com.balatro.ui;

import com.balatro.api.AbstractCard;
import com.balatro.api.Item;
import com.balatro.api.Joker;
import com.balatro.enums.*;
import com.balatro.structs.EditionItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SpriteUtil {

    private static BufferedImage jokers;
    private static BufferedImage tarots;
    private static BufferedImage tags;
    private static BufferedImage vouchers;
    private static BufferedImage deck;
    private static BufferedImage enhancers;
    private static BufferedImage editions;
    private static Map<String, Coordinate> jokerSprites;
    private static Map<String, Coordinate> tarotSprites;
    private static Map<String, Coordinate> voucherSprites;
    private static Map<String, Coordinate> tagSprites;

    static ObjectMapper mapper = new ObjectMapper();

    public static void initialize() {
        if (jokers != null) {
            return;
        }

        jokerSprites = new HashMap<>();
        tarotSprites = new HashMap<>();
        voucherSprites = new HashMap<>();
        tagSprites = new HashMap<>();

        try {
            loadJson("/json/jokers.json", jokerSprites);
            loadJson("/json/tarots.json", tarotSprites);
            loadJson("/json/vouchers.json", voucherSprites);
            loadJson("/json/tags.json", tagSprites);

            jokers = ImageIO.read(Objects.requireNonNull(SpriteUtil.class.getResourceAsStream("/images/Jokers.png")));
            tarots = ImageIO.read(Objects.requireNonNull(SpriteUtil.class.getResourceAsStream("/images/Tarots.png")));
            vouchers = ImageIO.read(Objects.requireNonNull(SpriteUtil.class.getResourceAsStream("/images/Vouchers.png")));
            tags = ImageIO.read(Objects.requireNonNull(SpriteUtil.class.getResourceAsStream("/images/Tags.png")));
            deck = ImageIO.read(Objects.requireNonNull(SpriteUtil.class.getResourceAsStream("/images/8BitDeck.png")));
            enhancers = ImageIO.read(Objects.requireNonNull(SpriteUtil.class.getResourceAsStream("/images/Enhancers.png")));
            editions = ImageIO.read(Objects.requireNonNull(SpriteUtil.class.getResourceAsStream("/images/Editions.png")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadJson(String fileName, Map<String, Coordinate> map) {
        List<Sprite> list = null;
        try {
            list = mapper.readValue(SpriteUtil.class.getResourceAsStream(fileName), mapper.getTypeFactory()
                    .constructCollectionType(List.class, Sprite.class));
            for (Sprite sprite : list) {
                map.put(sprite.name(), sprite.pos());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static @Nullable BufferedImage getSprite(@NotNull EditionItem item) {
        var base = getSprite(item.item());

        if (base == null) {
            return null;
        }

        if (item.edition() == Edition.NoEdition) {
            return base;
        }

        if (item.edition() == Edition.Negative) {
            return convertToNegative(base);
        }

        var enhancement = getEdition(item.edition());

        if (enhancement == null) {
            return base;
        }

        return merge(base, enhancement);
    }

    private static @NotNull BufferedImage convertToNegative(@NotNull BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();

        BufferedImage negative = new BufferedImage(width, height, original.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgba = original.getRGB(x, y);
                int a = (rgba >> 24) & 0xff;
                int r = (rgba >> 16) & 0xff;
                int g = (rgba >> 8) & 0xff;
                int b = rgba & 0xff;

                // Invert colors
                r = 255 - r;
                g = 255 - g;
                b = 255 - b;

                // Reconstruct pixel and set it
                int negativeRGBA = (a << 24) | (r << 16) | (g << 8) | b;
                negative.setRGB(x, y, negativeRGBA);
            }
        }

        return negative;
    }


    private static @Nullable BufferedImage getSprite(Item item) {
        initialize();

        if (item instanceof LegendaryJoker) {
            var coord = jokerSprites.get(item.getName());
            var coord2 = new Coordinate(coord.x(), coord.y() + 1);

            var a = sprite(71, 95, coord, jokers);
            var b = sprite(71, 95, coord2, jokers);

            return merge(a, b);
        }

        if (item instanceof Joker joker) {
            var coord = jokerSprites.get(item.getName());

            if (joker.getName().equals(UnCommonJoker101C.Hologram.getName())) {
                var canio = jokerSprites.get("Canio");
                var face = new Coordinate(canio.x() - 1, canio.y() + 1);

                return merge(sprite(71, 95, coord, jokers), sprite(71, 95, face, jokers));
            }

            return sprite(71, 95, coord, jokers);
        }

        if (item instanceof Voucher) {
            var coord = voucherSprites.get(item.getName());

            return sprite(71, 95, coord, vouchers);
        }

        if (item instanceof Spectral || item instanceof Tarot || item instanceof Planet || item instanceof Specials) {
            var coord = tarotSprites.get(item.getName());

            return sprite(71, 95, coord, tarots);
        }

        if (item instanceof Tag) {
            var coord = tagSprites.get(item.getName());

            return sprite(34, 34, coord, tags);
        }

        if (item instanceof AbstractCard card) {
            var coord = new Coordinate(card.card().getRank().index(), card.card().getSuit().index());
            var edition = getCoordinate(card.card().enhancement());

            var a = sprite(71, 95, coord, deck);
            var b = sprite(71, 95, edition, enhancers);

            return merge(b, a);
        }

        System.out.println("Unknown item: " + item.getClass().getName());

        return null;
    }

    private static @Nullable BufferedImage getEdition(@NotNull Edition edition) {
        if (edition.getEnhancerIndex() == -1) return null;

        return sprite(71, 94, new Coordinate(edition.getEnhancerIndex(), 0), editions);

    }

    @Contract("_ -> new")
    private static @NotNull Coordinate getCoordinate(Enhancement en) {
        int x = 1, y = 0;

        if (en == Enhancement.Luck) {
            x = 4;
            y = 1;
        } else if (en == Enhancement.Bonus) {
            x = 1;
            y = 1;
        } else if (en == Enhancement.Wild) {
            x = 3;
            y = 1;
        } else if (en == Enhancement.Gold) {
            x = 6;
            y = 0;
        } else if (en == Enhancement.Stone) {
            x = 5;
            y = 0;
        } else if (en == Enhancement.Steel) {
            x = 6;
            y = 1;
        } else if (en == Enhancement.Glass) {
            x = 5;
            y = 1;
        } else if (en == Enhancement.Mult) {
            x = 2;
            y = 1;
        }
        return new Coordinate(x, y);
    }

    private static BufferedImage sprite(int w, int h, Coordinate coordinate, @NotNull BufferedImage image) {
        if (coordinate == null) return null;
        return image.getSubimage(coordinate.x() * w, coordinate.y() * h, w, h);
    }

    private static @NotNull BufferedImage merge(@NotNull BufferedImage image1, BufferedImage image2) {
        var result = new BufferedImage(image1.getWidth(), image1.getHeight(), image1.getType());

        result.getGraphics().drawImage(image1, 0, 0, image1.getWidth(), image1.getHeight(), null);
        result.getGraphics().drawImage(image2, 0, 0, image2.getWidth(), image2.getHeight(), null);
        return result;
    }

}
