package com.balatro;

import com.balatro.api.Ante;
import com.balatro.api.Item;
import com.balatro.api.Run;
import com.balatro.enums.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

record RunImpl(String seed, List<Ante> antes) implements Run {

    @Override
    public String toJson() {
        try {
            return mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasTag(int ante, Tag tag) {
        return antes.get(ante - 1).hasTag(tag);
    }

    @Override
    public boolean hasTag(Tag tag) {
        return antes.stream().anyMatch(a -> a.hasTag(tag));
    }

    @Override
    public Ante getAnte(int ante) {
        return antes.get(ante - 1);
    }

    @Override
    public boolean hasLegendary(int ante, LegendaryJoker joker, Edition edition) {
        return antes.get(ante - 1).hasLegendary(joker, edition);
    }

    @Override
    public boolean hasInPack(int ante, Item item, Edition edition) {
        return antes.get(ante - 1).hasInPack(item, edition);
    }

    @Override
    public boolean hasInPack(Item item, Edition edition) {
        return antes.stream().anyMatch(a -> a.hasInPack(item, edition));
    }

    @Override
    public boolean hasInShop(int ante, @NotNull Item item, Edition edition) {
        return antes.get(ante - 1).hasInShop(item, edition);
    }

    @Override
    public boolean hasInShop(Item item, Edition edition) {
        return antes.stream().anyMatch(a -> a.hasInShop(item, edition));
    }

    @Override
    public boolean hasInShop(int ante, @NotNull Item item, int index, Edition edition) {
        return antes.get(ante - 1).hasInShop(item, index, edition);
    }

    @Override
    public long countLegendary() {
        return antes.stream()
                .mapToLong(Ante::countLegendary)
                .sum();
    }

    @Override
    public long countLegendary(int ante) {
        return antes.get(ante - 1).countInPack(Specials.THE_SOUL);
    }

    @Override
    public @NotNull String toString() {
        return seed;
    }

    @Override
    public boolean hasInSpectral(@NotNull Item item, Edition edition) {
        for (Ante ante : antes) {
            if (ante.hasInSpectral(item, edition)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasPack(PackType packType) {
        for (Ante ante : antes) {
            if (ante.hasPack(packType)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasInSpectral(int ante, @NotNull Item item, Edition edition) {
        return antes.get(ante - 1).hasInSpectral(item, edition);
    }

    @Override
    public boolean hasPack(int ante, PackType packType) {
        return antes.get(ante - 1).hasPack(packType);
    }

    @Override
    public boolean hasInBuffonPack(@NotNull Item item, Edition edition) {
        for (Ante ante : antes) {
            if (ante.hasInBuffonPack(item, edition)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasInBuffonPack(int ante, @NotNull Item item, Edition edition) {
        return antes.get(ante - 1).hasInBuffonPack(item, edition);
    }

    @Override
    public boolean hasVoucher(Voucher voucher) {
        for (Ante ante : antes) {
            if (ante.getVoucher() == voucher) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasVoucher(int ante, Voucher voucher) {
        return antes.get(ante - 1).getVoucher() == voucher;
    }

    @Override
    public boolean hasBoss(int ante, Boss boss) {
        return false;
    }

    @Override
    public boolean hasBoss(Boss boss) {
        for (Ante ante : antes) {
            if (ante.getBoss() == boss) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasInShop(@NotNull Item item, int index, Edition edition) {
        for (Ante ante : antes) {
            if (ante.hasInShop(item, index, edition)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int countInPack(@NotNull Item item) {
        return antes.stream()
                .mapToInt(a -> a.countInPack(item))
                .sum();
    }

    @Override
    public boolean hasLegendary(LegendaryJoker joker, Edition edition) {
        for (Ante ante : antes) {
            if (ante.hasLegendary(joker, edition)) {
                return true;
            }
        }
        return false;
    }
}
