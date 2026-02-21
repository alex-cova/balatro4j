package com.balatro.api;

import com.balatro.enums.*;
import org.jetbrains.annotations.NotNull;

public interface CommonQueries {

    boolean hasBoss(Boss boss);

    default boolean hasInPack(Item item) {
        return hasInPack(item, Edition.NoEdition);
    }

    boolean hasInPack(Item item, Edition edition);

    default boolean hasInShop(Item item) {
        return hasInShop(item, Edition.NoEdition);
    }

    boolean hasInShop(Item item, Edition edition);

    boolean hasInShop(@NotNull Item item, int index, Edition edition);

    long countLegendary();

    int countInPack(@NotNull Item item);

    boolean hasLegendary(LegendaryJoker joker, Edition edition);

    default boolean hasLegendary(LegendaryJoker joker) {
        return hasLegendary(joker, Edition.NoEdition);
    }

    boolean hasPack(PackType packType);

    default boolean hasInBuffonPack(@NotNull Item item) {
        return hasInBuffonPack(item, Edition.NoEdition);
    }

    boolean hasInBuffonPack(@NotNull Item item, Edition edition);

    boolean hasInSpectral(@NotNull Item item, Edition edition);

    boolean hasVoucher(Voucher voucher);

    boolean hasTag(Tag tag);

}
